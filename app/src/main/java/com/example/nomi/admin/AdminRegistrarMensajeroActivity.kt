package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilMensajero
import com.example.nomi.data.SupabaseRepository
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

        val etNombre = findViewById<EditText>(R.id.etNombreRegM)
        val etCedula = findViewById<EditText>(R.id.etCedulaRegM)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegM)
        val etPlaca  = findViewById<EditText>(R.id.etPlacaRegM)
        val etArea   = findViewById<EditText>(R.id.etAreaRegM)
        val etPass   = findViewById<EditText>(R.id.etPassRegM)
        val btnReg   = findViewById<Button>(R.id.btnFinalizarRegM)
        val btnCan   = findViewById<Button>(R.id.btnCancelarRegM)

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nombreCompleto = etNombre.text.toString().trim()
            val ced = etCedula.text.toString().trim()
            val cor = etCorreo.text.toString().trim()
            val pla = etPlaca.text.toString().trim()
            val are = etArea.text.toString().trim()
            val pas = etPass.text.toString().trim()

            if (nombreCompleto.isEmpty() || ced.isEmpty() || cor.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Campos obligatorios faltantes (Nombre, Cédula, Correo y Contraseña)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val partesNombre = nombreCompleto.split(" ", limit = 2)
            val nombre = partesNombre.getOrNull(0) ?: nombreCompleto
            val apellido = partesNombre.getOrNull(1) ?: "N/A"

            val tieneLicencia = if (pla.isNotEmpty()) "Sí" else "No"
            val tipoLicencia = if (pla.isNotEmpty()) "A2" else "N/A"

            lifecycleScope.launch {
                val nuevoM = PerfilMensajero(
                    Nombre = nombre,
                    Apellido = apellido,
                    tipo_doc = "CC",
                    numero_doc = ced,
                    Codigo_pais = "+57",
                    Telefono = 3000000000L,
                    Correo_electronico = cor,
                    direccion = "No especificada",
                    licencia_conduccion = tieneLicencia,
                    tipo_licencia = tipoLicencia,
                    zona_asignada = if (are.isNotEmpty()) are else "Bogotá D.C.",
                    disponible = true
                )
                
                repo.registrarMensajero(cor, pas, nuevoM).onSuccess {
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "✅ Mensajero registrado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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
