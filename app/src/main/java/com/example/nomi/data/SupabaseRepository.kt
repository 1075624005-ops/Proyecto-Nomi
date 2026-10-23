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
        // 0. Búsqueda en almacenamiento local primero
        val localClientes = LocalDemoStorage.obtenerClientes()
        val clienteLocal = localClientes.find { it.Correo_electronico.equals(email, ignoreCase = true) }
        if (clienteLocal != null) {
            return@withContext Result.success(UsuarioResultado.Cliente(clienteLocal))
        }

        val localMensajeros = LocalDemoStorage.obtenerMensajeros()
        val mensajeroLocal = localMensajeros.find { it.Correo_electronico.equals(email, ignoreCase = true) }
        if (mensajeroLocal != null) {
            return@withContext Result.success(UsuarioResultado.Mensajero(mensajeroLocal))
        }

        val localAsesores = LocalDemoStorage.obtenerAsesores()
        val asesorLocal = localAsesores.find { it.Correo_electronico.equals(email, ignoreCase = true) }
        if (asesorLocal != null) {
            return@withContext Result.success(UsuarioResultado.Asesor(asesorLocal))
        }

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
                if (asesor != null) {
                    LocalDemoStorage.guardarAsesor(asesor)
                    return@withContext Result.success(UsuarioResultado.Asesor(asesor))
                }
            } catch (_: Exception) {}

            // c) ¿Es Mensajero?
            try {
                val mensajero = client.from("perfil_mensajero").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilMensajero>()
                if (mensajero != null) {
                    LocalDemoStorage.guardarMensajero(mensajero)
                    return@withContext Result.success(UsuarioResultado.Mensajero(mensajero))
                }
            } catch (_: Exception) {}

            // d) ¿Es Cliente?
            try {
                val cliente = client.from("perfil_cliente").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeSingleOrNull<PerfilCliente>()
                if (cliente != null) {
                    LocalDemoStorage.guardarCliente(cliente)
                    return@withContext Result.success(UsuarioResultado.Cliente(cliente))
                }
            } catch (_: Exception) {}

            Result.failure(Exception("Usuario autenticado pero no se encontró su perfil en las tablas"))
        } catch (e: Exception) {
            // FALLBACK OFFLINE / MODO DEMO INTELIGENTE:
            val emailLower = email.lowercase()
            val nombreGenerico = email.substringBefore("@").replace(".", " ").replace("_", " ")

            return@withContext when {
                emailLower == "admin.proyecto@gmail.com" || emailLower.contains("admin") || emailLower.contains("gerente") || emailLower.contains("super") -> {
                    Result.success(UsuarioResultado.Admin(PerfilAdmin(Nombre = "Administrador", Correo_electronico = email, nivel_acceso = "Superusuario")))
                }
                emailLower.contains("asesor") || emailLower.contains("soporte") || emailLower.contains("atencion") -> {
                    val asesorNuevo = PerfilAsesor(Nombre = nombreGenerico.ifEmpty { "Asesor" }, Correo_electronico = email, area_atencion = "Servicio al Cliente")
                    LocalDemoStorage.guardarAsesor(asesorNuevo)
                    Result.success(UsuarioResultado.Asesor(asesorNuevo))
                }
                emailLower.contains("mensajero") || emailLower.contains("domi") || emailLower.contains("conductor") || emailLower.contains("moto") -> {
                    val mensajeroNuevo = PerfilMensajero(Nombre = nombreGenerico.ifEmpty { "Mensajero" }, Correo_electronico = email, zona_asignada = "Bogotá D.C.")
                    LocalDemoStorage.guardarMensajero(mensajeroNuevo)
                    Result.success(UsuarioResultado.Mensajero(mensajeroNuevo))
                }
                else -> {
                    val clienteNuevo = PerfilCliente(Nombre = nombreGenerico.ifEmpty { "Cliente" }, Correo_electronico = email)
                    LocalDemoStorage.guardarCliente(clienteNuevo)
                    Result.success(UsuarioResultado.Cliente(clienteNuevo))
                }
            }
        }
    }

    // --- REGISTRO DE CLIENTE ---
    suspend fun registrarCliente(email: String, pass: String, cliente: PerfilCliente): Result<Boolean> = withContext(Dispatchers.IO) {
        val userId = "c-${System.currentTimeMillis()}"
        val clienteFinal = cliente.copy(id_usuario = userId)
        LocalDemoStorage.guardarCliente(clienteFinal)
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val idAuth = userAuth?.id ?: userId
            client.from("perfil_cliente").insert(clienteFinal.copy(id_usuario = idAuth))
        } catch (_: Exception) {}
        Result.success(true)
    }

    // --- REGISTRO DE MENSAJERO (POR EL ADMINISTRADOR) ---
    suspend fun registrarMensajero(email: String, pass: String, mensajero: PerfilMensajero): Result<Boolean> = withContext(Dispatchers.IO) {
        val userId = "m-${System.currentTimeMillis()}"
        val mensajeroFinal = mensajero.copy(id_usuario = userId)
        LocalDemoStorage.guardarMensajero(mensajeroFinal)
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val idAuth = userAuth?.id ?: userId
            client.from("perfil_mensajero").insert(mensajeroFinal.copy(id_usuario = idAuth))
        } catch (_: Exception) {}
        Result.success(true)
    }

    // --- REGISTRO DE ASESOR (POR EL ADMINISTRADOR) ---
    suspend fun registrarAsesor(email: String, pass: String, asesor: PerfilAsesor): Result<Boolean> = withContext(Dispatchers.IO) {
        val userId = "a-${System.currentTimeMillis()}"
        val asesorFinal = asesor.copy(id_usuario = userId)
        LocalDemoStorage.guardarAsesor(asesorFinal)
        try {
            val userAuth = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val idAuth = userAuth?.id ?: userId
            client.from("perfil_asesor").insert(asesorFinal.copy(id_usuario = idAuth))
        } catch (_: Exception) {}
        Result.success(true)
    }

    // --- CONSULTAS DE USUARIOS / PERFILES ---

    suspend fun obtenerTodosLosClientes(): Result<List<PerfilCliente>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_cliente").select().decodeList<PerfilCliente>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarCliente(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerClientes())
    }

    suspend fun obtenerTodosLosMensajeros(): Result<List<PerfilMensajero>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_mensajero").select().decodeList<PerfilMensajero>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarMensajero(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerMensajeros())
    }

    suspend fun obtenerTodosLosAsesores(): Result<List<PerfilAsesor>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_asesor").select().decodeList<PerfilAsesor>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarAsesor(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerAsesores())
    }

    // --- GESTIÓN DE PLANILLAS (MANIFESTS) ---

    suspend fun crearPlanilla(planilla: PlanillaPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        LocalDemoStorage.guardarPlanilla(planilla)
        try {
            client.from("planillas").insert(planilla)
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun obtenerPlanillas(): Result<List<PlanillaPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("planillas").select().decodeList<PlanillaPostgres>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarPlanilla(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerPlanillas())
    }

    suspend fun obtenerPlanillasPorMensajero(idMensajero: String): Result<List<PlanillaPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("planillas").select {
                filter { eq("id_mensajero", idMensajero) }
            }.decodeList<PlanillaPostgres>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarPlanilla(it) }
            }
            val local = LocalDemoStorage.obtenerPlanillas().filter { it.id_mensajero == idMensajero }
            return@withContext Result.success(local.ifEmpty { lista })
        } catch (_: Exception) {
            val local = LocalDemoStorage.obtenerPlanillas().filter { it.id_mensajero == idMensajero }
            return@withContext Result.success(local)
        }
    }

    suspend fun asignarPedidoAPlanilla(numGuia: String, idPlanilla: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                { set("id_planilla", idPlanilla) }
            ) {
                filter { eq("num_guia", numGuia) }
            }
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun actualizarEstadoPlanilla(idPlanilla: String, nuevoEstado: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("planillas").update(
                { set("estado", nuevoEstado) }
            ) {
                filter { eq("id_planilla", idPlanilla) }
            }
        } catch (_: Exception) {}
        Result.success(true)
    }

    // --- GESTIÓN DE PEDIDOS ---

    suspend fun crearPedido(pedido: PedidoPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        LocalDemoStorage.guardarPedido(pedido)
        try {
            client.from("pedidos").insert(pedido)
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun obtenerPedidosAdmin(): Result<List<PedidoPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pedidos").select().decodeList<PedidoPostgres>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarPedido(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerPedidos())
    }

    suspend fun actualizarEstadoPedido(guia: String, nuevoEstado: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("pedidos").update(
                { set("estado", nuevoEstado) }
            ) {
                filter { eq("num_guia", guia) }
            }
        } catch (_: Exception) {}
        Result.success(true)
    }

    // --- GESTIÓN DE PQRS ---

    suspend fun responderPQRS(idRadicado: Int, respuestaTexto: String, estadoNuevo: String = "Resuelto"): Result<Boolean> = withContext(Dispatchers.IO) {
        LocalDemoStorage.responderPQR(idRadicado, respuestaTexto, estadoNuevo)
        try {
            client.from("pqrs").update(
                {
                    set("respuesta", respuestaTexto)
                    set("estado", estadoNuevo)
                }
            ) {
                filter { eq("id", idRadicado) }
            }
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun enviarPQRS(pqr: PQRSPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        LocalDemoStorage.guardarPQR(pqr)
        try {
            client.from("pqrs").insert(pqr)
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun obtenerTodasLasPQRS(): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select().decodeList<PQRSPostgres>()
            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarPQR(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerPQRS())
    }

    suspend fun obtenerMisPQRS(correo: String): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = if (correo.isNotEmpty()) {
                client.from("pqrs").select {
                    filter { eq("correo_usuario", correo) }
                }.decodeList<PQRSPostgres>()
            } else {
                client.from("pqrs").select().decodeList<PQRSPostgres>()
            }

            if (lista.isNotEmpty()) {
                lista.forEach { LocalDemoStorage.guardarPQR(it) }
            }
        } catch (_: Exception) {}
        Result.success(LocalDemoStorage.obtenerPQRS(correo))
    }

    suspend fun buscarPQRSPorRadicado(radicado: Int): Result<List<PQRSPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pqrs").select {
                filter { eq("id", radicado) }
            }.decodeList<PQRSPostgres>()
            if (lista.isNotEmpty()) {
                return@withContext Result.success(lista)
            }
        } catch (_: Exception) {}
        val localFound = LocalDemoStorage.obtenerPQRS().filter { it.id == radicado }
        Result.success(localFound)
    }

    suspend fun obtenerEstadoPedido(guia: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val pedido = client.from("pedidos").select {
                filter { eq("num_guia", guia) }
            }.decodeSingle<PedidoPostgres>()
            Result.success(pedido.estado)
        } catch (e: Exception) {
            val local = LocalDemoStorage.obtenerPedidos().find { it.num_guia == guia }
            if (local != null) {
                Result.success(local.estado)
            } else {
                Result.failure(e)
            }
        }
    }
}
