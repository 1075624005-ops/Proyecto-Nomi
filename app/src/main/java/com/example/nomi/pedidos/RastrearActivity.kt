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

        val etGuia = findViewById<EditText>(R.id.etGuiaRastreo)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarGuiaRastreo)
        val tvGuiaTitle = findViewById<TextView>(R.id.tvDetalleGuiaRastreo)
        val tvGuiaSub = findViewById<TextView>(R.id.tvDetalleInfoRastreo)

        findViewById<Button>(R.id.btnVolverRastreo)?.setOnClickListener { finish() }

        val guiaRecibida = intent.getStringExtra("guia") ?: ""
        if (guiaRecibida.isNotEmpty()) {
            etGuia.setText(guiaRecibida)
            buscarPedido(guiaRecibida, tvGuiaTitle, tvGuiaSub)
        }

        btnBuscar.setOnClickListener {
            val guiaQuery = etGuia.text.toString().trim()
            if (guiaQuery.isNotEmpty()) {
                buscarPedido(guiaQuery, tvGuiaTitle, tvGuiaSub)
            } else {
                Toast.makeText(this, "Ingrese un número de guía válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscarPedido(guia: String, tvTitle: TextView, tvSub: TextView) {
        lifecycleScope.launch {
            repo.obtenerPedidosAdmin().onSuccess { lista ->
                val ped = lista.find { it.num_guia.equals(guia, ignoreCase = true) }
                if (ped != null) {
                    tvTitle.text = "Guía: ${ped.num_guia}"
                    val planStr = if (!ped.id_planilla.isNullOrEmpty()) " | Planilla: ${ped.id_planilla}" else ""
                    tvSub.text = "Destinatario: ${ped.dest_nombre ?: "Cliente"} | Dir: ${ped.dest_dir ?: "Bogotá"}$planStr"

                    val estadoPaso = when (ped.estado) {
                        1 -> 1 // Solicitado
                        2 -> 2 // En Camino / Planilla
                        3 -> 4 // Entregado con éxito
                        else -> 2
                    }
                    actualizarEstado(estadoPaso)
                } else {
                    tvTitle.text = "Guía: $guia (No encontrada)"
                    tvSub.text = "Verifique el número de guía e intente de nuevo."
                    actualizarEstado(1)
                }
            }.onFailure {
                Toast.makeText(this@RastrearActivity, "Error al consultar guía", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarEstado(paso: Int) {
        val colorActivo = ContextCompat.getColor(this, R.color.brand_primary)
        val colorInactivo = Color.parseColor("#555555")

        val tvPaso1 = findViewById<TextView>(R.id.tvPaso1Titulo)
        val tvPaso2 = findViewById<TextView>(R.id.tvPaso2Titulo)
        val tvPaso3 = findViewById<TextView>(R.id.tvPaso3Titulo)
        val tvPaso4 = findViewById<TextView>(R.id.tvPaso4Titulo)

        findViewById<ImageView>(R.id.step1).setColorFilter(if (paso >= 1) colorActivo else colorInactivo)
        tvPaso1?.setTextColor(if (paso >= 1) ContextCompat.getColor(this, R.color.text_primary) else Color.GRAY)

        findViewById<ImageView>(R.id.step2).setColorFilter(if (paso >= 2) colorActivo else colorInactivo)
        tvPaso2?.setTextColor(if (paso >= 2) ContextCompat.getColor(this, R.color.text_primary) else Color.GRAY)

        findViewById<ImageView>(R.id.step3).setColorFilter(if (paso >= 3) colorActivo else colorInactivo)
        tvPaso3?.setTextColor(if (paso >= 3) ContextCompat.getColor(this, R.color.text_primary) else Color.GRAY)

        findViewById<ImageView>(R.id.step4).setColorFilter(if (paso >= 4) colorActivo else colorInactivo)
        tvPaso4?.setTextColor(if (paso >= 4) ContextCompat.getColor(this, R.color.text_primary) else Color.GRAY)
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
