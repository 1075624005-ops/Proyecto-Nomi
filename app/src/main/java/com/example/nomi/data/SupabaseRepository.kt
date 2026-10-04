package com.example.nomi.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseRepository {
    private val client = SupabaseClient.client

    // --- AUTENTICACIÓN Y PERFIL ---

    suspend fun login(email: String, pass: String): Result<UsuarioPostgres> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            val usuario = client.from("usuarios").select {
                filter {
                    eq("correo", email)
                }
            }.decodeSingle<UsuarioPostgres>()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(email: String, pass: String, datos: UsuarioPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userResponse = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                data = buildJsonObject {
                    put("nombre", datos.nombre)
                    put("tipo_doc", datos.tipo_doc ?: "CC")
                    put("num_doc", datos.num_doc ?: "")
                    put("telefono", datos.telefono ?: "")
                    put("direccion", datos.direccion ?: "")
                    put("rol", datos.rol)
                    put("placa", datos.placa ?: "")
                    put("area", datos.area ?: "")
                }
            }

            val uid = userResponse?.id ?: java.util.UUID.randomUUID().toString()
            val usuarioFinal = datos.copy(id = uid)

            try {
                client.from("usuarios").insert(usuarioFinal)
            } catch (e: Exception) {
                // Si ya existe la fila, actualizamos rol, placa y area
                client.from("usuarios").update({
                    set("rol", datos.rol)
                    set("placa", datos.placa ?: "")
                    set("area", datos.area ?: "")
                }) {
                    filter {
                        eq("correo", email)
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE USUARIOS ---

    suspend fun obtenerTodosLosUsuarios(): Result<List<UsuarioPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("usuarios").select().decodeList<UsuarioPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE PEDIDOS ---

    suspend fun crearPedido(pedido: PedidoPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Intentar insertar modelo completo
            client.from("pedidos").insert(pedido)
            Result.success(true)
        } catch (e: Exception) {
            try {
                // Insertar exactamente con la estructura de columnas existente en Supabase (num_guia, id_mensajero, estado, costo)
                @kotlinx.serialization.Serializable
                data class PedidoTablaSupabase(
                    val num_guia: String,
                    val id_mensajero: String? = null,
                    val estado: Int = 1,
                    val costo: Double? = 0.0
                )

                val pedidoDirecto = PedidoTablaSupabase(
                    num_guia = pedido.num_guia,
                    id_mensajero = if (pedido.id_mensajero.isNullOrEmpty()) null else pedido.id_mensajero,
                    estado = pedido.estado,
                    costo = pedido.costo
                )
                client.from("pedidos").insert(pedidoDirecto)
                Result.success(true)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    suspend fun obtenerPedidosAdmin(): Result<List<PedidoPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pedidos").select().decodeList<PedidoPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstadoPedido(guia: String, nuevoEstado: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                {
                    set("estado", nuevoEstado)
                }
            ) {
                filter {
                    eq("num_guia", guia)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarMensajeroAPedido(guia: String, idMensajero: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                {
                    set("id_mensajero", idMensajero)
                    set("estado", 2) // Pasa automáticamente a "EN CAMINO / EN RUTA"
                }
            ) {
                filter {
                    eq("num_guia", guia)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE PQRS ---

    suspend fun enviarPQRS(pqr: PQRSPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pqrs").insert(pqr)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerTodasLasPQRS(): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select().decodeList<PQRSPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMisPQRS(correo: String): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select {
                filter {
                    eq("correo_usuario", correo)
                }
            }.decodeList<PQRSPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPQRSPorRadicado(radicado: Int): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select {
                filter {
                    eq("id", radicado)
                }
            }.decodeList<PQRSPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun responderPQRS(idRadicado: Int, respuestaTexto: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pqrs").update(
                {
                    set("respuesta", respuestaTexto)
                    set("estado", "Resuelto")
                }
            ) {
                filter {
                    eq("id", idRadicado)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerEstadoPedido(guia: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pedido = client.from("pedidos").select {
                filter {
                    eq("num_guia", guia)
                }
            }.decodeSingle<PedidoPostgres>()
            Result.success(pedido.estado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
