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
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import com.example.nomi.pedidos.RotuloActivity
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
                filtrarLista("")
            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al cargar pedidos: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filtrarLista(query: String) {
        containerPedidos.removeAllViews()

        val listaFiltrada = if (query.isEmpty()) {
            listaPedidosCompleta
        } else {
            listaPedidosCompleta.filter { it.num_guia.lowercase().contains(query.lowercase()) }
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

            tvGuia.text = "📦 GUÍA: ${ped.num_guia}"
            tvMonto.text = "Valor: ${format.format(ped.costo ?: 0.0)} | Destino: ${ped.dest_localidad ?: "Bogotá"}"
            tvMensajero.text = if (ped.id_mensajero.isNullOrEmpty()) "🛵 Mensajero: Sin asignar" else "🛵 Mensajero: Asignado"

            val (textoEstado, colorEstado) = when (ped.estado) {
                1 -> Pair("SOLICITADO", "#00AEEF")
                2 -> Pair("EN CAMINO", "#FFC107")
                3 -> Pair("ENTREGADO", "#28A745")
                else -> Pair("NOVEDAD", "#DC3545")
            }

            tvEstado.text = textoEstado
            tvEstado.setTextColor(Color.parseColor(colorEstado))
            tvAccion.text = "Toca para ver resumen y asignar mensajero ➔"

            cardView.setOnClickListener {
                mostrarOpcionesPedido(ped)
            }

            containerPedidos.addView(cardView)
        }
    }

    private fun mostrarOpcionesPedido(ped: PedidoPostgres) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val resumen = "• Remitente: ${ped.rem_nombre ?: "Cliente"}\n" +
                "• Destinatario: ${ped.dest_nombre ?: "Registrado en Guía"}\n" +
                "• Dirección: ${ped.dest_dir ?: "-"} (${ped.dest_localidad ?: "Bogotá"})\n" +
                "• Contenido: ${ped.descripcion ?: "Carga General"}\n" +
                "• Valor: ${format.format(ped.costo ?: 0.0)}\n" +
                "• Modalidad: ${ped.modalidad_pago ?: "contraentrega"}"

        val opciones = arrayOf(
            "🛵 Asignar Domiciliario / Mensajero",
            "🖨️ Ver / Imprimir Rótulo PDF y QR",
            "🔄 Cambiar Estado de Entrega",
            "❌ Cancelar"
        )

        AlertDialog.Builder(this)
            .setTitle("📦 Resumen Guía: ${ped.num_guia}")
            .setMessage(resumen)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoAsignarMensajero(ped)
                    1 -> {
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
                    2 -> mostrarDialogoCambiarEstado(ped)
                }
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun mostrarDialogoAsignarMensajero(ped: PedidoPostgres) {
        pbCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            repo.obtenerTodosLosUsuarios().onSuccess { usuarios ->
                pbCarga.visibility = View.GONE
                val mensajeros = usuarios.filter { it.rol.lowercase().trim() == "mensajero" }
                if (mensajeros.isEmpty()) {
                    Toast.makeText(this@AdminListaPedidosActivity, "⚠️ No hay mensajeros registrados en el sistema", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val nombresMensajeros = mensajeros.map { m ->
                    "🛵 ${m.nombre.uppercase()} - Placa: ${m.placa ?: "Sin Placa"} (${m.area ?: "Bogotá"})"
                }.toTypedArray()

                AlertDialog.Builder(this@AdminListaPedidosActivity)
                    .setTitle("🛵 Asignar Domiciliario a Guía ${ped.num_guia}")
                    .setItems(nombresMensajeros) { _, which ->
                        val mensajeroSel = mensajeros[which]
                        asignarMensajeroEnSupabase(ped.num_guia, mensajeroSel)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }.onFailure {
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al cargar mensajeros", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun asignarMensajeroEnSupabase(guia: String, mensajero: UsuarioPostgres) {
        pbCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            repo.asignarMensajeroAPedido(guia, mensajero.id).onSuccess {
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "✅ Guía $guia asignada a ${mensajero.nombre} (En Camino)", Toast.LENGTH_LONG).show()
                cargarPedidosEnTiempoReal()
            }.onFailure { e ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@AdminListaPedidosActivity, "❌ Error al asignar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
