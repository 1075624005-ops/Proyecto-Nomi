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

        val btnSiguiente = findViewById<Button>(R.id.btnSiguientePQR)
        // Recolección de datos y navegación a GenerarPQRS
        btnSiguiente.setOnClickListener {
            val intent = Intent(this, GenerarPQRSActivity::class.java)
            // Pasar datos via extras
            startActivity(intent)
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
