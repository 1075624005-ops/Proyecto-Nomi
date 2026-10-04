package com.example.nomi.pqrs

import android.os.Bundle
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

        val nombreInput = intent.getStringExtra("nombre") ?: ""
        val correoInput = intent.getStringExtra("correo") ?: ""

        val spTipoPqr   = findViewById<Spinner>(R.id.spTipoPQRS)
        val etAsunto    = findViewById<EditText>(R.id.etAsuntoPQRS)
        val etDesc      = findViewById<EditText>(R.id.etDescPQRS)
        val btnEnviar   = findViewById<Button>(R.id.btnEnviarPQRS)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarPQRS)
        val cbAutorizacion = findViewById<CheckBox>(R.id.cbAutorizacionPQRS)
        val tvVerAutorizacion = findViewById<TextView>(R.id.tvVerAutorizacionPQRS)

        // Marcar casilla al tocar el texto o ver política
        tvVerAutorizacion.setOnClickListener {
            cbAutorizacion.isChecked = true
            AlertDialog.Builder(this)
                .setTitle("📋 Autorización Uso de Datos (Ley 1581)")
                .setMessage("Conforme a la Ley 1581 de 2012 de Tratamiento de Datos Personales, la información suministrada se usará exclusivamente para la gestión y respuesta de su solicitud PQRS en NOMI.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        val opciones = arrayOf("Petición", "Queja", "Reclamo", "Sugerencia")
        spTipoPqr.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)

        btnEnviar.setOnClickListener {
            val asunto  = etAsunto.text.toString().trim()
            val desc    = etDesc.text.toString().trim()
            val tipoPqr = spTipoPqr.selectedItem.toString()

            if (asunto.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor ingrese el asunto y la descripción de su caso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbAutorizacion.isChecked) {
                Toast.makeText(this, "⚠️ Por favor autorice el uso de datos marcando la casilla", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnEnviar.isEnabled = false
            btnEnviar.text = "Enviando PQRS..."

            val correoFinal = if (correoInput.isNotEmpty()) correoInput else "usuario@nomi.com"
            val nombreFinal = if (nombreInput.isNotEmpty()) nombreInput else "Usuario Nomi"

            lifecycleScope.launch {
                val pqr = PQRSPostgres(
                    correo_usuario = correoFinal,
                    nombre_usuario = nombreFinal,
                    asunto = "[$tipoPqr] $asunto",
                    descripcion = desc
                )
                repo.enviarPQRS(pqr).onSuccess {
                    Toast.makeText(this@GenerarPQRSActivity, "✅ PQRS Radicada Exitosamente en NOMI", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { err ->
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar Solicitud"
                    Toast.makeText(this@GenerarPQRSActivity, "❌ Error al enviar PQRS: ${err.message}", Toast.LENGTH_LONG).show()
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
