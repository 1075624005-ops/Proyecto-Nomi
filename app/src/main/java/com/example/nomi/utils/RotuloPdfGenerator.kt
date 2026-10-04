package com.example.nomi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.nomi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

data class DatosRotulo(
    val remNombre: String,
    val remDir: String,
    val destNombre: String,
    val destDir: String,
    val destTel: String,
    val peso: String,
    val servicio: String,
    val valorCobrar: String,
    val sloganFirebase: String,
    val qrBitmap: Bitmap? = null,
    val guia: String = "",
    val nomLocalidad: String = "",
    val descripcion: String = "",
    val tipoEnvio: String = "",
    val costo: Double = 0.0,
    val esContraentrega: Boolean = false
)

object RotuloPdfGenerator {

    suspend fun generar(context: Context, datos: DatosRotulo): File = withContext(Dispatchers.IO) {
        val view = withContext(Dispatchers.Main) {
            val inflater = LayoutInflater.from(context)
            val v = inflater.inflate(R.layout.pdf_rotulo_impresion, null)

            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

            v.findViewById<TextView>(R.id.tvGuiaPdf)?.text = datos.guia.ifEmpty { "NOMI-000000" }
            v.findViewById<TextView>(R.id.tvRemitentePdf)?.text =
                "Nombre: ${datos.remNombre}\nDirección: ${datos.remDir}"
            v.findViewById<TextView>(R.id.tvDestinatarioPdf)?.text =
                "Nombre: ${datos.destNombre}\nDirección: ${datos.destDir}\nLocalidad: ${datos.nomLocalidad}\nTel: ${datos.destTel}"
            v.findViewById<TextView>(R.id.tvContenidoPdf)?.text =
                "Descripción: ${datos.descripcion}\nServicio: ${datos.tipoEnvio} - ${datos.peso} kg"

            val tvEstadoPago = v.findViewById<TextView>(R.id.tvEstadoPagoPdf)
            val tvMontoPago = v.findViewById<TextView>(R.id.tvMontoPagoPdf)
            if (datos.esContraentrega) {
                tvEstadoPago?.text = "PAGO CONTRAENTREGA (COBRO EN DESTINO)"
                tvMontoPago?.text = "COBRAR: ${format.format(datos.costo)}"
            } else {
                tvEstadoPago?.text = "PAGO INMEDIATO (TRANSFERENCIA NEQUI)"
                tvMontoPago?.text = "POR PAGAR: ${format.format(datos.costo)}"
            }

            val qrPayload = "NOMI EXPRESS\nGuia: ${datos.guia}\nRemitente: ${datos.remNombre}\nDestinatario: ${datos.destNombre}\nDir: ${datos.destDir}, ${datos.nomLocalidad}\nTel: ${datos.destTel}\nMonto: ${format.format(datos.costo)}"
            val ivQr = v.findViewById<android.widget.ImageView>(R.id.ivQrPdf)
            ivQr?.setImageBitmap(generarQrBitmap(qrPayload, 320))

            // Cálculo dinámico de altura para que el PDF NUNCA se corte abajo
            val widthPx = (400 * context.resources.displayMetrics.density).toInt()
            v.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val measuredWidth = v.measuredWidth
            val measuredHeight = v.measuredHeight
            v.layout(0, 0, measuredWidth, measuredHeight)
            v
        }

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.measuredWidth, view.measuredHeight, 1).create()
        val page = document.startPage(pageInfo)
        view.draw(page.canvas)
        document.finishPage(page)

        val nombreArchivo = "Rotulo_${datos.guia.ifEmpty { "NOMI" }}.pdf"
        val file = File(context.cacheDir, nombreArchivo)

        document.writeTo(FileOutputStream(file))
        document.close()
        file
    }

    private fun generarQrBitmap(contenido: String, size: Int): Bitmap {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(contenido, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
