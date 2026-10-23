package com.example.nomi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.nomi.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
            val v = inflater.inflate(R.layout.item_rotulo_pdf, null)

            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

            val guiaStr = datos.guia.ifEmpty { "NOMI-000000" }
            v.findViewById<TextView>(R.id.tvPdfGuia)?.text = guiaStr

            // Generar código QR para el PDF impreso
            val ivQr = v.findViewById<ImageView>(R.id.ivPdfQr)
            val qrText = "NOMI GUIA: $guiaStr\nDEST: ${datos.destNombre}\nDIR: ${datos.destDir}\nTEL: ${datos.destTel}"
            val bitmapQr = generarQrBitmap(qrText, 250)
            ivQr?.setImageBitmap(bitmapQr)

            // Remitente
            val remTexto = "Nombre: ${datos.remNombre.ifEmpty { "NOMI Express" }}\nDirección: ${datos.remDir.ifEmpty { "Bogotá D.C." }}"
            v.findViewById<TextView>(R.id.tvPdfRemitente)?.text = remTexto

            // Destinatario
            val destTexto = "Nombre: ${datos.destNombre}\nDirección: ${datos.destDir}\nLocalidad: ${datos.nomLocalidad}\nTel: ${datos.destTel}"
            v.findViewById<TextView>(R.id.tvPdfDestinatario)?.text = destTexto

            // Contenido
            val contTexto = "Descripción: ${datos.descripcion}\nServicio: ${datos.tipoEnvio} - ${datos.peso} kg"
            v.findViewById<TextView>(R.id.tvPdfContenido)?.text = contTexto

            // Pago
            val tvEstadoPago = v.findViewById<TextView>(R.id.tvPdfEstadoPago)
            val tvMontoPago = v.findViewById<TextView>(R.id.tvPdfMontoPago)
            if (datos.esContraentrega) {
                tvEstadoPago?.text = "PAGO CONTRAENTREGA"
                tvMontoPago?.text = "POR PAGAR: ${format.format(datos.costo)}"
            } else {
                tvEstadoPago?.text = "PAGO INMEDIATO (TRANSFERENCIA NEQUI)"
                tvMontoPago?.text = "PAGADO - $ 0 A COBRAR"
            }

            val widthPx = (450 * context.resources.displayMetrics.density).toInt()
            val heightPx = (650 * context.resources.displayMetrics.density).toInt()
            v.measure(
                View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
            )
            v.layout(0, 0, widthPx, heightPx)
            v
        }

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
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
}