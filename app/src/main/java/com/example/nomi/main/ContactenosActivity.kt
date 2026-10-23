package com.example.nomi.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
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

        findViewById<View>(R.id.btnWhatsapp).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/573000000000?text=Hola,%20necesito%20soporte%20con%20un%20envío%20en%20Nomi"))
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnLlamar).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+573000000000"))
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnEmail).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@nomi.com?subject=Soporte%20App%20Nomi"))
                startActivity(intent)
            } catch (_: Exception) {
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
