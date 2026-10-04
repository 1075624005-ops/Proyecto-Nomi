package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminRegistrarMensajeroActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_mensajero)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etNombre   = findViewById<EditText>(R.id.etNombreRegM)
        val etCedula   = findViewById<EditText>(R.id.etCedulaRegM)
        val etTelefono = findViewById<EditText>(R.id.etTelefonoRegM)
        val etCorreo   = findViewById<EditText>(R.id.etCorreoRegM)
        val etPlaca    = findViewById<EditText>(R.id.etPlacaRegM)
        val etArea     = findViewById<EditText>(R.id.etAreaRegM)
        val etPass     = findViewById<EditText>(R.id.etPassRegM)
        val btnReg     = findViewById<Button>(R.id.btnFinalizarRegM)
        val btnCan     = findViewById<Button>(R.id.btnCancelarRegM)

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nom = etNombre.text.toString().trim()
            val ced = etCedula.text.toString().trim()
            val tel = etTelefono.text.toString().trim()
            val cor = etCorreo.text.toString().trim()
            val pla = etPlaca.text.toString().trim()
            val are = etArea.text.toString().trim()
            val pas = etPass.text.toString().trim()

            if (nom.isEmpty() || ced.isEmpty() || tel.isEmpty() || cor.isEmpty() || pla.isEmpty() || are.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Todos los campos son obligatorios: Nombre, Cédula, Teléfono, Correo, Placa, Zona y Contraseña", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnReg.isEnabled = false
            btnReg.text = "Registrando..."

            lifecycleScope.launch {
                val nuevoM = UsuarioPostgres(
                    id = "",
                    nombre = nom,
                    correo = cor,
                    tipo_doc = "CC",
                    num_doc = ced,
                    telefono = tel,
                    direccion = "Base Operativa Bogotá",
                    placa = pla,
                    area = are,
                    rol = "mensajero"
                )
                
                repo.registrarUsuario(cor, pas, nuevoM).onSuccess {
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "✅ Mensajero registrado con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    btnReg.isEnabled = true
                    btnReg.text = "REGISTRAR EN PLANILLA"
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "❌ Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
