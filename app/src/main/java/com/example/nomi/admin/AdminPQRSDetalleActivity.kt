package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PQRSPostgres
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminPQRSDetalleActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var pqrId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pqrs_detalle)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        pqrId = intent.getIntExtra("pqr_id", -1)

        val detNombre   = findViewById<TextView>(R.id.detNombre)
        val detCorreo   = findViewById<TextView>(R.id.detCorreo)
        val detDesc     = findViewById<TextView>(R.id.detDesc)
        val etRespuesta = findViewById<EditText>(R.id.etAdminRespuesta)
        val btnEnviar   = findViewById<Button>(R.id.btnAdminEnviarResp)
        val btnVolver   = findViewById<Button>(R.id.btnAdminVolver)

        btnVolver.setOnClickListener { finish() }

        if (pqrId != -1) {
            cargarDetallePQRS(detNombre, detCorreo, detDesc, etRespuesta)
        } else {
            Toast.makeText(this, "⚠️ Radicado no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnEnviar.setOnClickListener {
            val respuestaTexto = etRespuesta.text.toString().trim()
            if (respuestaTexto.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor redacte la respuesta antes de enviar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEnviar.isEnabled = false
            btnEnviar.text = "Guardando..."

            lifecycleScope.launch {
                repo.responderPQRS(pqrId, respuestaTexto).onSuccess {
                    Toast.makeText(this@AdminPQRSDetalleActivity, "✅ Respuesta enviada exitosamente al cliente", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { err ->
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "ENVIAR RESPUESTA"
                    Toast.makeText(this@AdminPQRSDetalleActivity, "❌ Error al responder: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargarDetallePQRS(tvNombre: TextView, tvCorreo: TextView, tvDesc: TextView, etResp: EditText) {
        lifecycleScope.launch {
            repo.buscarPQRSPorRadicado(pqrId).onSuccess { lista ->
                if (lista.isNotEmpty()) {
                    val pqr = lista[0]
                    tvNombre.text = "Cliente: ${pqr.nombre_usuario.ifEmpty { "Usuario Nomi" }}"
                    tvCorreo.text = "Correo: ${pqr.correo_usuario}"
                    tvDesc.text = "Asunto: ${pqr.asunto}\n\nDescripción:\n${pqr.descripcion}"

                    if (!pqr.respuesta.isNullOrEmpty()) {
                        etResp.setText(pqr.respuesta)
                    }
                }
            }.onFailure {
                Toast.makeText(this@AdminPQRSDetalleActivity, "❌ Error al cargar detalle", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
