package com.example.nomi.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    // Aquí pondremos los datos que encontraste en el panel de Supabase
    private const val SUPABASE_URL = "https://aiivbmblqyeukcsuknye.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_8lND4L1nKGxtroFX_O88bg__BQ-mrlI"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)     // Sirve para el Login
        install(Postgrest) // Sirve para las tablas de PostgreSQL
    }
}
