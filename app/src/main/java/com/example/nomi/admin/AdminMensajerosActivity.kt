package com.example.nomi.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseClient
import com.example.nomi.data.UsuarioPostgres
import com.example.nomi.*
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminMensajerosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_mensajeros)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val container = findViewById<LinearLayout>(R.id.containerMensajeros)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarMensajero)
        val btnVolver  = findViewById<Button>(R.id.btnVolverAdmin)

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, AdminRegistrarMensajeroActivity::class.java))
        }

        btnVolver.setOnClickListener { finish() }

        cargarPlanilla(container)
    }

    private fun cargarPlanilla(container: LinearLayout) {
        lifecycleScope.launch {
            try {
                val client = SupabaseClient.client
                val lista = client.from("usuarios").select {
                    filter {
                        eq("rol", "mensajero")
                    }
                }.decodeList<UsuarioPostgres>()

                container.removeAllViews()
                if (lista.isEmpty()) {
                    val tv = TextView(this@AdminMensajerosActivity).apply {
                        text = "No hay mensajeros registrados."
                        setTextColor(Color.GRAY)
                        gravity = android.view.Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    container.addView(tv)
                    return@launch
                }

                for (mensajero in lista) {
                    val card = com.google.android.material.card.MaterialCardView(this@AdminMensajerosActivity).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 24)
                        layoutParams = p
                        setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_surface_card))
                        radius = 16f
                        setContentPadding(30, 30, 30, 30)
                        strokeColor = ContextCompat.getColor(context, R.color.brand_primary)
                        strokeWidth = 2
                    }

                    val layout = LinearLayout(this@AdminMensajerosActivity).apply { orientation = LinearLayout.VERTICAL }
                    
                    val tvNombre = TextView(this@AdminMensajerosActivity).apply {
                        text = mensajero.nombre.uppercase()
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        setTypeface(null, Typeface.BOLD)
                        textSize = 16f
                    }

                    val tvInfo = TextView(this@AdminMensajerosActivity).apply {
                        val area = mensajero.area ?: "N/A"
                        val placa = mensajero.placa ?: "N/A"
                        text = "Área: $area | Vehículo: $placa"
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        textSize = 13f
                        setPadding(0, 8, 0, 0)
                    }

                    layout.addView(tvNombre)
                    layout.addView(tvInfo)
                    card.addView(layout)
                    container.addView(card)
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminMensajerosActivity, "Error al cargar planilla", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
