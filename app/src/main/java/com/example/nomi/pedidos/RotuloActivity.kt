package com.example.nomi.pedidos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.nomi.R
import com.example.nomi.utils.DatosRotulo
import com.example.nomi.utils.RotuloPdfGenerator
import com.example.nomi.main.HomeActivity
import java.text.NumberFormat
import java.util.Locale
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RotuloActivity : AppCompatActivity() {

    private lateinit var datos: DatosRotulo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotulo_pedido)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val guia = intent.getStringExtra("guia") ?: "-"
        val destNombre = intent.getStringExtra("dest_nombre") ?: "-"
        val destDir = intent.getStringExtra("dest_dir") ?: "-"
        val destTel = intent.getStringExtra("dest_tel") ?: "-"
        val nomLocalidad = intent.getStringExtra("dest_localidad_nom") ?: "-"
        val remNombre = intent.getStringExtra("rem_nombre") ?: "-"
        val remDir = intent.getStringExtra("rem_dir") ?: "-"
        val desc = intent.getStringExtra("ped_desc") ?: "-"
        val tipoEnvio = intent.getStringExtra("ped_tipo_envio") ?: "-"
        val peso = intent.getStringExtra("ped_peso") ?: "0"
        val costo = intent.getDoubleExtra("ped_costo", 0.0)
        val esContraentrega = intent.getBooleanExtra("ped_pago_contraentrega", true)

        datos = DatosRotulo(
                guia = guia,
                remNombre = remNombre,
                remDir = remDir,
                destNombre = destNombre,
                destDir = destDir,
                destTel = destTel,
                nomLocalidad = nomLocalidad,
                descripcion = desc,
                tipoEnvio = tipoEnvio,
                peso = peso,
                costo = costo,
                esContraentrega = esContraentrega,
                servicio = tipoEnvio,
                valorCobrar = costo.toString(),
                sloganFirebase = ""
        )

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        findViewById<TextView>(R.id.tvGuia).text = guia
        findViewById<TextView>(R.id.tvRemitente)?.text = "Nombre: $remNombre\nDirección: $remDir"
        findViewById<TextView>(R.id.tvDestinatario).text = "Nombre: $destNombre\nDirección: $destDir\nLocalidad: $nomLocalidad\nTel: $destTel"
        findViewById<TextView>(R.id.tvContenido).text = "Descripción: $desc\nServicio: $tipoEnvio - $peso kg"

        // Generar e inyectar el Código QR del Pedido
        val qrPayload = "NOMI EXPRESS\nGuia: $guia\nRemitente: $remNombre\nDestinatario: $destNombre\nDir: $destDir, $nomLocalidad\nTel: $destTel\nMonto: ${format.format(costo)}"
        val ivQr = findViewById<android.widget.ImageView>(R.id.ivQrRotulo)
        ivQr?.setImageBitmap(generarQrBitmap(qrPayload, 350))

        val tvEstadoPago = findViewById<TextView>(R.id.tvEstadoPago)
        val tvMontoPago = findViewById<TextView>(R.id.tvMontoPago)
        val btnPagarNequi = findViewById<Button>(R.id.btnPagarNequiRotulo)

        if (esContraentrega) {
            tvEstadoPago.text = "PAGO CONTRAENTREGA (COBRO EN DESTINO)"
            tvMontoPago.text = "COBRAR ${format.format(costo)}"
            btnPagarNequi?.visibility = android.view.View.GONE
        } else {
            tvEstadoPago.text = "PAGO INMEDIATO (TRANSFERENCIA NEQUI)"
            tvMontoPago.text = "POR PAGAR ${format.format(costo)}"
            btnPagarNequi?.visibility = android.view.View.VISIBLE
            btnPagarNequi?.setOnClickListener {
                val intentPago = Intent(this, PagoInmediatoActivity::class.java)
                intentPago.putExtra("guia", guia)
                intentPago.putExtra("ped_costo", costo)
                startActivity(intentPago)
            }
        }

        findViewById<Button>(R.id.btnImprimirRotulo).setOnClickListener {
            compartirRotulo()
        }

        findViewById<Button>(R.id.btnIrInicio).setOnClickListener {
            val intentHome = Intent(this, com.example.nomi.main.HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intentHome)
            finish()
        }
    }

    private fun compartirRotulo() {
        lifecycleScope.launch {
            try {
                val archivo = RotuloPdfGenerator.generar(this@RotuloActivity, datos)
                val uri: Uri = FileProvider.getUriForFile(
                    this@RotuloActivity,
                    "$packageName.fileprovider",
                    archivo
                )

                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(sendIntent, "Imprimir o compartir Rótulo PDF")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(viewIntent))

                startActivity(chooserIntent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@RotuloActivity, "Error al generar PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generarQrBitmap(contenido: String, size: Int): android.graphics.Bitmap {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(contenido, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
