package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilAsesor
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminRegistrarAsesorActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_asesor)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etNombre  = findViewById<EditText>(R.id.etNombreRegA)
        val etCedula  = findViewById<EditText>(R.id.etCedulaRegA)
        val etTel     = findViewById<EditText>(R.id.etTelefonoRegA)
        val etCorreo  = findViewById<EditText>(R.id.etCorreoRegA)
        val etDir     = findViewById<EditText>(R.id.etDireccionRegA)
        val etCodigo  = findViewById<EditText>(R.id.etCodigoAsesorRegA)
        val etArea    = findViewById<EditText>(R.id.etAreaAtencionRegA)
        val etPass    = findViewById<EditText>(R.id.etPassRegA)

        val btnReg = findViewById<Button>(R.id.btnFinalizarRegA)
        val btnCan = findViewById<Button>(R.id.btnCancelarRegA)

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nombreCompleto = etNombre.text.toString().trim()
            val ced = etCedula.text.toString().trim()
            val telStr = etTel.text.toString().trim()
            val cor = etCorreo.text.toString().trim()
            val dir = etDir.text.toString().trim()
            val cod = etCodigo.text.toString().trim()
            val are = etArea.text.toString().trim()
            val pas = etPass.text.toString().trim()

            if (nombreCompleto.isEmpty() || ced.isEmpty() || cor.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Complete los campos obligatorios (Nombre, Cédula, Correo y Contraseña)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val partesNombre = nombreCompleto.split(" ", limit = 2)
            val nombre = partesNombre.getOrNull(0) ?: nombreCompleto
            val apellido = partesNombre.getOrNull(1) ?: "N/A"
            val telLong = telStr.toLongOrNull() ?: 0L

            lifecycleScope.launch {
                val nuevoAsesor = PerfilAsesor(
                    Nombre = nombre,
                    Apellido = apellido,
                    tipo_doc = "CC",
                    numero_doc = ced,
                    Codigo_pais = "+57",
                    Telefono = telLong,
                    Correo_electronico = cor,
                    direccion = if (dir.isNotEmpty()) dir else "No registrada",
                    codigo_asesor = if (cod.isNotEmpty()) cod else "AS-${(100..999).random()}",
                    area_atencion = if (are.isNotEmpty()) are else "Servicio al Cliente"
                )

                repo.registrarAsesor(cor, pas, nuevoAsesor).onSuccess {
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "✅ Asesor registrado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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
