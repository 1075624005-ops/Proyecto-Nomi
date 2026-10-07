package com.example.nomi.pqrs

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DatosPQRSActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val spTipoDoc = findViewById<Spinner>(R.id.spTipoDocPQR)
        val opcionesDoc = arrayOf("Cédula de Ciudadanía", "Cédula de Extranjería", "NIT / Pasaporte")
        spTipoDoc.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesDoc)

        val etNombre = findViewById<EditText>(R.id.etNombrePQR)
        val etCorreo = findViewById<EditText>(R.id.etCorreoPQR)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguientePQR)

        val correoInput = intent.getStringExtra("correo")
        if (!correoInput.isNullOrEmpty()) {
            etCorreo.setText(correoInput)
        }

        btnSiguiente.setOnClickListener {
            val nom = etNombre.text.toString().trim()
            val cor = etCorreo.text.toString().trim()

            if (nom.isEmpty() || cor.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor ingrese su nombre y correo electrónico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, GenerarPQRSActivity::class.java).apply {
                putExtra("nombre", nom)
                putExtra("correo", cor)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
