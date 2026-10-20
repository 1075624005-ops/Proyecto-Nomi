package com.example.nomi.data

import kotlinx.serialization.Serializable

// --- CLASE DE CONTENEDOR Y SESIÓN COMPATIBLE ---
@Serializable
data class UsuarioPostgres(
    val id_usuario: String = "",
    val id: String = "",
    val correo: String = "",
    val nombre: String = "",
    val apellido: String? = null,
    val telefono: String = "",
    val tipo_doc: String? = "CC",
    val num_doc: String? = null,
    val direccion: String? = null,
    val rol: String = "cliente",
    val placa: String? = null,
    val area: String? = null,
    val habeas_data_aceptado: Boolean = false,
    val habeas_data_fecha: String? = null,
    val habeas_data_version: String? = "v1.0"
)

@Serializable
data class DatosUsuarioLogin(
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "cliente"
)

// --- TABLAS HIJAS ESPECIALIZADAS EN SUPABASE (ESTRUCTURA UNIFICADA BASE + CAMPOS ESPECÍFICOS) ---

@Serializable
data class PerfilClientePostgres(
    val id_usuario: String = "",
    val Nombre: String? = null,
    val Apellido: String? = null,
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String? = null,
    val direccion: String? = null
)

@Serializable
data class PerfilMensajeroPostgres(
    val id_usuario: String = "",
    val Nombre: String? = null,
    val Apellido: String? = null,
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String? = null,
    val direccion: String? = null,
    val licencia_conduccion: String = "",
    val tipo_licencia: String = "A2",
    val zona_asignada: String = "Bogotá D.C.",
    val disponible: Boolean = true
)

@Serializable
data class PerfilAsesorPostgres(
    val id_usuario: String = "",
    val Nombre: String? = null,
    val Apellido: String? = null,
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String? = null,
    val direccion: String? = null,
    val codigo_asesor: String = "ASE-001",
    val area_atencion: String = "Servicio al Cliente"
)

@Serializable
data class PerfilAdminPostgres(
    val id_usuario: String = "",
    val Nombre: String? = null,
    val Apellido: String? = null,
    val tipo_doc: String? = "CC",
    val numero_doc: String? = null,
    val Codigo_pais: String? = "+57",
    val Telefono: Long? = null,
    val Correo_electronico: String? = null,
    val direccion: String? = null,
    val nivel_acceso: String = "Superusuario"
)

@Serializable
data class VehiculoPostgres(
    val id_vehiculo: String = "",
    val placa: String = "",
    val tipo_vehiculo: String = "Moto",
    val marca: String? = null,
    val modelo: String? = null,
    val vencimiento_soat: String? = null,
    val vencimiento_tecnomecanica: String? = null,
    val id_mensajero_asignado: String? = null
)

@Serializable
data class HabeasDataConsentPostgres(
    val id_usuario: String = "",
    val aceptado: Boolean = true,
    val version_politica: String = "v1.0 - Ley 1581 de 2012"
)

// --- TABLAS DE PEDIDOS Y PQRS ---

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
