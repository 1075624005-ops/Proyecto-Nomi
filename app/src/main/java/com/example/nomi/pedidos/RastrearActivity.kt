package com.example.nomi.pedidos

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PedidoPostgres
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RastrearActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var listaCompleta: List<PedidoPostgres> = emptyList()

    private lateinit var container: LinearLayout
    private lateinit var pb: ProgressBar
    private lateinit var etBuscar: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastrear)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        container = findViewById(R.id.containerRastreo)
        pb = findViewById(R.id.pbRastreo)
        etBuscar = findViewById(R.id.etBuscarGuiaRastreo)

        findViewById<Button>(R.id.btnVolverRastreo)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnBuscarGuiaRastreo)?.setOnClickListener {
            filtrarResultados(etBuscar.text.toString().trim())
        }

        val guiaInicial = intent.getStringExtra("guia") ?: ""
        if (guiaInicial.isNotEmpty()) {
            etBuscar.setText(guiaInicial)
        }

        cargarHistorial()
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    private fun cargarHistorial() {
        pb.visibility = View.VISIBLE
        container.removeAllViews()

        lifecycleScope.launch {
            repo.obtenerPedidosAdmin().onSuccess { lista ->
                pb.visibility = View.GONE
                listaCompleta = lista
                filtrarResultados(etBuscar.text.toString().trim())
            }.onFailure {
                pb.visibility = View.GONE
                Toast.makeText(this@RastrearActivity, "❌ Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filtrarResultados(query: String) {
        container.removeAllViews()

        val listaFiltrada = if (query.isEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { it.num_guia.lowercase().contains(query.lowercase()) }
        }

        if (listaFiltrada.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No se encontraron envíos registrados."
                setTextColor(Color.GRAY)
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 0)
            }
            container.addView(tv)
            return
        }

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        for (ped in listaFiltrada) {
            val card = com.google.android.material.card.MaterialCardView(this).apply {
                val p = LinearLayout.LayoutParams(-1, -2)
                p.setMargins(0, 0, 0, 20)
                layoutParams = p
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_surface_card))
                radius = 14f
                setContentPadding(24, 24, 24, 24)
                strokeColor = ContextCompat.getColor(context, R.color.brand_primary)
                strokeWidth = 1
            }

            val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

            val rowHeader = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val tvGuia = TextView(this).apply {
                val p = LinearLayout.LayoutParams(0, -2, 1f)
                layoutParams = p
                text = "📦 GUÍA: ${ped.num_guia}"
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                setTypeface(null, Typeface.BOLD)
                textSize = 15f
            }

            val (textoEstado, colorEstado) = when (ped.estado) {
                1 -> Pair("📦 SOLICITADO", "#00AEEF")
                2 -> Pair("🛵 EN CAMINO", "#FFC107")
                3 -> Pair("✅ ENTREGADO", "#28A745")
                else -> Pair("⚠️ NOVEDAD", "#DC3545")
            }

            val tvEstado = TextView(this).apply {
                text = textoEstado
                setTextColor(Color.parseColor(colorEstado))
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
            }

            rowHeader.addView(tvGuia)
            rowHeader.addView(tvEstado)

            val tvDetalle = TextView(this).apply {
                val loc = ped.dest_localidad ?: "Bogotá"
                val dir = ped.dest_dir ?: "Dirección registrada"
                text = "📍 Destino: $loc ($dir)\n💰 Valor: ${format.format(ped.costo ?: 0.0)}"
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                textSize = 13f
                setPadding(0, 8, 0, 0)
            }

            val tvAccion = TextView(this).apply {
                text = "Toca para ver Rótulo PDF y Código QR ➔"
                setTextColor(ContextCompat.getColor(context, R.color.brand_primary))
                textSize = 12f
                gravity = android.view.Gravity.END
                setPadding(0, 10, 0, 0)
            }

            layout.addView(rowHeader)
            layout.addView(tvDetalle)
            layout.addView(tvAccion)

            card.addView(layout)

            card.setOnClickListener {
                val intentRotulo = Intent(this, RotuloActivity::class.java)
                intentRotulo.putExtra("guia", ped.num_guia)
                intentRotulo.putExtra("dest_nombre", ped.dest_nombre ?: "-")
                intentRotulo.putExtra("dest_dir", ped.dest_dir ?: "-")
                intentRotulo.putExtra("dest_tel", ped.dest_tel ?: "-")
                intentRotulo.putExtra("dest_localidad_nom", ped.dest_localidad ?: "-")
                intentRotulo.putExtra("rem_nombre", ped.rem_nombre ?: "-")
                intentRotulo.putExtra("rem_dir", ped.rem_dir ?: "-")
                intentRotulo.putExtra("ped_desc", ped.descripcion ?: "Servicio de transporte")
                intentRotulo.putExtra("ped_tipo_envio", ped.tipo_servicio ?: "Estándar")
                intentRotulo.putExtra("ped_peso", ped.peso_kg ?: "1")
                intentRotulo.putExtra("ped_costo", ped.costo ?: 0.0)
                intentRotulo.putExtra("ped_pago_contraentrega", ped.modalidad_pago == "contraentrega")
                startActivity(intentRotulo)
            }

            container.addView(card)
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
