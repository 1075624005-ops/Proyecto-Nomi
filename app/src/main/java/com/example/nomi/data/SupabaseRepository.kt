package com.example.nomi.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {
    private val client = SupabaseClient.client

    // --- AUTENTICACIÓN Y BÚSQUEDA EN LAS 4 TABLAS DE PERFILES ---

    suspend fun login(email: String, pass: String): Result<UsuarioResultado> = withContext(Dispatchers.IO) {
        try {
            // 1. Autenticar credenciales en Supabase Auth
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }

            // 2. Buscar progresivamente en las 4 tablas
            // a) ¿Es Administrador?
            try {
                val admin = client.from("perfil_admin").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilAdmin>()
                if (admin != null) return@withContext Result.success(UsuarioResultado.Admin(admin))
            } catch (_: Exception) {}

            // b) ¿Es Asesor?
            try {
                val asesor = client.from("perfil_asesor").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilAsesor>()
                if (asesor != null) return@withContext Result.success(UsuarioResultado.Asesor(asesor))
            } catch (_: Exception) {}

            // c) ¿Es Mensajero?
            try {
                val mensajero = client.from("perfil_mensajero").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilMensajero>()
                if (mensajero != null) return@withContext Result.success(UsuarioResultado.Mensajero(mensajero))
            } catch (_: Exception) {}

            // d) ¿Es Cliente?
            try {
                val cliente = client.from("perfil_cliente").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilCliente>()
                if (cliente != null) return@withContext Result.success(UsuarioResultado.Cliente(cliente))
            } catch (_: Exception) {}

            Result.failure(Exception("Usuario autenticado pero no se encontró su perfil en las tablas"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTRO DE CLIENTE ---
    suspend fun registrarCliente(email: String, pass: String, cliente: PerfilCliente): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = userAuth?.id ?: ""
            val clienteFinal = cliente.copy(id_usuario = userId)

            client.from("perfil_cliente").insert(clienteFinal)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTRO DE MENSAJERO (POR EL ADMINISTRADOR) ---
    suspend fun registrarMensajero(email: String, pass: String, mensajero: PerfilMensajero): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = userAuth?.id ?: ""
            val mensajeroFinal = mensajero.copy(id_usuario = userId)

            client.from("perfil_mensajero").insert(mensajeroFinal)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTRO DE ASESOR (POR EL ADMINISTRADOR) ---
    suspend fun registrarAsesor(email: String, pass: String, asesor: PerfilAsesor): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = userAuth?.id ?: ""
            val asesorFinal = asesor.copy(id_usuario = userId)

            client.from("perfil_asesor").insert(asesorFinal)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CONSULTAS DE USUARIOS / PERFILES ---

    suspend fun obtenerTodosLosClientes(): Result<List<PerfilCliente>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_cliente").select().decodeList<PerfilCliente>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerTodosLosMensajeros(): Result<List<PerfilMensajero>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_mensajero").select().decodeList<PerfilMensajero>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerTodosLosAsesores(): Result<List<PerfilAsesor>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_asesor").select().decodeList<PerfilAsesor>()
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
                { set("estado", nuevoEstado) }
            ) {
                filter { eq("num_guia", guia) }
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
                filter { eq("correo_usuario", correo) }
            }.decodeList<PQRSPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPQRSPorRadicado(radicado: Int): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select {
                filter { eq("id", radicado) }
            }.decodeList<PQRSPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerEstadoPedido(guia: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pedido = client.from("pedidos").select {
                filter { eq("num_guia", guia) }
            }.decodeSingle<PedidoPostgres>()
            Result.success(pedido.estado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
