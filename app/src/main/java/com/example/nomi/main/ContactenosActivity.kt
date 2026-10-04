package com.example.nomi.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactenosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactenos)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        // 1. BOTÓN WHATSAPP
        findViewById<View>(R.id.btnWhatsapp)?.setOnClickListener {
            val mensaje = "Hola NOMI, necesito atención e información sobre un servicio."
            val url = "https://wa.me/573138150074?text=${Uri.encode(mensaje)}"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. BOTÓN LLAMADA DE SERVICIO
        findViewById<View>(R.id.btnLlamar)?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+573138150074"))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. BOTÓN CORREO ELECTRÓNICO
        findViewById<View>(R.id.btnEmail)?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:nomi.proyecto@gmail.com")).apply {
                    putExtra(Intent.EXTRA_SUBJECT, "Consulta Soporte NOMI")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se encontró aplicación de correo", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnVolverContacto).setOnClickListener { finish() }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
