package com.example.nomi.pqrs

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
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

class ConsultarPQRSActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultar_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val correo    = intent.getStringExtra("correo") ?: ""
        val container = findViewById<LinearLayout>(R.id.containerPQRS)
        val btnVolver = findViewById<Button>(R.id.btnVolverConsultar)

        val etBuscarRadicado = EditText(this).apply {
            hint = "Buscar por radicado"
            setTextColor(Color.BLACK)
            setBackgroundResource(android.R.drawable.editbox_background_normal)
            setPadding(30, 30, 30, 30)
        }
        container.addView(etBuscarRadicado, 0)

        etBuscarRadicado.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                val num = etBuscarRadicado.text.toString().toIntOrNull()
                if (num != null) {
                    buscar(num, container, etBuscarRadicado)
                } else {
                    cargar(correo, container, etBuscarRadicado)
                }
                true
            } else false
        }

        cargar(correo, container, etBuscarRadicado)
        btnVolver.setOnClickListener { finish() }
    }

    private fun cargar(correo: String, container: LinearLayout, buscador: View) {
        lifecycleScope.launch {
            repo.obtenerMisPQRS(correo).onSuccess { lista ->
                container.removeAllViews()
                container.addView(buscador)
                lista.forEach { renderizar(it, container) }
            }
        }
    }

    private fun buscar(num: Int, container: LinearLayout, buscador: View) {
        lifecycleScope.launch {
            repo.buscarPQRSPorRadicado(num).onSuccess { lista ->
                container.removeAllViews()
                container.addView(buscador)
                lista.forEach { renderizar(it, container) }
            }
        }
    }

    private fun renderizar(pqr: PQRSPostgres, container: LinearLayout) {
        val card = CardView(this).apply {
            val p = LinearLayout.LayoutParams(-1, -2)
            p.setMargins(0, 30, 0, 0)
            layoutParams = p
            setCardBackgroundColor(ContextCompat.getColor(this@ConsultarPQRSActivity, R.color.app_surface_card))
            radius = 15f
            setContentPadding(25, 25, 25, 25)
        }
        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val tvR = TextView(this).apply {
            text = "RADICADO #${pqr.id}"
            setTextColor(ContextCompat.getColor(this@ConsultarPQRSActivity, R.color.brand_primary))
            setTypeface(null, Typeface.BOLD)
        }
        val tvA = TextView(this).apply {
            text = pqr.asunto
            setTextColor(ContextCompat.getColor(this@ConsultarPQRSActivity, R.color.text_primary))
        }
        val tvE = TextView(this).apply {
            text = "Estado: ${pqr.estado}"
            setTextColor(if (pqr.estado == "Pendiente") Color.YELLOW else Color.GREEN)
        }
        lay.addView(tvR); lay.addView(tvA); lay.addView(tvE)
        card.addView(lay)
        container.addView(card)
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
