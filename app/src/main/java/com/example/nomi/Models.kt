package com.example.nomi

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
    val area: String? = null
)

@Serializable
data class PedidoPostgres(
    val num_guia: String,
    val id_mensajero: String? = null,
    val estado: Int = 1,
    val costo: Double? = null
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
