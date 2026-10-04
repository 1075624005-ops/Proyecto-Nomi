package com.example.nomi.pqrs

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
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.PQRSPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GenerarPQRSActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val nombre      = intent.getStringExtra("nombre") ?: ""
        val correo      = intent.getStringExtra("correo") ?: "anonimo@nomi.com"

        val spTipoPqr   = findViewById<Spinner>(R.id.spTipoPQRS)
        val etAsunto    = findViewById<EditText>(R.id.etAsuntoPQRS)
        val etDesc      = findViewById<EditText>(R.id.etDescPQRS)
        val btnEnviar   = findViewById<Button>(R.id.btnEnviarPQRS)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarPQRS)
        val cbAutorizacion = findViewById<CheckBox>(R.id.cbAutorizacionPQRS)
        val tvVerAutorizacion = findViewById<TextView>(R.id.tvVerAutorizacionPQRS)

        tvVerAutorizacion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Autorización uso de datos")
                .setMessage("Conforme a la Ley 1581 de 2012...")
                .setPositiveButton("Aceptar") { _, _ -> cbAutorizacion.isChecked = true }
                .show()
        }

        val opciones = arrayOf("Petición", "Queja", "Reclamo", "Sugerencia")
        spTipoPqr.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)

        btnEnviar.setOnClickListener {
            val asunto  = etAsunto.text.toString().trim()
            val desc    = etDesc.text.toString().trim()
            if (asunto.isEmpty() || desc.isEmpty() || !cbAutorizacion.isChecked) {
                Toast.makeText(this, "⚠️ Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnEnviar.isEnabled = false
            btnEnviar.text = "Enviando..."

            lifecycleScope.launch {
                val pqr = PQRSPostgres(
                    correo_usuario = correo,
                    nombre_usuario = nombre,
                    asunto = asunto,
                    descripcion = desc
                )
                repo.enviarPQRS(pqr).onSuccess {
                    Toast.makeText(this@GenerarPQRSActivity, "✅ PQRS Enviada", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure {
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "ENVIAR"
                }
            }
        }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
