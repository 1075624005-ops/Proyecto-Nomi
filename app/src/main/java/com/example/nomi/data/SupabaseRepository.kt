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
                    put("apellido", datos.apellido ?: "")
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
            val usuarioFinal = datos.copy(id_usuario = uid, id = uid)

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

            // Insertar en la tabla especializada de perfil e insertar el registro legal de Habeas Data
            try {
                if (datos.rol.lowercase() == "cliente") {
                    val perfilCliente = PerfilClientePostgres(
                        id_usuario = uid,
                        tipo_doc = datos.tipo_doc ?: "CC",
                        numero_doc = datos.num_doc ?: "",
                        direccion = datos.direccion
                    )
                    client.from("perfil_cliente").insert(perfilCliente)
                } else if (datos.rol.lowercase() == "mensajero") {
                    val perfilMensajero = PerfilMensajeroPostgres(
                        id_usuario = uid,
                        placa_vehiculo = datos.placa ?: "SIN-PLACA",
                        tipo_vehiculo = "Moto",
                        licencia_conduccion = "Licencia Registrada",
                        zona_asignada = datos.area ?: "Bogotá D.C."
                    )
                    client.from("perfil_mensajero").insert(perfilMensajero)
                } else if (datos.rol.lowercase() == "admin") {
                    val perfilAdmin = PerfilAdminPostgres(
                        id_usuario = uid,
                        nivel_acceso = "Superusuario"
                    )
                    client.from("perfil_admin").insert(perfilAdmin)
                }

                // Guardar registro de consentimiento de la Ley 1581 / 2012 (Habeas Data)
                val habeasData = HabeasDataConsentPostgres(
                    id_usuario = uid,
                    aceptado = true,
                    version_politica = "v1.0 - Ley 1581 de 2012"
                )
                client.from("habeas_data_consent").insert(habeasData)
            } catch (eChild: Exception) {
                eChild.printStackTrace()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarPerfilCliente(perfil: PerfilClientePostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("perfil_cliente").insert(perfil)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarPerfilMensajero(perfil: PerfilMensajeroPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("perfil_mensajero").insert(perfil)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE USUARIOS Y MENSAJEROS ---

    suspend fun obtenerTodosLosUsuarios(): Result<List<UsuarioPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("usuarios").select().decodeList<UsuarioPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMensajerosActivos(): Result<List<UsuarioPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("usuarios").select {
                filter {
                    eq("rol", "mensajero")
                }
            }.decodeList<UsuarioPostgres>()
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

    suspend fun crearPedidoConReintento(
        pedidoBase: PedidoPostgres,
        codigoLocalidad: String = "01"
    ): Result<PedidoPostgres> = withContext(Dispatchers.IO) {
        var intento = 0
        var ultimoError: Exception? = null
        var pedidoActual = pedidoBase

        while (intento < 5) {
            try {
                client.from("pedidos").insert(pedidoActual)
                return@withContext Result.success(pedidoActual)
            } catch (e: Exception) {
                ultimoError = e
                intento++
                // Generar nueva clave primaria para evitar colisión de clave única
                val nuevoSecuencial = (1000..9999).random()
                val nuevaGuia = "N${codigoLocalidad.padStart(2, '0')}${nuevoSecuencial}"
                pedidoActual = pedidoActual.copy(num_guia = nuevaGuia)
            }
        }
        Result.failure(ultimoError ?: Exception("No se pudo generar la guía de pedido tras $intento reintentos"))
    }

    suspend fun obtenerPedidosAdmin(): Result<List<PedidoPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pedidos").select().decodeList<PedidoPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMisPedidos(idOEmailCliente: String): Result<List<PedidoPostgres>> = withContext(Dispatchers.IO) {
        try {
            val todos = client.from("pedidos").select().decodeList<PedidoPostgres>()
            val misPedidos = todos.filter { p ->
                p.id_cliente?.equals(idOEmailCliente, ignoreCase = true) == true ||
                p.rem_nombre?.equals(idOEmailCliente, ignoreCase = true) == true ||
                p.rem_tel?.equals(idOEmailCliente, ignoreCase = true) == true
            }
            Result.success(misPedidos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPedidoPorGuia(guia: String): Result<PedidoPostgres?> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pedidos").select {
                filter {
                    eq("num_guia", guia.trim())
                }
            }.decodeList<PedidoPostgres>()
            Result.success(lista.firstOrNull())
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

    suspend fun actualizarEstadoPagoPedido(guia: String, nuevoEstadoPago: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                {
                    set("estado_pago", nuevoEstadoPago)
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

    suspend fun asignarMensajeroYEstado(
        guia: String,
        idMensajero: String,
        nuevoEstado: Int = 2
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                {
                    set("id_mensajero", idMensajero)
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
