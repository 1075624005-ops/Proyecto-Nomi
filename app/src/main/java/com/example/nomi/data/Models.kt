package com.example.nomi.data

import kotlinx.serialization.Serializable

// --- MODELOS EXACTOS DE TABLAS DE PERFIL EN SUPABASE ---

@Serializable
data class PerfilCliente(
    val id_usuario: String? = null,
    val Nombre: String,
    val Apellido: String? = "",
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String,
    val direccion: String? = null
)

@Serializable
data class PerfilMensajero(
    val id_usuario: String? = null,
    val Nombre: String,
    val Apellido: String? = "",
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String,
    val direccion: String? = null,
    val licencia_conduccion: String? = null,
    val tipo_licencia: String? = "A2",
    val zona_asignada: String? = "Bogotá D.C.",
    val disponible: Boolean? = true
)

@Serializable
data class PerfilAsesor(
    val id_usuario: String? = null,
    val Nombre: String,
    val Apellido: String? = "",
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String,
    val direccion: String? = null,
    val codigo_asesor: String? = null,
    val area_atencion: String? = "Servicio al Cliente"
)

@Serializable
data class PerfilAdmin(
    val id_usuario: String? = null,
    val Nombre: String,
    val Apellido: String? = "",
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String,
    val direccion: String? = null,
    val nivel_acceso: String? = "Superusuario"
)

// --- RESULTADO DE AUTENTICACIÓN SEGÚN EL ROL ENCONTRADO ---
sealed class UsuarioResultado {
    data class Admin(val perfil: PerfilAdmin) : UsuarioResultado()
    data class Asesor(val perfil: PerfilAsesor) : UsuarioResultado()
    data class Mensajero(val perfil: PerfilMensajero) : UsuarioResultado()
    data class Cliente(val perfil: PerfilCliente) : UsuarioResultado()
}

// --- ESTRUCTURA DE PEDIDOS Y PLANILLAS ---

@Serializable
data class PlanillaPostgres(
    val id_planilla: String,
    val cd_origen: String,
    val cd_destino: String,
    val id_mensajero: String? = null,
    val vehiculo: String? = null,
    val estado: String = "Creada",
    val fecha_creacion: String? = null
)

@Serializable
data class PedidoPostgres(
    val num_guia: String,                       // Primary Key (ej. N050001)
    val id_cliente: String? = null,             // Foreign Key -> perfil_cliente.id_usuario
    val id_mensajero: String? = null,           // Foreign Key -> perfil_mensajero.id_usuario
    val id_planilla: String? = null,            // Foreign Key -> planillas.id_planilla
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
    val estado: Int = 1,                        // 1=Solicitado, 2=En Planilla/En Ruta, 3=Entregado
    val costo: Double? = 0.0
)

@Serializable
data class PQRSPostgres(
    val id: Int? = null,
    val correo_usuario: String,
    val nombre_usuario: String,
    val asunto: String,
    val descripcion: String,
    val estado: String = "Pendiente",
    val respuesta: String? = null
)
