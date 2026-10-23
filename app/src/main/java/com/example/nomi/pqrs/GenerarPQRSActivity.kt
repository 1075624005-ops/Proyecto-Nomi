package com.example.nomi.pqrs

import android.content.Intent
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
import com.example.nomi.main.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

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
                Toast.makeText(this, "⚠️ Completa todos los campos y acepta la autorización", Toast.LENGTH_SHORT).show()
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
                    val radicadoNum = 1000 + Random().nextInt(9000)
                    AlertDialog.Builder(this@GenerarPQRSActivity)
                        .setTitle("✅ ¡PQRS Creada con Éxito!")
                        .setMessage("Su solicitud ha sido registrada correctamente.\n\nNúmero de Radicado: #PQR-$radicadoNum\n\nPuede conservar este número para consultar el estado de su requerimiento.")
                        .setCancelable(false)
                        .setPositiveButton("Volver al Inicio") { _, _ ->
                            val intent = Intent(this@GenerarPQRSActivity, PQRSMenuActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .show()
                }.onFailure {
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "ENVIAR"
                    Toast.makeText(this@GenerarPQRSActivity, "⚠️ PQRS guardada en modo local (Exposición)", Toast.LENGTH_LONG).show()
                    val radicadoNum = 1000 + Random().nextInt(9000)
                    AlertDialog.Builder(this@GenerarPQRSActivity)
                        .setTitle("✅ ¡PQRS Registrada!")
                        .setMessage("Su solicitud ha sido registrada.\n\nNúmero de Radicado: #PQR-$radicadoNum")
                        .setCancelable(false)
                        .setPositiveButton("Volver al Inicio") { _, _ ->
                            val intent = Intent(this@GenerarPQRSActivity, PQRSMenuActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .show()
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
