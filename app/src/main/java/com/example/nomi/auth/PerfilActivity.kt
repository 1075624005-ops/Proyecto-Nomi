package com.example.nomi.auth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nomi.R
import com.example.nomi.data.SupabaseClient
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import com.google.android.material.textfield.TextInputLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var currentUser: UsuarioPostgres? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etNombre = findViewById<EditText>(R.id.etNombrePerfil)
        val spTipoDoc = findViewById<Spinner>(R.id.spTipoDocPerfil)
        val etNumDoc = findViewById<EditText>(R.id.etCedulaPerfil)
        val etTel = findViewById<EditText>(R.id.etTelefonoPerfil)
        val etCorreo = findViewById<EditText>(R.id.etCorreoPerfil)
        
        val tilNombre = findViewById<TextInputLayout>(R.id.tilNombre)
        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilTel = findViewById<TextInputLayout>(R.id.tilTel)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)

        val btnGuardar = findViewById<Button>(R.id.btnGuardarPerfil)
        val btnVolver = findViewById<Button>(R.id.btnVolverPerfil)

        // Configuración Spinner
        val opciones = arrayOf("CC", "NIT", "CE", "PT")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        spTipoDoc.adapter = adapter

        // --- CARGAR DATOS DESDE SUPABASE ---
        val user = SupabaseClient.client.auth.currentSessionOrNull()?.user
        if (user != null) {
            // Lógica para cargar perfil...
        }

        tilNombre.setEndIconOnClickListener { mostrarDialogoEditar("Editar Nombre", etNombre) }
        tilCedula.setEndIconOnClickListener { mostrarDialogoEditar("Editar Documento", etNumDoc) }
        tilTel.setEndIconOnClickListener { mostrarDialogoEditar("Editar Teléfono", etTel) }
        
        btnGuardar.setOnClickListener {
            Toast.makeText(this, "Función en migración", Toast.LENGTH_SHORT).show()
        }

        btnVolver.setOnClickListener { finish() }
    }

    private fun mostrarDialogoEditar(titulo: String, campo: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        val input = EditText(this)
        input.setText(campo.text.toString())
        builder.setView(input)
        builder.setPositiveButton("Hecho") { _, _ -> campo.setText(input.text.toString()) }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
