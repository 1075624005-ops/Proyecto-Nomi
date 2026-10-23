package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.LocalDemoStorage
import com.example.nomi.data.PQRSPostgres
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.launch

class AdminPQRSDetalleActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var pqrId: Int = -1
    private var pqrActual: PQRSPostgres? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pqrs_detalle)

        pqrId = intent.getIntExtra("pqr_id", -1)

        val tvTitle          = findViewById<TextView>(R.id.tvAdminDetalleTitle)
        val detNombre        = findViewById<TextView>(R.id.detNombre)
        val detTipoPers      = findViewById<TextView>(R.id.detTipoPers)
        val detDoc           = findViewById<TextView>(R.id.detDoc)
        val detCorreo        = findViewById<TextView>(R.id.detCorreo)
        val detDir           = findViewById<TextView>(R.id.detDir)
        val detDesc          = findViewById<TextView>(R.id.detDesc)
        val etRespuesta      = findViewById<EditText>(R.id.etAdminRespuesta)
        val btnEnviarResp    = findViewById<Button>(R.id.btnAdminEnviarResp)
        val btnVolver        = findViewById<Button>(R.id.btnAdminVolver)

        btnVolver.setOnClickListener { finish() }

        cargarDetallePQRS(detNombre, detTipoPers, detDoc, detCorreo, detDir, detDesc, etRespuesta, tvTitle)

        btnEnviarResp.setOnClickListener {
            val respTexto = etRespuesta.text.toString().trim()
            if (respTexto.isEmpty()) {
                Toast.makeText(this, "⚠️ Ingrese el texto de la respuesta antes de enviar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pqrId == -1) {
                Toast.makeText(this, "Error: Radicado no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEnviarResp.isEnabled = false
            btnEnviarResp.text = "Guardando..."

            lifecycleScope.launch {
                repo.responderPQRS(pqrId, respTexto, "Resuelto").onSuccess {
                    Toast.makeText(this@AdminPQRSDetalleActivity, "✅ Respuesta enviada y guardada con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure {
                    btnEnviarResp.isEnabled = true
                    btnEnviarResp.text = "ENVIAR RESPUESTA"
                    Toast.makeText(this@AdminPQRSDetalleActivity, "⚠️ Error al guardar respuesta", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarDetallePQRS(
        tvNombre: TextView,
        tvTipo: TextView,
        tvDoc: TextView,
        tvCorreo: TextView,
        tvDir: TextView,
        tvDesc: TextView,
        etResp: EditText,
        tvTitle: TextView
    ) {
        lifecycleScope.launch {
            val lista = LocalDemoStorage.obtenerPQRS()
            pqrActual = lista.find { it.id == pqrId }

            if (pqrActual == null && pqrId != -1) {
                repo.buscarPQRSPorRadicado(pqrId).onSuccess { res ->
                    pqrActual = res.firstOrNull()
                }
            }

            val pqr = pqrActual
            if (pqr != null) {
                tvTitle.text = "RADICADO #${pqr.id} - ${pqr.estado.uppercase()}"
                tvNombre.text = "Nombre: ${pqr.nombre_usuario.ifEmpty { "Cliente Nomi" }}"
                tvTipo.text = "Tipo: Requerimiento PQRS"
                tvDoc.text = "Radicado ID: #${pqr.id}"
                tvCorreo.text = "Correo: ${pqr.correo_usuario}"
                tvDir.text = "Estado Actual: ${pqr.estado}"
                tvDesc.text = "ASUNTO: ${pqr.asunto}\n\nSITUACIÓN REPORTADA:\n${pqr.descripcion}"

                if (!pqr.respuesta.isNullOrEmpty()) {
                    etResp.setText(pqr.respuesta)
                }
            } else {
                Toast.makeText(this@AdminPQRSDetalleActivity, "No se encontró el detalle del radicado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
