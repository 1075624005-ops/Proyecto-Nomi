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
import com.example.nomi.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminListaPQRSActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val container = findViewById<LinearLayout>(R.id.containerAdminPQRS)
        val btnVolver = findViewById<Button>(R.id.btnVolverLista)

        cargarListaDesdeSupabase(container)

        btnVolver.setOnClickListener { finish() }
    }

    private fun cargarListaDesdeSupabase(container: LinearLayout) {
        lifecycleScope.launch {
            val resultado = repo.obtenerTodasLasPQRS()
            
            container.removeAllViews()
            
            resultado.onSuccess { lista ->
                if (lista.isEmpty()) {
                    val tv = TextView(this@AdminListaPQRSActivity)
                    tv.text = "No hay PQRS registradas."
                    tv.setTextColor(Color.GRAY)
                    tv.gravity = android.view.Gravity.CENTER
                    container.addView(tv)
                    return@onSuccess
                }

                for (pqr in lista) {
                    val idStr = pqr.id.toString()
                    val asunto = pqr.asunto
                    val estado = pqr.estado
                    val nombre = pqr.nombre_usuario

                    val card = CardView(this@AdminListaPQRSActivity).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 32)
                        layoutParams = p
                        setCardBackgroundColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.app_surface_card))
                        radius = 15f
                        setContentPadding(25, 25, 25, 25)
                        isClickable = true
                        isFocusable = true
                    }

                    val layout = LinearLayout(this@AdminListaPQRSActivity).apply { orientation = LinearLayout.VERTICAL }

                    val tvRad = TextView(this@AdminListaPQRSActivity).apply {
                        text = "RADICADO #$idStr - $nombre"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.brand_primary))
                        setTypeface(null, Typeface.BOLD)
                    }

                    val tvAsu = TextView(this@AdminListaPQRSActivity).apply {
                        text = "Asunto: $asunto"
                        setTextColor(ContextCompat.getColor(this@AdminListaPQRSActivity, R.color.text_primary))
                    }

                    val tvEst = TextView(this@AdminListaPQRSActivity).apply {
                        text = "Estado: $estado"
                        setTextColor(if (estado == "Pendiente") Color.YELLOW else Color.GREEN)
                    }

                    layout.addView(tvRad)
                    layout.addView(tvAsu)
                    layout.addView(tvEst)
                    card.addView(layout)

                    card.setOnClickListener {
                        val intent = Intent(this@AdminListaPQRSActivity, AdminPQRSDetalleActivity::class.java)
                        intent.putExtra("pqr_id", pqr.id)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            }.onFailure {
                Toast.makeText(this@AdminListaPQRSActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
