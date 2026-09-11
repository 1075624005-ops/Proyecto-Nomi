package com.example.nomi

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {
    private val client = SupabaseClient.client

    // --- AUTENTICACIÓN Y PERFIL ---

    /**
     * Inicia sesión en Supabase y recupera el perfil técnico del usuario.
     */
    suspend fun login(email: String, pass: String): Result<UsuarioPostgres> = withContext(Dispatchers.IO) {
        try {
            // 1. Intentar entrar con correo y clave
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            
            // 2. Si entra, buscamos sus datos (rol, nombre, etc.) en la tabla 'usuarios'
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

    /**
     * Registra un nuevo usuario en la nube y guarda su ficha técnica en PostgreSQL.
     */
    suspend fun registrarUsuario(email: String, pass: String, datos: UsuarioPostgres): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // 1. Crear el usuario en el sistema de seguridad
            val session = client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            
            // 2. Capturar el ID único que generó la nube
            // En Supabase-kt 3.x, signUpWith devuelve UserInfo que tiene el id directamente
            val idAuth = client.auth.retrieveUserForCurrentSession().id
            
            // 3. Crear la ficha técnica en nuestra tabla de PostgreSQL
            val perfilParaPostgres = datos.copy(id = idAuth)
            client.from("usuarios").insert(perfilParaPostgres)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GESTIÓN DE PEDIDOS ---

    suspend fun obtenerPedidosAdmin(): Result<List<PedidoPostgres>> = withContext(Dispatchers.IO) {
        try {
            val lista = client.from("pedidos").select().decodeList<PedidoPostgres>()
            Result.success(lista)
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
}
