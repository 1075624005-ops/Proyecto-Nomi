package com.example.nomi.data

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioPostgres(
    val id: String, // UUID generado por Supabase
    val nombre: String,
    val correo: String,
    val tipo_doc: String? = null,
    val num_doc: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val rol: String = "cliente",
    val placa: String? = null,
    val area: String? = null,
    val habeas_data_aceptado: Boolean = false,
    val habeas_data_fecha: String? = null,
    val habeas_data_version: String? = "v1.0"
)

// --- ESTRUCTURA ESPECIALIZADA OPCIÓN B (TABLAS PADRE E HIJAS PARA MODELO ER Y ACADÉMICO) ---

@Serializable
data class ClienteDetallePostgres(
    val id: Int? = null,
    val usuario_id: String,
    val habeas_data_aceptado: Boolean = true,
    val habeas_data_fecha: String? = null,
    val habeas_data_version: String? = "v1.0"
)

@Serializable
data class MensajeroDetallePostgres(
    val id: Int? = null,
    val usuario_id: String,
    val placa: String? = null,
    val vehiculo: String? = "Moto",
    val area_cobertura: String? = null
)

@Serializable
data class AsesorDetallePostgres(
    val id: Int? = null,
    val usuario_id: String,
    val codigo_asesor: String? = null,
    val area_atencion: String? = "Servicio al Cliente"
)

// --- TABLA PEDIDOS COMPLETA (VINCULADA CON CLIENTE Y MENSAJERO) ---

@Serializable
data class PedidoPostgres(
    val num_guia: String,                       // Primary Key (ej. N050001)
    val id_cliente: String? = null,             // Foreign Key -> usuarios.id
    val id_mensajero: String? = null,           // Foreign Key -> usuarios.id
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
    val estado_pago: String? = "Pendiente",     // 'Pendiente' o 'Pagado'
    val estado: Int = 1,                        // 1=Solicitado, 2=En Ruta, 3=Entregado
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
