package com.example.nomi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Environment
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
            val v = inflater.inflate(R.layout.activity_rotulo_pedido, null)

            val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

            v.findViewById<TextView>(R.id.tvGuia)?.text = datos.guia.ifEmpty { "NOMI-000000" }
            v.findViewById<TextView>(R.id.tvRemitente)?.text =
                "Nombre: ${datos.remNombre}\nDirección: ${datos.remDir}"
            v.findViewById<TextView>(R.id.tvDestinatario)?.text =
                "Nombre: ${datos.destNombre}\nDirección: ${datos.destDir}\nLocalidad: ${datos.nomLocalidad}\nTel: ${datos.destTel}"
            v.findViewById<TextView>(R.id.tvContenido)?.text =
                "Descripción: ${datos.descripcion}\nServicio: ${datos.tipoEnvio} - ${datos.peso} kg"

            val tvEstadoPago = v.findViewById<TextView>(R.id.tvEstadoPago)
            val tvMontoPago = v.findViewById<TextView>(R.id.tvMontoPago)
            if (datos.esContraentrega) {
                tvEstadoPago?.text = "PAGO CONTRAENTREGA (COBRO EN DESTINO)"
                tvMontoPago?.text = "COBRAR ${format.format(datos.costo)}"
            } else {
                tvEstadoPago?.text = "PAGO INMEDIATO (TRANSFERENCIA NEQUI)"
                tvMontoPago?.text = "POR PAGAR ${format.format(datos.costo)}"
            }

            // Ocultar los botones de acción para que no aparezcan dentro del documento PDF impreso
            v.findViewById<View>(R.id.btnPagarNequiRotulo)?.visibility = View.GONE
            v.findViewById<View>(R.id.btnImprimirRotulo)?.visibility = View.GONE
            v.findViewById<View>(R.id.btnIrInicio)?.visibility = View.GONE

            val widthPx = (400 * context.resources.displayMetrics.density).toInt()
            val heightPx = (600 * context.resources.displayMetrics.density).toInt()
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
}
