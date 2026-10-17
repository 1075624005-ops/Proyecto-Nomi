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

    suspend fun login(email: String, pass: String): Result<DatosUsuarioLogin> = withContext(Dispatchers.IO) {
        try {
            // 1. Autenticar credenciales en Supabase Auth
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }

            // 2. Buscar primero en la tabla de ADMINISTRADORES
            try {
                val adminList = client.from("perfil_admin").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeList<PerfilAdminPostgres>()

                if (adminList.isNotEmpty()) {
                    val a = adminList[0]
                    val nombreAdmin = "${a.Nombre ?: "Administrador"} ${a.Apellido ?: ""}".trim().ifEmpty { "Administrador NOMI" }
                    return@withContext Result.success(DatosUsuarioLogin(nombre = nombreAdmin, correo = email, rol = "admin"))
                }
            } catch (e: Exception) {}

            // 3. Buscar en la tabla de ASESORES
            try {
                val asesorList = client.from("perfil_asesor").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeList<PerfilAsesorPostgres>()

                if (asesorList.isNotEmpty()) {
                    val a = asesorList[0]
                    val nombreAsesor = "${a.Nombre ?: "Asesor"} ${a.Apellido ?: ""}".trim().ifEmpty { "Asesor NOMI" }
                    return@withContext Result.success(DatosUsuarioLogin(nombre = nombreAsesor, correo = email, rol = "asesor"))
                }
            } catch (e: Exception) {}

            // 4. Buscar en la tabla de MENSAJEROS / DOMICILIARIOS
            try {
                val mensajeroList = client.from("perfil_mensajero").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeList<PerfilMensajeroPostgres>()

                if (mensajeroList.isNotEmpty()) {
                    val m = mensajeroList[0]
                    val nombreMensajero = "${m.Nombre ?: "Mensajero"} ${m.Apellido ?: ""}".trim().ifEmpty { "Mensajero NOMI" }
                    return@withContext Result.success(DatosUsuarioLogin(nombre = nombreMensajero, correo = email, rol = "mensajero"))
                }
            } catch (e: Exception) {}

            // 5. Buscar en la tabla de CLIENTES
            try {
                val clienteList = client.from("perfil_cliente").select {
                    filter { eq("Correo_electronico", email) }
                }.decodeList<PerfilClientePostgres>()

                if (clienteList.isNotEmpty()) {
                    val c = clienteList[0]
                    val nombreCliente = "${c.Nombre ?: ""} ${c.Apellido ?: ""}".trim().ifEmpty { "Cliente Nomi" }
                    return@withContext Result.success(DatosUsuarioLogin(nombre = nombreCliente, correo = email, rol = "cliente"))
                }
            } catch (e: Exception) {}

            // Fallback si autenticó pero no está en tablas
            Result.success(DatosUsuarioLogin(nombre = "Usuario Nomi", correo = email, rol = "cliente"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarUsuario(
        email: String,
        pass: String,
        nombre: String,
        apellido: String = "",
        tipoDoc: String = "CC",
        numDoc: String = "",
        telefono: String = "",
        direccion: String = "",
        rol: String = "cliente",
        placa: String = "",
        area: String = ""
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Crear el usuario en Supabase Auth
            val userResponse = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }

            val uid = userResponse?.id ?: java.util.UUID.randomUUID().toString()

            // 2. Guardar directamente en la tabla del Rol correspondiente en Supabase
            when (rol.lowercase().trim()) {
                "cliente" -> {
                    val perfilCliente = PerfilClientePostgres(
                        id_usuario = uid,
                        Nombre = nombre,
                        Apellido = apellido,
                        tipo_doc = tipoDoc,
                        numero_doc = numDoc,
                        Codigo_pais = "+57",
                        Telefono = telefono.toLongOrNull() ?: 0L,
                        Correo_electronico = email,
                        direccion = direccion
                    )
                    client.from("perfil_cliente").insert(perfilCliente)
                }
                "mensajero" -> {
                    val perfilMensajero = PerfilMensajeroPostgres(
                        id_usuario = uid,
                        Nombre = nombre,
                        Apellido = apellido,
                        Correo_electronico = email,
                        Telefono = telefono.toLongOrNull() ?: 0L,
                        Codigo_pais = "+57",
                        numero_doc = numDoc,
                        placa_vehiculo = if (placa.isNotEmpty()) placa else "SIN-PLACA",
                        tipo_vehiculo = "Moto",
                        licencia_conduccion = "Licencia Registrada",
                        vencimiento_soat = "2027-12-31",
                        vencimiento_tecnomecanica = "2027-12-31",
                        zona_asignada = if (area.isNotEmpty()) area else "Bogotá D.C."
                    )
                    client.from("perfil_mensajero").insert(perfilMensajero)
                }
                "asesor" -> {
                    val perfilAsesor = PerfilAsesorPostgres(
                        id_usuario = uid,
                        Nombre = nombre,
                        Apellido = apellido,
                        Correo_electronico = email,
                        Telefono = telefono.toLongOrNull() ?: 0L,
                        codigo_asesor = "ASE-${(100..999).random()}",
                        area_atencion = "Servicio al Cliente"
                    )
                    client.from("perfil_asesor").insert(perfilAsesor)
                }
                "admin" -> {
                    val perfilAdmin = PerfilAdminPostgres(
                        id_usuario = uid,
                        Nombre = nombre,
                        Apellido = apellido,
                        Correo_electronico = email,
                        nivel_acceso = "Superusuario"
                    )
                    client.from("perfil_admin").insert(perfilAdmin)
                }
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

    // --- GESTIÓN DE USUARIOS Y MENSAJEROS ---

    suspend fun obtenerTodosLosUsuarios(): Result<List<DatosUsuarioLogin>> = withContext(Dispatchers.IO) {
        try {
            val listaCompleta = mutableListOf<DatosUsuarioLogin>()

            // 1. Cargar Clientes
            try {
                val clientes = client.from("perfil_cliente").select().decodeList<PerfilClientePostgres>()
                for (c in clientes) {
                    val nom = "${c.Nombre ?: ""} ${c.Apellido ?: ""}".trim().ifEmpty { "Cliente Nomi" }
                    listaCompleta.add(DatosUsuarioLogin(nombre = nom, correo = c.Correo_electronico ?: "", rol = "cliente"))
                }
            } catch (e: Exception) {}

            // 2. Cargar Mensajeros
            try {
                val mensajeros = client.from("perfil_mensajero").select().decodeList<PerfilMensajeroPostgres>()
                for (m in mensajeros) {
                    val nom = "${m.Nombre ?: ""} ${m.Apellido ?: ""}".trim().ifEmpty { "Mensajero NOMI" }
                    listaCompleta.add(DatosUsuarioLogin(nombre = nom, correo = m.Correo_electronico ?: "", rol = "mensajero"))
                }
            } catch (e: Exception) {}

            // 3. Cargar Asesores
            try {
                val asesores = client.from("perfil_asesor").select().decodeList<PerfilAsesorPostgres>()
                for (a in asesores) {
                    val nom = "${a.Nombre ?: ""} ${a.Apellido ?: ""}".trim().ifEmpty { "Asesor NOMI" }
                    listaCompleta.add(DatosUsuarioLogin(nombre = nom, correo = a.Correo_electronico ?: "", rol = "asesor"))
                }
            } catch (e: Exception) {}

            // 4. Cargar Administradores
            try {
                val admins = client.from("perfil_admin").select().decodeList<PerfilAdminPostgres>()
                for (adm in admins) {
                    val nom = "${adm.Nombre ?: ""} ${adm.Apellido ?: ""}".trim().ifEmpty { "Admin NOMI" }
                    listaCompleta.add(DatosUsuarioLogin(nombre = nom, correo = adm.Correo_electronico ?: "", rol = "admin"))
                }
            } catch (e: Exception) {}

            Result.success(listaCompleta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerMensajerosActivos(): Result<List<PerfilMensajeroPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("perfil_mensajero").select().decodeList<PerfilMensajeroPostgres>()
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cambiarRolUsuario(idUsuario: String, email: String, nuevoRol: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Actualizar rol en la tabla padre 'usuarios'
            client.from("usuarios").update({
                set("rol", nuevoRol)
            }) {
                filter {
                    eq("correo", email)
                }
            }

            // 2. Limpiar perfiles antiguos e insertar en la tabla especializada del nuevo rol
            try {
                if (nuevoRol.lowercase() == "mensajero") {
                    val perfilM = PerfilMensajeroPostgres(
                        id_usuario = idUsuario,
                        placa_vehiculo = "POR-ASIGNAR",
                        tipo_vehiculo = "Moto",
                        licencia_conduccion = "Licencia Registrada",
                        zona_asignada = "Bogotá D.C."
                    )
                    client.from("perfil_mensajero").insert(perfilM)
                } else if (nuevoRol.lowercase() == "asesor") {
                    val perfilA = PerfilAsesorPostgres(
                        id_usuario = idUsuario,
                        codigo_asesor = "ASE-${(100..999).random()}",
                        area_atencion = "Servicio al Cliente"
                    )
                    client.from("asesores").insert(perfilA)
                } else if (nuevoRol.lowercase() == "admin") {
                    val perfilAdmin = PerfilAdminPostgres(
                        id_usuario = idUsuario,
                        nivel_acceso = "Superusuario"
                    )
                    client.from("perfil_admin").insert(perfilAdmin)
                }
            } catch (eChild: Exception) {
                eChild.printStackTrace()
            }

            Result.success(true)
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
