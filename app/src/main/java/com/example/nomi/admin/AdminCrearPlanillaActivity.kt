package com.example.nomi.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilMensajero
import com.example.nomi.data.PedidoPostgres
import com.example.nomi.data.PlanillaPostgres
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminCrearPlanillaActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private val listaMensajeros = mutableListOf<PerfilMensajero>()
    private val listaPedidos = mutableListOf<PedidoPostgres>()
    private val guiasSeleccionadas = mutableSetOf<String>()

    private lateinit var spinnerMensajeros: Spinner
    private lateinit var containerEnvios: LinearLayout
    private lateinit var etIdPlanilla: EditText
    private lateinit var etCdOrigen: EditText
    private lateinit var etCdDestino: EditText
    private lateinit var etVehiculo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_crear_planilla)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        etIdPlanilla = findViewById(R.id.etIdPlanilla)
        etCdOrigen = findViewById(R.id.etCdOrigen)
        etCdDestino = findViewById(R.id.etCdDestino)
        etVehiculo = findViewById(R.id.etVehiculo)
        spinnerMensajeros = findViewById(R.id.spinnerMensajeros)
        containerEnvios = findViewById(R.id.containerEnviosSeleccion)

        // Generar ID sugerido consecutivo
        lifecycleScope.launch {
            repo.obtenerPlanillas().onSuccess { planillas ->
                val consecutivo = String.format(Locale.US, "PLN-%04d", planillas.size + 1)
                etIdPlanilla.setText(consecutivo)
            }.onFailure {
                val fechaStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())
                etIdPlanilla.setText("PLN-$fechaStr")
            }
        }

        cargarDatos()

        findViewById<Button>(R.id.btnGenerarPlanilla).setOnClickListener {
            generarPlanilla()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            // 1. Cargar mensajeros
            repo.obtenerTodosLosMensajeros().onSuccess { mensajeros ->
                listaMensajeros.clear()
                listaMensajeros.addAll(mensajeros)
                val nombres = mensajeros.map { "${it.Nombre} ${it.Apellido ?: ""} (${it.zona_asignada ?: "Bogotá D.C."})" }
                val adapter = ArrayAdapter(this@AdminCrearPlanillaActivity, android.R.layout.simple_spinner_item, nombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMensajeros.adapter = adapter
            }

            // 2. Cargar pedidos
            repo.obtenerPedidosAdmin().onSuccess { pedidos ->
                listaPedidos.clear()
                // Solo pedidos activos pendientes (estado 1 o sin planilla)
                listaPedidos.addAll(pedidos.filter { it.estado == 1 })
                renderizarPedidos()
            }
        }
    }

    private fun renderizarPedidos() {
        containerEnvios.removeAllViews()
        if (listaPedidos.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No hay envíos pendientes en pool para despachar."
                setTextColor(android.graphics.Color.GRAY)
                setPadding(10, 20, 10, 20)
            }
            containerEnvios.addView(tv)
            return
        }

        for (ped in listaPedidos) {
            val cb = CheckBox(this).apply {
                text = "Guía: ${ped.num_guia} | Dest: ${ped.dest_nombre ?: "N/A"} (${ped.dest_localidad ?: "Bogotá D.C."})"
                setTextColor(resources.getColor(R.color.text_primary, null))
                textSize = 13.5f
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        guiasSeleccionadas.add(ped.num_guia)
                    } else {
                        guiasSeleccionadas.remove(ped.num_guia)
                    }
                }
            }
            containerEnvios.addView(cb)
        }
    }

    private fun generarPlanilla() {
        val idPlanilla = etIdPlanilla.text.toString().trim()
        val cdOrigen = etCdOrigen.text.toString().trim()
        val cdDestino = etCdDestino.text.toString().trim()
        val vehiculo = etVehiculo.text.toString().trim()

        if (idPlanilla.isEmpty()) {
            Toast.makeText(this, "Ingrese el ID de la planilla", Toast.LENGTH_SHORT).show()
            return
        }

        if (guiasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un envío para la planilla", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedMessengerIndex = spinnerMensajeros.selectedItemPosition
        val mensajeroSeleccionado = if (selectedMessengerIndex >= 0 && selectedMessengerIndex < listaMensajeros.size) {
            listaMensajeros[selectedMessengerIndex]
        } else {
            null
        }

        val idMensajero = mensajeroSeleccionado?.id_usuario
        val nombreMensajero = mensajeroSeleccionado?.let { "${it.Nombre} ${it.Apellido ?: ""}".trim() } ?: "Sin asignar"

        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val planilla = PlanillaPostgres(
            id_planilla = idPlanilla,
            cd_origen = cdOrigen,
            cd_destino = cdDestino,
            id_mensajero = idMensajero,
            vehiculo = vehiculo,
            estado = "En Tránsito",
            fecha_creacion = fechaActual
        )

        lifecycleScope.launch {
            val resPlanilla = repo.crearPlanilla(planilla)
            if (resPlanilla.isSuccess) {
                // Asignar cada guía seleccionada a la planilla y cambiar estado a 2 (En Ruta/Planilla)
                for (guia in guiasSeleccionadas) {
                    repo.asignarPedidoAPlanilla(guia, idPlanilla)
                    repo.actualizarEstadoPedido(guia, 2)
                }

                // Construir desglose de guías para mostrar la planilla creada al usuario
                val sbGuias = StringBuilder()
                guiasSeleccionadas.forEach { g ->
                    sbGuias.append("• Guía: $g (Estado: EN CAMINO)\n")
                }

                val mensajeResumen = """
                    📋 **PLANILLA DESPACHADA EXITOSAMENTE**
                    
                    • **ID Planilla:** $idPlanilla
                    • **CD Origen:** $cdOrigen
                    • **CD Destino:** $cdDestino
                    • **Vehículo:** $vehiculo
                    • **Mensajero Asignado:** $nombreMensajero
                    • **Estado:** En Tránsito
                    
                    📦 **ENVÍOS INCLUIDOS (${guiasSeleccionadas.size}):**
                    $sbGuias
                    
                    ✅ *El estado de todos los envíos ha sido actualizado a EN CAMINO (Estado 2).*
                """.trimIndent()

                androidx.appcompat.app.AlertDialog.Builder(this@AdminCrearPlanillaActivity)
                    .setTitle("✅ PLANILLA DE DESPACHO CREADA")
                    .setMessage(mensajeResumen)
                    .setCancelable(false)
                    .setPositiveButton("Aceptar y Finalizar") { _, _ ->
                        finish()
                    }
                    .show()
            } else {
                Toast.makeText(this@AdminCrearPlanillaActivity, "❌ Error al crear planilla: ${resPlanilla.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
