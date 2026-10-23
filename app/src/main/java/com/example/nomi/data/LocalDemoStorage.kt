package com.example.nomi.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object LocalDemoStorage {
    private const val PREFS_NAME = "nomi_local_demo_prefs"
    private lateinit var appContext: Context

    // Correos mock antiguos que deben filtrarse siempre
    private val CORREOS_MOCK_PROHIBIDOS = emptySet<String>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun limpiarClientes() {
        getPrefs()?.edit()?.remove("lista_clientes")?.apply()
    }

    private fun getPrefs(): android.content.SharedPreferences? {
        if (!::appContext.isInitialized) return null
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- PQRS ---
    fun guardarPQR(pqr: PQRSPostgres) {
        val prefs = getPrefs() ?: return
        val lista = obtenerPQRS().toMutableList()
        val nuevoId = (lista.maxOfOrNull { it.id ?: 0 } ?: 1000) + 1
        val pqrConId = pqr.copy(id = nuevoId, estado = "Pendiente")
        lista.add(0, pqrConId)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_pqrs", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerPQRS(correo: String = ""): List<PQRSPostgres> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_pqrs", null) ?: return emptyList()
        val lista: List<PQRSPostgres> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
        val limpia = lista.filter { !CORREOS_MOCK_PROHIBIDOS.contains(it.correo_usuario.lowercase()) }
        if (correo.isEmpty()) return limpia
        val filtrada = limpia.filter { it.correo_usuario.equals(correo, ignoreCase = true) }
        return filtrada.ifEmpty { limpia }
    }

    fun responderPQR(idRadicado: Int, respuestaTexto: String, estadoNuevo: String = "Resuelto") {
        val prefs = getPrefs() ?: return
        val json = prefs.getString("lista_pqrs", null) ?: return
        val lista: MutableList<PQRSPostgres> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString<List<PQRSPostgres>>(json).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
        val index = lista.indexOfFirst { it.id == idRadicado }
        if (index != -1) {
            val actual = lista[index]
            val actualizada = actual.copy(respuesta = respuestaTexto, estado = estadoNuevo)
            lista[index] = actualizada
            try {
                val jsonNuevo = Json { ignoreUnknownKeys = true }.encodeToString(lista)
                prefs.edit().putString("lista_pqrs", jsonNuevo).apply()
            } catch (_: Exception) {}
        }
    }

    // --- PEDIDOS ---
    fun guardarPedido(pedido: PedidoPostgres) {
        val prefs = getPrefs() ?: return
        val lista = obtenerPedidos().toMutableList()
        lista.removeIf { it.num_guia == pedido.num_guia }
        lista.add(0, pedido)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_pedidos", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerPedidos(): List<PedidoPostgres> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_pedidos", null) ?: return emptyList()
        val lista: List<PedidoPostgres> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
        // Filtrar guías de prueba antiguas para limpiar la vista
        val guiasPruebaAntiguas = setOf("N050001", "N010001")
        return lista.filter { !guiasPruebaAntiguas.contains(it.num_guia) }
    }

    fun limpiarPedidos() {
        getPrefs()?.edit()?.remove("lista_pedidos")?.apply()
        getPrefs()?.edit()?.remove("lista_planillas")?.apply()
    }

    // --- PLANILLAS ---
    fun guardarPlanilla(planilla: PlanillaPostgres) {
        val prefs = getPrefs() ?: return
        val lista = obtenerPlanillas().toMutableList()
        lista.removeIf { id_planilla -> id_planilla.id_planilla == planilla.id_planilla }
        lista.add(0, planilla)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_planillas", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerPlanillas(): List<PlanillaPostgres> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_planillas", null) ?: return emptyList()
        return try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    // --- CLIENTES ---
    fun guardarCliente(cliente: PerfilCliente) {
        val prefs = getPrefs() ?: return
        val lista = obtenerClientes().toMutableList()
        val existente = lista.find { it.Correo_electronico.equals(cliente.Correo_electronico, true) }

        val clienteAInsertar = if (existente != null) {
            cliente.copy(
                id_usuario = cliente.id_usuario ?: existente.id_usuario,
                Nombre = if (cliente.Nombre.isNotEmpty() && cliente.Nombre != "Cliente") cliente.Nombre else existente.Nombre,
                Apellido = if (!cliente.Apellido.isNullOrEmpty()) cliente.Apellido else existente.Apellido,
                tipo_doc = if (!cliente.tipo_doc.isNullOrEmpty()) cliente.tipo_doc else existente.tipo_doc,
                numero_doc = if (!cliente.numero_doc.isNullOrEmpty()) cliente.numero_doc else existente.numero_doc,
                Codigo_pais = if (!cliente.Codigo_pais.isNullOrEmpty()) cliente.Codigo_pais else existente.Codigo_pais,
                Telefono = cliente.Telefono ?: existente.Telefono,
                direccion = if (!cliente.direccion.isNullOrEmpty()) cliente.direccion else existente.direccion
            )
        } else {
            cliente
        }

        lista.removeIf { it.Correo_electronico.equals(cliente.Correo_electronico, true) }
        lista.add(0, clienteAInsertar)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_clientes", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerClientes(): List<PerfilCliente> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_clientes", null) ?: return emptyList()
        val lista: List<PerfilCliente> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
        return lista.filter { !CORREOS_MOCK_PROHIBIDOS.contains(it.Correo_electronico.lowercase()) }
    }

    // --- MENSAJEROS ---
    fun guardarMensajero(mensajero: PerfilMensajero) {
        val prefs = getPrefs() ?: return
        val lista = obtenerMensajeros().toMutableList()
        val existente = lista.find { it.Correo_electronico.equals(mensajero.Correo_electronico, true) }

        val mensajeroAInsertar = if (existente != null) {
            mensajero.copy(
                id_usuario = mensajero.id_usuario ?: existente.id_usuario,
                Nombre = if (mensajero.Nombre.isNotEmpty() && mensajero.Nombre != "Mensajero") mensajero.Nombre else existente.Nombre,
                Apellido = if (!mensajero.Apellido.isNullOrEmpty()) mensajero.Apellido else existente.Apellido,
                tipo_doc = if (!mensajero.tipo_doc.isNullOrEmpty()) mensajero.tipo_doc else existente.tipo_doc,
                numero_doc = if (!mensajero.numero_doc.isNullOrEmpty()) mensajero.numero_doc else existente.numero_doc,
                Codigo_pais = if (!mensajero.Codigo_pais.isNullOrEmpty()) mensajero.Codigo_pais else existente.Codigo_pais,
                Telefono = mensajero.Telefono ?: existente.Telefono,
                direccion = if (!mensajero.direccion.isNullOrEmpty()) mensajero.direccion else existente.direccion,
                licencia_conduccion = if (!mensajero.licencia_conduccion.isNullOrEmpty()) mensajero.licencia_conduccion else existente.licencia_conduccion,
                tipo_licencia = if (!mensajero.tipo_licencia.isNullOrEmpty()) mensajero.tipo_licencia else existente.tipo_licencia,
                zona_asignada = if (!mensajero.zona_asignada.isNullOrEmpty()) mensajero.zona_asignada else existente.zona_asignada
            )
        } else {
            mensajero
        }

        lista.removeIf { it.Correo_electronico.equals(mensajero.Correo_electronico, true) }
        lista.add(0, mensajeroAInsertar)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_mensajeros", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerMensajeros(): List<PerfilMensajero> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_mensajeros", null) ?: return emptyList()
        val lista: List<PerfilMensajero> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
        return lista.filter { !CORREOS_MOCK_PROHIBIDOS.contains(it.Correo_electronico.lowercase()) }
    }

    // --- ASESORES ---
    fun guardarAsesor(asesor: PerfilAsesor) {
        val prefs = getPrefs() ?: return
        val lista = obtenerAsesores().toMutableList()
        val existente = lista.find { it.Correo_electronico.equals(asesor.Correo_electronico, true) }

        val asesorAInsertar = if (existente != null) {
            asesor.copy(
                id_usuario = asesor.id_usuario ?: existente.id_usuario,
                Nombre = if (asesor.Nombre.isNotEmpty() && asesor.Nombre != "Asesor") asesor.Nombre else existente.Nombre,
                Apellido = if (!asesor.Apellido.isNullOrEmpty()) asesor.Apellido else existente.Apellido,
                tipo_doc = if (!asesor.tipo_doc.isNullOrEmpty()) asesor.tipo_doc else existente.tipo_doc,
                numero_doc = if (!asesor.numero_doc.isNullOrEmpty()) asesor.numero_doc else existente.numero_doc,
                Codigo_pais = if (!asesor.Codigo_pais.isNullOrEmpty()) asesor.Codigo_pais else existente.Codigo_pais,
                Telefono = asesor.Telefono ?: existente.Telefono,
                direccion = if (!asesor.direccion.isNullOrEmpty()) asesor.direccion else existente.direccion,
                codigo_asesor = if (!asesor.codigo_asesor.isNullOrEmpty()) asesor.codigo_asesor else existente.codigo_asesor,
                area_atencion = if (!asesor.area_atencion.isNullOrEmpty()) asesor.area_atencion else existente.area_atencion
            )
        } else {
            asesor
        }

        lista.removeIf { it.Correo_electronico.equals(asesor.Correo_electronico, true) }
        lista.add(0, asesorAInsertar)
        try {
            val json = Json { ignoreUnknownKeys = true }.encodeToString(lista)
            prefs.edit().putString("lista_asesores", json).apply()
        } catch (_: Exception) {}
    }

    fun obtenerAsesores(): List<PerfilAsesor> {
        val prefs = getPrefs() ?: return emptyList()
        val json = prefs.getString("lista_asesores", null) ?: return emptyList()
        val lista: List<PerfilAsesor> = try {
            Json { ignoreUnknownKeys = true }.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
        return lista.filter { !CORREOS_MOCK_PROHIBIDOS.contains(it.Correo_electronico.lowercase()) }
    }
}
