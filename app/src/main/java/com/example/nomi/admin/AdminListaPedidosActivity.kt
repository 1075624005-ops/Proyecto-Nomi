package com.example.nomi.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PedidoPostgres
import com.example.nomi.data.SupabaseClient
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.pedidos.RotuloActivity
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AdminListaPedidosActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var listaPedidosCompleta: List<PedidoPostgres> = emptyList()

    private lateinit var containerPedidos: LinearLayout
    private lateinit var pbCarga: ProgressBar
    private lateinit var etBuscarGuia: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pedidos)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        containerPedidos = findViewById(R.id.containerPedidos)
        pbCarga = findViewById(R.id.progressBarPedidos)
        etBuscarGuia = findViewById(R.id.etBuscarGuia)

        findViewById<Button>(R.id.btnVolverPedidos).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnFiltrar).setOnClickListener {
            filtrarLista(etBuscarGuia.text.toString().trim())
        }

        findViewById<Button>(R.id.btnLimpiar).setOnClickListener {
            etBuscarGuia.setText("")
            filtrarLista("")
        }

        cargarPedidosEnTiempoReal()
    }

    override fun onResume() {
        super.onResume()
        cargarPedidosEnTiempoReal()
    }

    private fun cargarPedidosEnTiempoReal() {
        pbCarga.visibility = View.VISIBLE
        containerPedidos.removeAllViews()

        lifecycleScope.launch {
            repo.obtenerPedidosAdmin().onSuccess { lista ->
                pbCarga.visibility = View.GONE
                listaPedidosCompleta = lista
                filtrarLista(etBuscarGuia.text.toString().trim())
            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al cargar pedidos: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        containerPedidos.removeAllViews()

        val correoMensajeroFilter = intent.getStringExtra("correo_mensajero")

        var listaFiltrada = if (query.isEmpty()) {
            listaPedidosCompleta
        } else {
            listaPedidosCompleta.filter { it.num_guia.lowercase().contains(query.lowercase()) }
        }

        if (!correoMensajeroFilter.isNullOrEmpty()) {
            listaFiltrada = listaFiltrada.filter {
                it.id_mensajero?.equals(correoMensajeroFilter, ignoreCase = true) == true
            }
        }

        if (listaFiltrada.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "No se encontraron pedidos registrados"
                setTextColor(Color.GRAY)
                textSize = 15f
                setPadding(20, 40, 20, 20)
            }
            containerPedidos.addView(tvVacio)
            return
        }

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        for (ped in listaFiltrada) {
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_usuario_card, containerPedidos, false)
            
            val tvGuia = cardView.findViewById<TextView>(R.id.tvNombreUsuarioCard)
            val tvEstado = cardView.findViewById<TextView>(R.id.tvRolUsuarioCard)
            val tvMonto = cardView.findViewById<TextView>(R.id.tvCorreoUsuarioCard)
            val tvMensajero = cardView.findViewById<TextView>(R.id.tvDocUsuarioCard)
            val tvAccion = cardView.findViewById<TextView>(R.id.tvTelUsuarioCard)

            tvGuia.text = "Guía: ${ped.num_guia}"
            tvMonto.text = "Valor Total: ${format.format(ped.costo ?: 0.0)}"
            tvMensajero.text = if (ped.id_mensajero.isNullOrEmpty()) "Mensajero: Sin asignar" else "Mensajero: ${ped.id_mensajero}"

            val (textoEstado, colorEstado) = when (ped.estado) {
                1 -> Pair("SOLICITADO", "#00AEEF")
                2 -> Pair("EN CAMINO", "#FFC107")
                3 -> Pair("ENTREGADO", "#28A745")
                else -> Pair("NOVEDAD", "#DC3545")
            }

            tvEstado.text = textoEstado
            tvEstado.setTextColor(Color.parseColor(colorEstado))
            tvAccion.text = "Toca para ver resumen y gestionar"

            cardView.setOnClickListener {
                mostrarResumenYGestion(ped)
            }

            containerPedidos.addView(cardView)
        }
    }

    private fun mostrarResumenYGestion(ped: PedidoPostgres) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val resumenTexto = StringBuilder().apply {
            append("📍 REMITENTE:\n")
            append("${ped.rem_nombre ?: "N/A"}\nTel: ${ped.rem_tel ?: "N/A"}\nDir: ${ped.rem_dir ?: "N/A"}\n\n")
            append("🎯 DESTINATARIO:\n")
            append("${ped.dest_nombre ?: "N/A"}\nTel: ${ped.dest_tel ?: "N/A"}\nDir: ${ped.dest_dir ?: "N/A"}, ${ped.dest_localidad ?: ""}\n\n")
            append("📦 CONTENIDO:\n")
            append("${ped.descripcion ?: "Sin descripción"}\nServicio: ${ped.tipo_servicio ?: "Estándar"} - ${ped.peso_kg ?: ""}\n\n")
            append("💰 VALOR Y PAGO:\n")
            append("Total: ${format.format(ped.costo ?: 0.0)}\nModalidad: ${ped.modalidad_pago?.uppercase() ?: "CONTRAENTREGA"}\nEstado Pago: ${ped.estado_pago ?: "Pendiente"}\n\n")
            append("🛵 DOMICILIARIO ASIGNADO:\n")
            append(if (ped.id_mensajero.isNullOrEmpty()) "Sin Domiciliario Asignado" else ped.id_mensajero)
        }.toString()

        val opciones = arrayOf(
            "🛵 Asignar Domiciliario / Mensajero",
            "💳 Marcar Estado de Pago (PAGADO / PENDIENTE)",
            "🔄 Cambiar Estado de Entrega",
            "📋 Ver / Reimprimir Rótulo PDF",
            "❌ Cerrar"
        )

        AlertDialog.Builder(this)
            .setTitle("Guía Oficial NOMI: ${ped.num_guia}")
            .setMessage(resumenTexto)
            .setPositiveButton("GESTIONAR") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Opciones de Gestión - ${ped.num_guia}")
                    .setItems(opciones) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoAsignarMensajero(ped)
                            1 -> mostrarDialogoCambiarEstadoPago(ped)
                            2 -> mostrarDialogoCambiarEstado(ped)
                            3 -> abrirRotuloPdf(ped)
                        }
                    }
                    .show()
            }
            .setNegativeButton("CERRAR", null)
            .show()
    }

    private fun mostrarDialogoCambiarEstadoPago(ped: PedidoPostgres) {
        val opcionesPago = arrayOf("🟢 PAGADO / CONFIRMADO", "🟡 PENDIENTE DE PAGO")
        val valoresPago = arrayOf("Pagado", "Pendiente")

        AlertDialog.Builder(this)
            .setTitle("Actualizar Estado de Pago - Guía ${ped.num_guia}")
            .setItems(opcionesPago) { _, which ->
                val nuevoEstadoPago = valoresPago[which]
                pbCarga.visibility = View.VISIBLE
                lifecycleScope.launch {
                    repo.actualizarEstadoPagoPedido(ped.num_guia, nuevoEstadoPago).onSuccess {
                        pbCarga.visibility = View.GONE
                        Toast.makeText(this@AdminListaPedidosActivity, "✅ Estado de Pago actualizado a '$nuevoEstadoPago'", Toast.LENGTH_SHORT).show()
                        cargarPedidosEnTiempoReal()
                    }.onFailure { e ->
                        pbCarga.visibility = View.GONE
                        Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al actualizar pago: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoAsignarMensajero(ped: PedidoPostgres) {
        pbCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            repo.obtenerMensajerosActivos().onSuccess { listaMensajeros ->
                pbCarga.visibility = View.GONE
                if (listaMensajeros.isEmpty()) {
                    Toast.makeText(this@AdminListaPedidosActivity, "⚠️ No hay domiciliarios registrados con rol 'mensajero' en Supabase", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val nombresMensajeros = listaMensajeros.map { m ->
                    "${m.nombre} (${m.placa ?: m.correo})"
                }.toTypedArray()

                AlertDialog.Builder(this@AdminListaPedidosActivity)
                    .setTitle("Seleccionar Domiciliario / Mensajero")
                    .setItems(nombresMensajeros) { _, index ->
                        val mensajeroSeleccionado = listaMensajeros[index]
                        val idOMailMensajero = mensajeroSeleccionado.correo.ifEmpty { mensajeroSeleccionado.id }
                        asignarMensajeroASupabase(ped.num_guia, idOMailMensajero)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()

            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al consultar mensajeros: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun asignarMensajeroASupabase(guia: String, idMensajero: String) {
        pbCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            repo.asignarMensajeroYEstado(guia, idMensajero, nuevoEstado = 2).onSuccess {
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "✅ Domiciliario asignado exitosamente ($idMensajero) - Estado: EN CAMINO", Toast.LENGTH_LONG).show()
                cargarPedidosEnTiempoReal()
            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al asignar domiciliario: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun abrirRotuloPdf(ped: PedidoPostgres) {
        val intentRotulo = Intent(this, RotuloActivity::class.java).apply {
            putExtra("guia", ped.num_guia)
            putExtra("id_cliente", ped.id_cliente ?: "")
            putExtra("rem_nombre", ped.rem_nombre ?: "")
            putExtra("rem_dir", ped.rem_dir ?: "")
            putExtra("dest_nombre", ped.dest_nombre ?: "")
            putExtra("dest_dir", ped.dest_dir ?: "")
            putExtra("dest_tel", ped.dest_tel ?: "")
            putExtra("dest_localidad_nom", ped.dest_localidad ?: "")
            putExtra("ped_desc", ped.descripcion ?: "")
            putExtra("ped_tipo_envio", ped.tipo_servicio ?: "")
            putExtra("ped_peso", ped.peso_kg ?: "0")
            putExtra("ped_costo", ped.costo ?: 0.0)
            putExtra("ped_pago_contraentrega", ped.modalidad_pago.equals("contraentrega", ignoreCase = true))
            putExtra("user_role", "admin")
        }
        startActivity(intentRotulo)
    }

    private fun mostrarDialogoCambiarEstado(ped: PedidoPostgres) {
        val estadosTexto = arrayOf("1. Solicitado (Pendiente)", "2. En Camino / En Ruta", "3. Entregado con Éxito", "4. Novedad / No Entregado")
        val valoresEstado = arrayOf(1, 2, 3, 4)

        AlertDialog.Builder(this)
            .setTitle("Actualizar Estado en Supabase")
            .setItems(estadosTexto) { _, which ->
                val nuevoEstado = valoresEstado[which]
                actualizarEstadoSupabase(ped.num_guia, nuevoEstado)
            }
            .show()
    }

    private fun actualizarEstadoSupabase(guia: String, nuevoEstado: Int) {
        pbCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            repo.actualizarEstadoPedido(guia, nuevoEstado).onSuccess {
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "✅ Estado actualizado a $nuevoEstado", Toast.LENGTH_SHORT).show()
                cargarPedidosEnTiempoReal()
            }.onFailure { e ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
