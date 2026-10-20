package com.example.nomi.auth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseClient
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import com.google.android.material.textfield.TextInputLayout
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

        val btnGuardar = findViewById<Button>(R.id.btnGuardarPerfil)
        val btnRestablecer = findViewById<Button>(R.id.btnRestablecerPass)
        val btnVolver = findViewById<Button>(R.id.btnVolverPerfil)

        // Configuración Spinner
        val opciones = arrayOf("CC", "NIT", "CE", "PAS")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        spTipoDoc.adapter = adapter

        // Cargar correo e información inicial
        val userSession = SupabaseClient.client.auth.currentUserOrNull()
        val email = intent.getStringExtra("correo") ?: userSession?.email ?: "usuario@nomi.com"
        val nombre = intent.getStringExtra("nombre") ?: ""

        etCorreo.setText(email)
        if (nombre.isNotEmpty()) etNombre.setText(nombre)

        // Cargar perfil en vivo desde Supabase
        lifecycleScope.launch {
            repo.obtenerTodosLosUsuarios().onSuccess { usuarios ->
                val miPerfil = usuarios.find { it.correo.equals(email, ignoreCase = true) }
                if (miPerfil != null) {
                    currentUser = miPerfil
                    etNombre.setText(miPerfil.nombre)
                    etNumDoc.setText(miPerfil.num_doc ?: "")
                    etTel.setText(miPerfil.telefono)
                }
            }
        }

        tilNombre.setEndIconOnClickListener { mostrarDialogoEditar("Editar Nombre", etNombre) }
        tilCedula.setEndIconOnClickListener { mostrarDialogoEditar("Editar Documento", etNumDoc) }
        tilTel.setEndIconOnClickListener { mostrarDialogoEditar("Editar Teléfono", etTel) }

        // Botón Restablecer / Cambiar Contraseña vía Correo
        btnRestablecer.setOnClickListener {
            val userEmail = etCorreo.text.toString().trim()
            if (userEmail.isEmpty()) {
                Toast.makeText(this, "⚠️ Correo no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRestablecer.isEnabled = false
            btnRestablecer.text = "Enviando verificación..."

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.resetPasswordForEmail(userEmail)
                    Toast.makeText(this@PerfilActivity, "✅ Enlace de restablecimiento enviado a $userEmail. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@PerfilActivity, "❌ Error al enviar correo: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    btnRestablecer.isEnabled = true
                    btnRestablecer.text = "🔒 CAMBIAR CONTRASEÑA (VERIFICAR AL CORREO)"
                }
            }
        }

        btnGuardar.setOnClickListener {
            Toast.makeText(this, "✅ Datos de perfil actualizados", Toast.LENGTH_SHORT).show()
            finish()
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
