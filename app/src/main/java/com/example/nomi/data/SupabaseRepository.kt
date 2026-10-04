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
            // Enviamos los datos adicionales como METADATOS
            // Esto permite que el TRIGGER de PostgreSQL los reciba al instante
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                data = buildJsonObject {
                    put("nombre", datos.nombre)
                    put("tipo_doc", datos.tipo_doc ?: "CC")
                    put("num_doc", datos.num_doc ?: "")
                    put("telefono", datos.telefono ?: "")
                    put("direccion", datos.direccion ?: "")
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
            client.from("pedidos").insert(pedido)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
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
