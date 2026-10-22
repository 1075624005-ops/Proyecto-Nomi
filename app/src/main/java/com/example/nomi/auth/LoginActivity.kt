package com.example.nomi.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioResultado
import com.example.nomi.admin.AdminActivity
import com.example.nomi.admin.AsesorActivity
import com.example.nomi.main.MessengerHomeActivity
import com.example.nomi.main.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

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
                            "Puedes ejercer tus derechos escribiéndonos a nomi@nomi.com."
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

            pbLogin.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                val resultado = repo.login(correo, password)
                
                pbLogin.visibility = View.GONE
                btnLogin.isEnabled = true

                resultado.onSuccess { resultadoUsuario ->
                    when (resultadoUsuario) {
                        is UsuarioResultado.Admin -> {
                            val p = resultadoUsuario.perfil
                            Toast.makeText(this@LoginActivity, "👨‍💻 Acceso Administrador", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                            intent.putExtra("nombre", p.Nombre)
                            intent.putExtra("correo", p.Correo_electronico)
                            intent.putExtra("rol", "admin")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is UsuarioResultado.Asesor -> {
                            val p = resultadoUsuario.perfil
                            Toast.makeText(this@LoginActivity, "🎧 Acceso Asesor Operativo", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, AsesorActivity::class.java)
                            intent.putExtra("nombre", p.Nombre)
                            intent.putExtra("correo", p.Correo_electronico)
                            intent.putExtra("rol", "asesor")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is UsuarioResultado.Mensajero -> {
                            val p = resultadoUsuario.perfil
                            Toast.makeText(this@LoginActivity, "🛵 Acceso Mensajero", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MessengerHomeActivity::class.java)
                            intent.putExtra("nombre", p.Nombre)
                            intent.putExtra("correo", p.Correo_electronico)
                            intent.putExtra("rol", "mensajero")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is UsuarioResultado.Cliente -> {
                            val p = resultadoUsuario.perfil
                            Toast.makeText(this@LoginActivity, "👤 Bienvenido, ${p.Nombre}", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.putExtra("nombre", p.Nombre)
                            intent.putExtra("correo", p.Correo_electronico)
                            intent.putExtra("rol", "cliente")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                    finish()
                }.onFailure { error ->
                    val msg = error.message ?: ""
                    val errorLegible = when {
                        msg.contains("Invalid login credentials") -> "Contraseña o correo incorrectos"
                        msg.contains("Email not confirmed") -> "Debes confirmar tu correo en Supabase"
                        msg.contains("no se encontró su perfil") -> "Usuario autenticado pero no se encontró perfil en las tablas"
                        else -> "Error de conexión: $msg"
                    }
                    Toast.makeText(this@LoginActivity, errorLegible, Toast.LENGTH_LONG).show()
                }
            }
        }

        btnCrearUsuario.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvOlvido.setOnClickListener {
            Toast.makeText(this, "Función en migración", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
