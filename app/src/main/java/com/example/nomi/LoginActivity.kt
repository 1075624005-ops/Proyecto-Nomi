package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// IMPORTACIONES DE FIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etCorreo        = findViewById<EditText>(R.id.etUsuario)
        val etPassword      = findViewById<EditText>(R.id.etPassword)
        val btnLogin        = findViewById<Button>(R.id.btnLogin)
        val btnCrearUsuario = findViewById<Button>(R.id.btnCrearUsuario)
        val tvOlvido        = findViewById<TextView>(R.id.tvOlvido)
        val prefs = getSharedPreferences("nomi_prefs", MODE_PRIVATE)
        val yaVioAviso = prefs.getBoolean("aviso_datos_visto", false)

        if (!yaVioAviso) {
            AlertDialog.Builder(this)
                .setTitle("🔒 Aviso de Privacidad")
                .setMessage(
                    "Bienvenido a Nomi.\n\n" +
                            "Recolectamos datos como nombre, correo, documento y dirección " +
                            "para gestionar tu cuenta y pedidos, conforme a la " +
                            "Ley 1581 de 2012 (Habeas Data).\n\n" +
                            "Al usar la app aceptas nuestra Política de Datos. " +
                            "Puedes ejercer tus derechos escribiéndonos a {nomisas@nomi.com]."
                )
                .setCancelable(false)
                .setPositiveButton("Entendido y Acepto") { _, _ ->
                    prefs.edit().putBoolean("aviso_datos_visto", true).apply()
                }
                .setNegativeButton("Salir") { _, _ ->
                    finish()
                }
                .show()
        }

        btnLogin.setOnClickListener {
            val correo   = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ── LOGIN USUARIOS (FIREBASE) ────────────────
            auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user?.uid
                    if (uid != null) {
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { documento ->
                                val nombre = documento.getString("nombre") ?: "Usuario"
                                val rol = documento.getString("rol") ?: "cliente"

                                // Si el rol es admin, vamos al panel de admin
                                if (rol == "admin") {
                                    Toast.makeText(this, "👨‍💻 Acceso concedido al Jefe", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, AdminActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                } else {
                                    // Si no, al Home normal
                                    val intent = Intent(this, HomeActivity::class.java)
                                    intent.putExtra("nombre", nombre)
                                    intent.putExtra("correo", correo)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                                finish()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
        }

        btnCrearUsuario.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvOlvido.setOnClickListener {
            mostrarDialogoRecuperacion()
        }
    }

    private fun mostrarDialogoRecuperacion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar contraseña")
        builder.setMessage("Ingresa tu correo para enviarte el enlace:")
        val input = EditText(this)
        input.hint = "correo@ejemplo.com"
        builder.setView(input)
        builder.setPositiveButton("Enviar") { _, _ ->
            val mail = input.text.toString().trim()
            if (mail.isNotEmpty()) {
                auth.sendPasswordResetEmail(mail).addOnSuccessListener {
                    Toast.makeText(this, "✅ Revisa tu correo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
