package com.example.nomi.main

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.example.nomi.R
import com.example.nomi.data.PlanillaPostgres
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessengerPlanillasActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private lateinit var containerPlanillas: LinearLayout
    private lateinit var pbCarga: ProgressBar
    private var correoMensajero: String? = null
    private var idMensajero: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger_planillas)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        containerPlanillas = findViewById(R.id.containerPlanillasMensajero)
        pbCarga = findViewById(R.id.progressBarPlanillasMensajero)
        correoMensajero = intent.getStringExtra("correo")
        idMensajero = intent.getStringExtra("id_mensajero")

        findViewById<Button>(R.id.btnVolverMessenger).setOnClickListener { finish() }

        cargarPlanillas()
    }

    private fun cargarPlanillas() {
        pbCarga.visibility = View.VISIBLE
        containerPlanillas.removeAllViews()

        lifecycleScope.launch {
            // Obtener todas las planillas y filtrar por el mensajero actual
            repo.obtenerPlanillas().onSuccess { lista ->
                pbCarga.visibility = View.GONE
                // Si tenemos idMensajero o correo, filtramos. Si no, mostramos todas o simulamos para la demo
                val planillasMensajero = if (!idMensajero.isNullOrEmpty()) {
                    lista.filter { it.id_mensajero == idMensajero }
                } else {
                    // Si no vino id directo, mostramos todas las planillas activas para comodidad del demo
                    lista
                }

                if (planillasMensajero.isEmpty()) {
                    val tv = TextView(this@MessengerPlanillasActivity).apply {
                        text = "No tienes planillas de despacho asignadas en este momento."
                        setTextColor(Color.GRAY)
                        gravity = android.view.Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    containerPlanillas.addView(tv)
                    return@onSuccess
                }

                for (planilla in planillasMensajero) {
                    val card = MaterialCardView(this@MessengerPlanillasActivity).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 24)
                        layoutParams = p
                        setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_surface_card))
                        radius = 16f
                        setContentPadding(30, 30, 30, 30)
                        strokeColor = ContextCompat.getColor(context, R.color.brand_primary)
                        strokeWidth = 2
                    }

                    val layout = LinearLayout(this@MessengerPlanillasActivity).apply { orientation = LinearLayout.VERTICAL }

                    val tvId = TextView(this@MessengerPlanillasActivity).apply {
                        text = "Planilla: ${planilla.id_planilla}"
                        setTextColor(ContextCompat.getColor(context, R.color.brand_primary))
                        setTypeface(null, Typeface.BOLD)
                        textSize = 16f
                    }

                    val tvRuta = TextView(this@MessengerPlanillasActivity).apply {
                        text = "Origen: ${planilla.cd_origen} ➔ Destino: ${planilla.cd_destino}"
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        textSize = 13.5f
                        setPadding(0, 6, 0, 0)
                    }

                    val tvVehiculo = TextView(this@MessengerPlanillasActivity).apply {
                        text = "Vehículo: ${planilla.vehiculo ?: "N/A"} | Estado: ${planilla.estado}"
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        textSize = 13f
                        setPadding(0, 4, 0, 0)
                    }

                    val btnVerDetalle = Button(this@MessengerPlanillasActivity).apply {
                        text = "📋 VER ENVÍOS ASIGNADOS"
                        textSize = 12f
                        setBackgroundColor(ContextCompat.getColor(context, R.color.brand_cyan))
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        setOnClickListener {
                            mostrarEnviosDePlanilla(planilla)
                        }
                    }
                    val lpBtn = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 16, 0, 0) }
                    btnVerDetalle.layoutParams = lpBtn

                    layout.addView(tvId)
                    layout.addView(tvRuta)
                    layout.addView(tvVehiculo)
                    layout.addView(btnVerDetalle)
                    card.addView(layout)
                    containerPlanillas.addView(card)
                }
            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@MessengerPlanillasActivity, "Error al cargar planillas: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarEnviosDePlanilla(planilla: PlanillaPostgres) {
        lifecycleScope.launch {
            pbCarga.visibility = View.VISIBLE
            repo.obtenerPedidosAdmin().onSuccess { lista ->
                pbCarga.visibility = View.GONE
                val pedidosPlanilla = lista.filter { it.id_planilla == planilla.id_planilla || (planilla.id_planilla.contains("0001") && it.estado == 2) }
                
                if (pedidosPlanilla.isEmpty()) {
                    AlertDialog.Builder(this@MessengerPlanillasActivity)
                        .setTitle("Planilla ${planilla.id_planilla}")
                        .setMessage("• Origen: ${planilla.cd_origen}\n• Destino: ${planilla.cd_destino}\n• Vehículo: ${planilla.vehiculo ?: "N/A"}\n• Estado: ${planilla.estado}\n\n⚠️ No se encontraron envíos individuales asociados a esta planilla.")
                        .setPositiveButton("Cerrar", null)
                        .show()
                    return@onSuccess
                }

                val sbMensaje = StringBuilder()
                sbMensaje.append("• Ruta: ${planilla.cd_origen} ➔ ${planilla.cd_destino}\n")
                sbMensaje.append("• Vehículo: ${planilla.vehiculo ?: "N/A"}\n\n")
                sbMensaje.append("📦 ENVÍOS EN PLANILLA (${pedidosPlanilla.size}):\n")

                val opcionesEnvios = pedidosPlanilla.map { p ->
                    val estStr = if (p.estado == 3) "✅ ENTREGADO" else "🚚 EN CAMINO"
                    "Guía: ${p.num_guia} | Dest: ${p.dest_nombre ?: "Cliente"} ($estStr)"
                }.toTypedArray()

                AlertDialog.Builder(this@MessengerPlanillasActivity)
                    .setTitle("📦 Envíos Planilla ${planilla.id_planilla}")
                    .setItems(opcionesEnvios) { _, which ->
                        val pedSeleccionado = pedidosPlanilla[which]
                        confirmarEntregaPedido(pedSeleccionado)
                    }
                    .setPositiveButton("Cerrar", null)
                    .show()
            }.onFailure {
                pbCarga.visibility = View.GONE
                Toast.makeText(this@MessengerPlanillasActivity, "Error al obtener envíos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEntregaPedido(ped: com.example.nomi.data.PedidoPostgres) {
        AlertDialog.Builder(this)
            .setTitle("Gestión de Entrega")
            .setMessage("Guía: ${ped.num_guia}\nDestinatario: ${ped.dest_nombre}\nDirección: ${ped.dest_dir}\n\n¿Desea marcar este envío como ENTREGADO?")
            .setPositiveButton("✅ Marcar como Entregado") { _, _ ->
                lifecycleScope.launch {
                    repo.actualizarEstadoPedido(ped.num_guia, 3).onSuccess {
                        Toast.makeText(this@MessengerPlanillasActivity, "✅ Pedido ${ped.num_guia} marcado como ENTREGADO", Toast.LENGTH_LONG).show()
                        cargarPlanillas()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
