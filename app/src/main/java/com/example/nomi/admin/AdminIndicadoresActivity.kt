package com.example.nomi.admin

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminIndicadoresActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    private lateinit var pbCarga: ProgressBar
    private lateinit var svIndicadores: ScrollView
    private lateinit var tvTotalPedidos: TextView
    private lateinit var tvEstadoPedidos: TextView
    private lateinit var tvTotalUsuariosInd: TextView
    private lateinit var tvDesgloseUsuarios: TextView
    private lateinit var tvTotalPQRSInd: TextView
    private lateinit var tvDesglosePQRS: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_indicadores)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        findViewById<ImageButton>(R.id.btnVolverIndicadores).setOnClickListener { finish() }

        pbCarga = findViewById(R.id.pbCargaIndicadores)
        svIndicadores = findViewById(R.id.svIndicadores)
        tvTotalPedidos = findViewById(R.id.tvTotalPedidos)
        tvEstadoPedidos = findViewById(R.id.tvEstadoPedidos)
        tvTotalUsuariosInd = findViewById(R.id.tvTotalUsuariosInd)
        tvDesgloseUsuarios = findViewById(R.id.tvDesgloseUsuarios)
        tvTotalPQRSInd = findViewById(R.id.tvTotalPQRSInd)
        tvDesglosePQRS = findViewById(R.id.tvDesglosePQRS)

        findViewById<Button>(R.id.btnExportarExcelInd).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("📈 Exportación de Reportes Excel")
                .setMessage("Generación de Reportes Estructurados:\n\nProcesando la consolidación de usuarios, pedidos y métricas globales para la descarga del informe consolidado.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        cargarIndicadoresEnTiempoReal()
    }

    private fun cargarIndicadoresEnTiempoReal() {
        pbCarga.visibility = View.VISIBLE
        svIndicadores.visibility = View.GONE

        lifecycleScope.launch {
            var totalPedidos = 0
            var pedSolicitados = 0
            var pedEnCamino = 0
            var pedEntregados = 0

            var usrClientes = 0
            var usrAsesores = 0
            var usrMensajeros = 0

            var totalPQRS = 0
            var pqrPendientes = 0
            var pqrEnProceso = 0
            var pqrResueltas = 0

            repo.obtenerPedidosAdmin().onSuccess { pedidos ->
                totalPedidos = pedidos.size
                pedSolicitados = pedidos.count { it.estado == 1 }
                pedEnCamino = pedidos.count { it.estado == 2 }
                pedEntregados = pedidos.count { it.estado == 3 }
            }

            repo.obtenerTodosLosClientes().onSuccess { clientes ->
                usrClientes = clientes.size
            }

            repo.obtenerTodosLosAsesores().onSuccess { asesores ->
                usrAsesores = asesores.size
            }

            repo.obtenerTodosLosMensajeros().onSuccess { mensajeros ->
                usrMensajeros = mensajeros.size
            }

            repo.obtenerTodasLasPQRS().onSuccess { pqrs ->
                totalPQRS = pqrs.size
                pqrPendientes = pqrs.count { it.estado.lowercase().contains("pendiente") }
                pqrEnProceso = pqrs.count { it.estado.lowercase().contains("proceso") }
                pqrResueltas = pqrs.count { it.estado.lowercase().contains("resuelt") }
            }

            val totalUsuarios = usrClientes + usrAsesores + usrMensajeros

            pbCarga.visibility = View.GONE
            svIndicadores.visibility = View.VISIBLE

            tvTotalPedidos.text = "Total Pedidos Registrados: $totalPedidos"
            tvEstadoPedidos.text = "• Solicitados / Pendientes (1): $pedSolicitados\n• En Camino (2): $pedEnCamino\n• Entregados (3): $pedEntregados"

            tvTotalUsuariosInd.text = "Total Usuarios Registrados: $totalUsuarios"
            tvDesgloseUsuarios.text = "• Clientes: $usrClientes\n• Asesores: $usrAsesores\n• Mensajeros: $usrMensajeros"

            tvTotalPQRSInd.text = "Total Radicados PQRS: $totalPQRS"
            tvDesglosePQRS.text = "• Pendientes: $pqrPendientes\n• En Proceso: $pqrEnProceso\n• Resueltas: $pqrResueltas"
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
