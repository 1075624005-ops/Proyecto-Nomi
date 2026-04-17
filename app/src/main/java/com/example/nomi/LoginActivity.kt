package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)

        val etCorreo        = findViewById<EditText>(R.id.etUsuario)
        val etPassword      = findViewById<EditText>(R.id.etPassword)
        val btnLogin        = findViewById<Button>(R.id.btnLogin)
        val btnCrearUsuario = findViewById<Button>(R.id.btnCrearUsuario)
        val tvOlvido        = findViewById<TextView>(R.id.tvOlvido)

        btnLogin.setOnClickListener {
            val correo   = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultado = db.validarLogin(correo, password)
            if (resultado.startsWith("ok")) {
                val datos = resultado.split("|")
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("nombre", datos[1])
                intent.putExtra("correo", datos[2])
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        btnCrearUsuario.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- FLUJO DE RECUPERAR CONTRASEÑA ---
        tvOlvido.setOnClickListener {
            mostrarDialogoPedirCorreo()
        }
    }

    private fun mostrarDialogoPedirCorreo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar Contraseña")
        builder.setMessage("Ingrese su correo electrónico registrado:")

        val input = EditText(this)
        input.hint = "correo@ejemplo.com"
        builder.setView(input)

        builder.setPositiveButton("Siguiente") { _, _ ->
            val correo = input.text.toString().trim()
            if (db.existeCorreo(correo)) {
                mostrarDialogoVerificacion(correo)
            } else {
                Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoVerificacion(correo: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verificación")
        builder.setMessage("Se envió un código a su correo. Ingrese '1234' para continuar:")

        val input = EditText(this)
        input.hint = "Código de 4 dígitos"
        builder.setView(input)

        builder.setPositiveButton("Verificar") { _, _ ->
            if (input.text.toString() == "1234") {
                mostrarDialogoNuevaClave(correo)
            } else {
                Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    private fun mostrarDialogoNuevaClave(correo: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Contraseña")
        builder.setMessage("Escriba su nueva contraseña:")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Cambiar") { _, _ ->
            val nuevaClave = input.text.toString().trim()
            if (nuevaClave.isNotEmpty()) {
                if (db.cambiarPassword(correo, nuevaClave)) {
                    Toast.makeText(this, "✅ Contraseña actualizada. Ya puede iniciar sesión.", Toast.LENGTH_LONG).show()
                }
            }
        }
        builder.show()
    }
}