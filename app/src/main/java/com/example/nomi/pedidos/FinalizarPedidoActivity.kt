package com.example.nomi.pedidos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import java.text.NumberFormat
import java.util.*
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FinalizarPedidoActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_pedido)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        // 1. Recuperar datos del pedido
        val remNombre = intent.getStringExtra("rem_nombre") ?: "-"
        val remTel    = intent.getStringExtra("rem_tel") ?: "-"
        val remDir    = intent.getStringExtra("rem_dir") ?: "-"

        val destNombre = intent.getStringExtra("dest_nombre") ?: "-"
        val destTel    = intent.getStringExtra("dest_tel") ?: "-"
        val destDir    = intent.getStringExtra("dest_dir") ?: "-"
        val codLocalidad = intent.getStringExtra("dest_localidad_cod") ?: "00"
        val nomLocalidad = intent.getStringExtra("dest_localidad_nom") ?: "No especificada"

        val desc        = intent.getStringExtra("ped_desc") ?: "Sin descripción"
        val tipoEnvio   = intent.getStringExtra("ped_tipo_envio") ?: "Estándar"
        val ancho       = intent.getStringExtra("ped_ancho") ?: "0"
        val largo       = intent.getStringExtra("ped_largo") ?: "0"
        val alto        = intent.getStringExtra("ped_alto") ?: "0"
        val peso        = intent.getStringExtra("ped_peso") ?: "0"
        val costo       = intent.getDoubleExtra("ped_costo", 0.0)
        val esContraentrega = intent.getBooleanExtra("ped_pago_contraentrega", false)

        // 2. Vincular vistas del XML
        val tvResRem    = findViewById<TextView>(R.id.tvResumenRem)
        val tvResDest   = findViewById<TextView>(R.id.tvResumenDest)
        val tvResPedido = findViewById<TextView>(R.id.tvResumenPedido)
        val tvResPago   = findViewById<TextView>(R.id.tvResumenPago)
        val tvTotal     = findViewById<TextView>(R.id.tvTotalFinal)
        val btnFinalizar = findViewById<Button>(R.id.btnConfirmarPedido)
        val btnModificar = findViewById<Button>(R.id.btnModificarPedido)

        // 3. Poblar la información en las tarjetas
        tvResRem.text = "REMITENTE: $remNombre\nTELÉFONO: $remTel\nDIRECCIÓN: $remDir"
        tvResDest.text = "DESTINATARIO: $destNombre\nLOCALIDAD: $nomLocalidad\nDIRECCIÓN: $destDir\nTELÉFONO: $destTel"
        tvResPedido.text = "CONTENIDO: $desc\nSERVICIO: $tipoEnvio\nDIMENSIONES: ${ancho}x${largo}x${alto} cm\nPESO: $peso kg"

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val modalidadTexto = if (esContraentrega) "Pagar al recibir (Contraentrega)" else "Pago Inmediato (Transferencia/Link)"
        tvResPago.text = "MODALIDAD DE PAGO: $modalidadTexto"
        tvTotal.text = "TOTAL A PAGAR: ${format.format(costo)}"

        // 4. Confirmación Final con Registro en Supabase y Generación de Rótulo PDF / Pago
        btnFinalizar.setOnClickListener {
            btnFinalizar.isEnabled = false
            btnFinalizar.text = "Guardando pedido..."

            lifecycleScope.launch {
                var totalActuales = 0
                repo.obtenerPedidosAdmin().onSuccess { lista ->
                    totalActuales = lista.size
                }

                val codLocalidadLimpio = if (codLocalidad.length >= 2) codLocalidad.substring(0, 2) else "01"
                val numeroConsecutivo = String.format(Locale.US, "%04d", totalActuales + 1)
                val guiaGenerada = "N" + codLocalidadLimpio + numeroConsecutivo

                val nuevoPedido = com.example.nomi.data.PedidoPostgres(
                    num_guia = guiaGenerada,
                    rem_nombre = remNombre,
                    rem_tel = remTel,
                    rem_dir = remDir,
                    dest_nombre = destNombre,
                    dest_tel = destTel,
                    dest_dir = destDir,
                    dest_localidad = nomLocalidad,
                    descripcion = desc,
                    dimensiones = "${ancho}x${largo}x${alto} cm",
                    peso_kg = "$peso kg",
                    tipo_servicio = tipoEnvio,
                    modalidad_pago = if (esContraentrega) "contraentrega" else "inmediato",
                    estado = 1,
                    costo = costo
                )

                repo.crearPedido(nuevoPedido)

                Toast.makeText(this@FinalizarPedidoActivity, "✅ Pedido Creado Exitosamente\nGuía: $guiaGenerada", Toast.LENGTH_LONG).show()

                if (esContraentrega) {
                    val intentRotulo = Intent(this@FinalizarPedidoActivity, RotuloActivity::class.java)
                    intentRotulo.putExtra("guia", guiaGenerada)
                    intentRotulo.putExtra("rem_nombre", remNombre)
                    intentRotulo.putExtra("rem_dir", remDir)
                    intentRotulo.putExtra("dest_nombre", destNombre)
                    intentRotulo.putExtra("dest_dir", destDir)
                    intentRotulo.putExtra("dest_tel", destTel)
                    intentRotulo.putExtra("dest_localidad_nom", nomLocalidad)
                    intentRotulo.putExtra("ped_desc", desc)
                    intentRotulo.putExtra("ped_tipo_envio", tipoEnvio)
                    intentRotulo.putExtra("ped_peso", peso)
                    intentRotulo.putExtra("ped_costo", costo)
                    intentRotulo.putExtra("ped_pago_contraentrega", true)
                    startActivity(intentRotulo)
                } else {
                    val intentPago = Intent(this@FinalizarPedidoActivity, PagoInmediatoActivity::class.java)
                    intentPago.putExtra("guia", guiaGenerada)
                    intentPago.putExtra("ped_costo", costo)
                    startActivity(intentPago)
                }
                finish()
            }
        }

        btnModificar.setOnClickListener {
            finish()
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
