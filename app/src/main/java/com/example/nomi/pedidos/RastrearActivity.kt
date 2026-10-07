package com.example.nomi.pedidos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PedidoPostgres
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class RastrearActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private lateinit var containerMisEnvios: LinearLayout
    private lateinit var pbCarga: ProgressBar
    private lateinit var etBuscarGuia: EditText
    private lateinit var tvSubtitulo: TextView

    private var clientEmailSession: String = ""
    private var clientNameSession: String = ""
    private var listaMisPedidos: List<PedidoPostgres> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastrear)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        containerMisEnvios = findViewById(R.id.containerMisEnvios)
        pbCarga = findViewById(R.id.pbCargaRastreo)
        etBuscarGuia = findViewById(R.id.etBuscarGuiaClient)
        tvSubtitulo = findViewById(R.id.tvSubtituloCliente)

        // Obtener datos de la sesión del cliente
        clientEmailSession = intent.getStringExtra("correo") ?: ""
        clientNameSession = intent.getStringExtra("nombre") ?: ""

        if (clientEmailSession.isEmpty()) {
            try {
                clientEmailSession = SupabaseClient.client.auth.currentUserOrNull()?.email ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (clientEmailSession.isNotEmpty()) {
            tvSubtitulo.text = "Envíos de: $clientEmailSession"
        }

        val guiaInicial = intent.getStringExtra("guia") ?: ""
        if (guiaInicial.isNotEmpty()) {
            etBuscarGuia.setText(guiaInicial)
        }

        findViewById<Button>(R.id.btnVolverRastreo)?.setOnClickListener { finish() }

        findViewById<Button>(R.id.btnBuscarGuiaClient)?.setOnClickListener {
            val query = etBuscarGuia.text.toString().trim()
            filtrarODesplegarGuia(query)
        }

        cargarMisPedidos()
    }

    override fun onResume() {
        super.onResume()
        cargarMisPedidos()
    }

    private fun cargarMisPedidos() {
        pbCarga.visibility = View.VISIBLE
        containerMisEnvios.removeAllViews()

        lifecycleScope.launch {
            repo.obtenerPedidosAdmin().onSuccess { listaCompleta ->
                pbCarga.visibility = View.GONE
                
                // Filtrar solo los envíos que pertenecen al cliente logueado
                listaMisPedidos = if (clientEmailSession.isNotEmpty() || clientNameSession.isNotEmpty()) {
                    listaCompleta.filter { p ->
                        p.id_cliente?.equals(clientEmailSession, ignoreCase = true) == true ||
                        p.rem_nombre?.equals(clientNameSession, ignoreCase = true) == true ||
                        p.rem_nombre?.equals(clientEmailSession, ignoreCase = true) == true
                    }
                } else {
                    listaCompleta
                }

                val queryBuscador = etBuscarGuia.text.toString().trim()
                filtrarODesplegarGuia(queryBuscador)

            }.onFailure { err ->
                pbCarga.visibility = View.GONE
                Toast.makeText(this@RastrearActivity, "❌ Error al cargar mis envíos: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filtrarODesplegarGuia(query: String) {
        containerMisEnvios.removeAllViews()

        val listaAMostrar = if (query.isEmpty()) {
            listaMisPedidos
        } else {
            // Si busca por guía específica, consulta primero en sus pedidos o busca globalmente la guía
            val filtradosLocal = listaMisPedidos.filter { it.num_guia.equals(query, ignoreCase = true) || it.num_guia.lowercase().contains(query.lowercase()) }
            if (filtradosLocal.isNotEmpty()) {
                filtradosLocal
            } else {
                // Intenta búsqueda específica
                emptyList()
            }
        }

        if (listaAMostrar.isEmpty() && query.isNotEmpty()) {
            // Intentar buscar la guía específica en la base de datos
            pbCarga.visibility = View.VISIBLE
            lifecycleScope.launch {
                repo.buscarPedidoPorGuia(query).onSuccess { pedEncontrado ->
                    pbCarga.visibility = View.GONE
                    containerMisEnvios.removeAllViews()
                    if (pedEncontrado != null) {
                        agregarTarjetaPedido(pedEncontrado)
                    } else {
                        mostrarMensajeVacio("No se encontró la guía '$query' en el sistema")
                    }
                }.onFailure {
                    pbCarga.visibility = View.GONE
                    mostrarMensajeVacio("No se encontró la guía '$query'")
                }
            }
            return
        }

        if (listaAMostrar.isEmpty()) {
            mostrarMensajeVacio("Aún no tienes envíos registrados en tu historial")
            return
        }

        for (ped in listaAMostrar) {
            agregarTarjetaPedido(ped)
        }
    }

    private fun agregarTarjetaPedido(ped: PedidoPostgres) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.item_usuario_card, containerMisEnvios, false)

        val tvGuia = cardView.findViewById<TextView>(R.id.tvNombreUsuarioCard)
        val tvEstado = cardView.findViewById<TextView>(R.id.tvRolUsuarioCard)
        val tvMonto = cardView.findViewById<TextView>(R.id.tvCorreoUsuarioCard)
        val tvDestino = cardView.findViewById<TextView>(R.id.tvDocUsuarioCard)
        val tvAccion = cardView.findViewById<TextView>(R.id.tvTelUsuarioCard)

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        tvGuia.text = "📦 Guía NOMI: ${ped.num_guia}"
        tvMonto.text = "Valor Total: ${format.format(ped.costo ?: 0.0)} (${ped.modalidad_pago?.uppercase() ?: "CONTRAENTREGA"})"
        tvDestino.text = "Enviar a: ${ped.dest_nombre ?: "N/A"} - ${ped.dest_dir ?: ""}, ${ped.dest_localidad ?: ""}"

        val (textoEstado, colorEstado) = when (ped.estado) {
            1 -> Pair("● SOLICITADO (EN ESPERA)", "#00AEEF")
            2 -> Pair("● EN CAMINO / EN RUTA", "#FFC107")
            3 -> Pair("● ENTREGADO CON ÉXITO", "#28A745")
            else -> Pair("● NOVEDAD EN ENTREGA", "#DC3545")
        }

        tvEstado.text = textoEstado
        tvEstado.setTextColor(Color.parseColor(colorEstado))
        tvAccion.text = "🔍 Toca para ver Rótulo PDF y Código QR"

        cardView.setOnClickListener {
            val intentRotulo = Intent(this, RotuloActivity::class.java).apply {
                putExtra("guia", ped.num_guia)
                putExtra("id_cliente", ped.id_cliente ?: clientEmailSession)
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
                putExtra("user_role", "cliente")
            }
            startActivity(intentRotulo)
        }

        containerMisEnvios.addView(cardView)
    }

    private fun mostrarMensajeVacio(mensaje: String) {
        val tvVacio = TextView(this).apply {
            text = mensaje
            setTextColor(Color.GRAY)
            textSize = 14f
            setPadding(20, 40, 20, 20)
        }
        containerMisEnvios.addView(tvVacio)
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}