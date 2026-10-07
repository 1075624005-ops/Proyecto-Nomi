package com.example.nomi.data

import kotlinx.serialization.Serializable

// --- TABLA PADRE: USUARIOS ---
@Serializable
data class UsuarioPostgres(
    val id_usuario: String = "", // UUID de Supabase Auth
    val id: String = "",         // Alias de compatibilidad
    val correo: String = "",
    val nombre: String = "",
    val apellido: String? = null,
    val telefono: String = "",
    val tipo_doc: String? = "CC",
    val num_doc: String? = null,
    val direccion: String? = null,
    val rol: String = "cliente",
    val placa: String? = null,
    val area: String? = null
)

// --- TABLAS HIJAS ESPECIALIZADAS ---

@Serializable
data class PerfilClientePostgres(
    val id_usuario: String,
    val tipo_doc: String = "CC",
    val numero_doc: String,
    val direccion: String? = null
)

@Serializable
data class PerfilMensajeroPostgres(
    val id_usuario: String,
    val placa_vehiculo: String,
    val tipo_vehiculo: String = "Moto",
    val licencia_conduccion: String = "",
    val vencimiento_soat: String = "",
    val vencimiento_tecnomecanica: String = "",
    val zona_asignada: String = "Bogotá D.C.",
    val disponible: Boolean = true
)

@Serializable
data class PerfilAsesorPostgres(
    val id_usuario: String,
    val codigo_asesor: String = "ASE-001",
    val area_atencion: String = "Servicio al Cliente"
)

// --- TABLA ENVIOS / PEDIDOS COMPLETA ---

@Serializable
data class PedidoPostgres(
    val num_guia: String = "",
    val guia_oficial: String = "",
    val id_cliente: String? = null,
    val id_mensajero: String? = null,
    val rem_nombre: String? = null,
    val rem_tel: String? = null,
    val rem_dir: String? = null,
    val dest_nombre: String? = null,
    val dest_tel: String? = null,
    val dest_dir: String? = null,
    val dest_localidad: String? = null,
    val descripcion: String? = null,
    val dimensiones: String? = null,
    val peso_kg: String? = null,
    val tipo_servicio: String? = null,
    val modalidad_pago: String? = "contraentrega",
    val estado_pago: String? = "Pendiente",
    val estado: Int = 1,
    val costo: Double? = 0.0
)

@Serializable
data class PQRSPostgres(
    val id: Int? = null,
    val radicado: String? = null,
    val correo_usuario: String = "",
    val nombre_usuario: String = "",
    val asunto: String = "",
    val descripcion: String = "",
    val estado: String = "Pendiente",
    val respuesta: String? = null
)
