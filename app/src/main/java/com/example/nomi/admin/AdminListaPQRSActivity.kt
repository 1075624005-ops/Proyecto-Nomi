package com.example.nomi.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.PQRSPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminListaPQRSActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        container = findViewById(R.id.containerAdminPQRS)
        val btnVolver = findViewById<Button>(R.id.btnVolverLista)

        btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        cargarListaDesdeSupabase(container)
    }

    private fun cargarListaDesdeSupabase(container: LinearLayout) {
        lifecycleScope.launch {
            val resultado = repo.obtenerTodasLasPQRS()
            
            container.removeAllViews()
            
            resultado.onSuccess { lista ->
                if (lista.isEmpty()) {
                    val tv = TextView(this@AdminListaPQRSActivity).apply {
                        text = "No hay PQRS registradas en el sistema."
                        setTextColor(Color.GRAY)
                        gravity = android.view.Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    container.addView(tv)
                    return@onSuccess
                }

                for (pqr in lista) {
                    val idStr = pqr.id.toString()
                    val asunto = pqr.asunto
                    val estado = pqr.estado
                    val nombre = pqr.nombre_usuario.ifEmpty { "Cliente Nomi" }
                    val correo = pqr.correo_usuario

                    val card = CardView(this@AdminListaPQRSActivity).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 24)
                        layoutParams = p
                        setCardBackgroundColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.app_surface_card))
                        radius = 16f
                        setContentPadding(30, 30, 30, 30)
                        isClickable = true
                        isFocusable = true
                    }

                    val layout = LinearLayout(this@AdminListaPQRSActivity).apply { orientation = LinearLayout.VERTICAL }

                    val rowHeader = LinearLayout(this@AdminListaPQRSActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }

                    val tvRad = TextView(this@AdminListaPQRSActivity).apply {
                        val p = LinearLayout.LayoutParams(0, -2, 1f)
                        layoutParams = p
                        text = "🗂️ RADICADO #$idStr"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.brand_primary))
                        setTypeface(null, Typeface.BOLD)
                        textSize = 15f
                    }

                    val esResuelto = estado.lowercase().contains("resuelt")
                    val tvEst = TextView(this@AdminListaPQRSActivity).apply {
                        text = if (esResuelto) "🟢 RESUELTO" else "🟡 PENDIENTE"
                        setTextColor(if (esResuelto) Color.parseColor("#28A745") else Color.parseColor("#FFC107"))
                        setTypeface(null, Typeface.BOLD)
                        textSize = 12f
                    }

                    rowHeader.addView(tvRad)
                    rowHeader.addView(tvEst)

                    val tvCliente = TextView(this@AdminListaPQRSActivity).apply {
                        text = "👤 $nombre ($correo)"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.text_primary))
                        textSize = 13f
                        setPadding(0, 6, 0, 0)
                    }

                    val tvAsu = TextView(this@AdminListaPQRSActivity).apply {
                        text = "📌 Asunto: $asunto"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.text_secondary))
                        textSize = 13f
                        setPadding(0, 4, 0, 0)
                    }

                    val tvAccion = TextView(this@AdminListaPQRSActivity).apply {
                        text = "Toca para ver detalle y responder ➔"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.brand_primary))
                        textSize = 12f
                        gravity = android.view.Gravity.END
                        setPadding(0, 10, 0, 0)
                    }

                    layout.addView(rowHeader)
                    layout.addView(tvCliente)
                    layout.addView(tvAsu)
                    layout.addView(tvAccion)

                    card.addView(layout)

                    card.setOnClickListener {
                        val intent = Intent(this@AdminListaPQRSActivity, AdminPQRSDetalleActivity::class.java)
                        intent.putExtra("pqr_id", pqr.id)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            }.onFailure {
                Toast.makeText(this@AdminListaPQRSActivity, "❌ Error al cargar lista PQRS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
