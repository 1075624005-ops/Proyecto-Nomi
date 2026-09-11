package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // NUEVO: Instancia del repositorio de PostgreSQL
    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etCorreo        = findViewById<EditText>(R.id.etUsuario)
        val etPassword      = findViewById<EditText>(R.id.etPassword)
        val btnLogin        = findViewById<Button>(R.id.btnLogin)
        val pbLogin         = findViewById<ProgressBar>(R.id.pbLogin)
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

            // MOSTRAR CARGA Y DESACTIVAR BOTÓN
            pbLogin.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            // 🚀 NUEVO: LOGIN CON POSTGRESQL (SUPABASE)
            lifecycleScope.launch {
                val resultado = repo.login(correo, password)
                
                pbLogin.visibility = View.GONE
                btnLogin.isEnabled = true

                resultado.onSuccess { usuario ->
                    val rol = usuario.rol.trim()
                    
                    if (rol == "admin") {
                        Toast.makeText(this@LoginActivity, "👨‍💻 Acceso Administrador", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else if (rol == "mensajero") {
                        Toast.makeText(this@LoginActivity, "📦 Acceso Mensajero", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MessengerHomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.putExtra("nombre", usuario.nombre)
                        intent.putExtra("correo", usuario.correo)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    finish()
                }.onFailure { error ->
                    Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnCrearUsuario.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvOlvido.setOnClickListener {
            // TODO: Migrar recuperación de contraseña a Supabase
            Toast.makeText(this, "Función en migración", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
