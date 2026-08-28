package com.example.nomi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.NumberFormat
import java.util.Locale
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CuentaPagoConfig {
    const val TITULAR = "Nomi App"
    const val NUMERO = "300 000 0000"
    const val ENTIDAD = "Nequi"
    const val WHATSAPP_NEGOCIO = "573000000000"
}

class PagoInmediatoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago_inmediato)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        db = FirebaseFirestore.getInstance()

        val pedidoId = intent.getStringExtra("pedido_id") ?: ""
        val guia = intent.getStringExtra("guia") ?: "-"
        val costo = intent.getDoubleExtra("ped_costo", 0.0)
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        findViewById<TextView>(R.id.tvGuiaPago).text = guia
        findViewById<TextView>(R.id.tvMontoAPagar).text = format.format(costo)
        findViewById<TextView>(R.id.tvTitularCuenta).text = CuentaPagoConfig.TITULAR
        findViewById<TextView>(R.id.tvNumeroCuenta).text = CuentaPagoConfig.NUMERO
        findViewById<TextView>(R.id.tvEntidadCuenta).text = CuentaPagoConfig.ENTIDAD

        val qrPayload = "${CuentaPagoConfig.ENTIDAD}: ${CuentaPagoConfig.NUMERO}\nPedido: $guia\nValor: ${format.format(costo)}"
        findViewById<ImageView>(R.id.ivQrPago).setImageBitmap(generarQr(qrPayload, 400))

        findViewById<Button>(R.id.btnCopiarNumero).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("numero_pago", CuentaPagoConfig.NUMERO))
            Toast.makeText(this, "Número copiado", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnEnviarWhatsapp).setOnClickListener {
            enviarComprobanteWhatsapp(guia, costo)
        }

        findViewById<Button>(R.id.btnYaTransferi).setOnClickListener {
            confirmarTransferencia(pedidoId)
        }

        findViewById<Button>(R.id.btnCancelarPago).setOnClickListener { finish() }
    }

    private fun enviarComprobanteWhatsapp(guia: String, costo: Double) {
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val mensaje = "Hola, ya hice la transferencia del pedido $guia por ${format.format(costo)}. Adjunto el comprobante 📎"
        val url = "https://wa.me/${CuentaPagoConfig.WHATSAPP_NEGOCIO}?text=${Uri.encode(mensaje)}"

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarTransferencia(pedidoId: String) {
        val intentRotulo = Intent(this, RotuloActivity::class.java)
        intentRotulo.putExtras(intent)

        if (pedidoId.isEmpty()) {
            startActivity(intentRotulo)
            finish()
            return
        }

        db.collection("pedidos").document(pedidoId)
            .update("estado_pago", "Pendiente de verificación")
            .addOnCompleteListener {
                startActivity(intentRotulo)
                finish()
            }
    }

    private fun generarQr(contenido: String, size: Int): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
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