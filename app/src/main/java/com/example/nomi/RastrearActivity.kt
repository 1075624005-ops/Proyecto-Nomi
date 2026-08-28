package com.example.nomi

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RastrearActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastrear)

        db = DatabaseHelper(this)

        // Quitamos la lógica del buscador interno ya que lo eliminamos del XML
        val guiaRecibida = intent.getStringExtra("guia") ?: ""
        
        if (guiaRecibida.isNotEmpty()) {
            val estado = db.obtenerEstadoPedido(guiaRecibida)
            if (estado != -1) {
                actualizarEstado(estado)
            } else {
                Toast.makeText(this, "Guía no encontrada", Toast.LENGTH_SHORT).show()
                finish() // Volver al inicio si no existe
            }
        }
    }

    private fun actualizarEstado(paso: Int) {
        val colorActivo = ContextCompat.getColor(this, R.color.brand_primary)
        val colorInactivo = Color.GRAY

        // Actualizamos los iconos de los pasos
        findViewById<ImageView>(R.id.step1).setColorFilter(if (paso >= 1) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step2).setColorFilter(if (paso >= 2) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step3).setColorFilter(if (paso >= 3) colorActivo else colorInactivo)
        findViewById<ImageView>(R.id.step4).setColorFilter(if (paso >= 4) colorActivo else colorInactivo)
        
        // También podemos cambiar el color del texto si quisiéramos (opcional)
    }
}