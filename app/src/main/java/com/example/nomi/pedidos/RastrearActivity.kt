package com.example.nomi.pedidos

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RastrearActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastrear)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        findViewById<Button>(R.id.btnVolverRastreo)?.setOnClickListener { finish() }

        val guiaRecibida = intent.getStringExtra("guia") ?: ""
        
        if (guiaRecibida.isNotEmpty()) {
            Toast.makeText(this, "🚧 Módulo en mantenimiento para la versión actual", Toast.LENGTH_LONG).show()
        }
    }

    private fun actualizarEstado(paso: Int) {
        val colorActivo = ContextCompat.getColor(this, R.color.brand_primary)
        val colorInactivo = Color.GRAY

        findViewById<ImageView>(R.id.step1).setColorFilter(if (paso >= 1) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step2).setColorFilter(if (paso >= 2) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step3).setColorFilter(if (paso >= 3) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step4).setColorFilter(if (paso >= 4) colorActivo else colorInactivo)
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
