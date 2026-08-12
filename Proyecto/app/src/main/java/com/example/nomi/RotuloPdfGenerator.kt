package com.example.nomi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

data class DatosRotulo(
    val guia: String,
    val remNombre: String,
    val remDir: String,
    val destNombre: String,
    val destDir: String,
    val destTel: String,
    val nomLocalidad: String,
    val descripcion: String,
    val tipoEnvio: String,
    val peso: String,
    val costo: Double,
    val esContraentrega: Boolean
)

object RotuloPdfGenerator {

    private const val ANCHO_PT = 283
    private const val ALTO_PT = 425
    private val COLOR_MARCA = Color.parseColor("#00AEEF")

    fun generar(context: Context, datos: DatosRotulo): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(ANCHO_PT, ALTO_PT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val margen = 16f

        val altoFranja = 90f
        canvas.drawRect(0f, 0f, ANCHO_PT.toFloat(), altoFranja, Paint().apply { color = COLOR_MARCA })

        val paintLogo = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
        }
        canvas.drawText("nomi", margen, 26f, paintLogo)

        val paintSubLogo = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            alpha = 200
        }
        canvas.drawText("pedidos a domicilio", margen, 38f, paintSubLogo)

        val qrSize = 55
        val qrBitmap = generarQr(datos.guia, qrSize)
        canvas.drawBitmap(qrBitmap, ANCHO_PT - margen - qrSize, 12f, null)

        val paintGuia = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            isFakeBoldText = true
        }
        canvas.drawText(datos.guia, margen, 70f, paintGuia)

        var y = altoFranja + 22f

        val paintLabel = Paint().apply { color = Color.DKGRAY; textSize = 8f }
        val paintTexto = Paint().apply { color = Color.BLACK; textSize = 11f }
        val paintTextoBold = Paint(paintTexto).apply { isFakeBoldText = true }
        val paintNombreGrande = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        val paintLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        canvas.drawText("DE", margen, y, paintLabel)
        y += 13f
        canvas.drawText(datos.remNombre, margen, y, paintTextoBold)
        y += 13f
        y = dibujarTextoMultilinea(canvas, datos.remDir, margen, y, paintTexto, ANCHO_PT - 2 * margen)
        y += 6f
        canvas.drawLine(margen, y, ANCHO_PT - margen, y, paintLinea)
        y += 16f

        canvas.drawText("PARA", margen, y, paintLabel)
        y += 18f
        canvas.drawText(datos.destNombre, margen, y, paintNombreGrande)
        y += 16f
        y = dibujarTextoMultilinea(canvas, datos.destDir, margen, y, paintTexto, ANCHO_PT - 2 * margen)
        canvas.drawText(datos.nomLocalidad, margen, y, paintTexto)
        y += 14f
        canvas.drawText("Tel: ${datos.destTel}", margen, y, paintTexto)
        y += 16f
        canvas.drawLine(margen, y, ANCHO_PT - margen, y, paintLinea)
        y += 16f

        canvas.drawText("CONTENIDO", margen, y, paintLabel)
        y += 13f
        y = dibujarTextoMultilinea(canvas, datos.descripcion, margen, y, paintTexto, ANCHO_PT - 2 * margen)
        canvas.drawText("Servicio: ${datos.tipoEnvio} - Peso: ${datos.peso} kg", margen, y, paintTexto)
        y += 20f

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val colorFondo = if (datos.esContraentrega) Color.parseColor("#FFF3CD") else Color.parseColor("#D4EDDA")
        val colorTexto = if (datos.esContraentrega) Color.parseColor("#856404") else Color.parseColor("#155724")
        val altoBadge = 55f

        canvas.drawRect(0f, y, ANCHO_PT.toFloat(), y + altoBadge, Paint().apply { color = colorFondo })

        val centroX = ANCHO_PT / 2f
        val paintBadgeTexto = Paint().apply {
            color = colorTexto; textSize = 10f; textAlign = Paint.Align.CENTER
        }
        val paintBadgeMonto = Paint().apply {
            color = colorTexto; textSize = 17f; isFakeBoldText = true; textAlign = Paint.Align.CENTER
        }

        if (datos.esContraentrega) {
            canvas.drawText("PAGO CONTRAENTREGA", centroX, y + 21f, paintBadgeTexto)
            canvas.drawText("COBRAR ${format.format(datos.costo)}", centroX, y + 41f, paintBadgeMonto)
        } else {
            canvas.drawText("PAGO INMEDIATO CONFIRMADO", centroX, y + 21f, paintBadgeTexto)
            canvas.drawText("PAGADO - NO COBRAR", centroX, y + 41f, paintBadgeMonto)
        }

        pdfDocument.finishPage(page)

        val carpeta = File(context.cacheDir, "rotulos")
        if (!carpeta.exists()) carpeta.mkdirs()
        val archivo = File(carpeta, "rotulo_${datos.guia}.pdf")
        FileOutputStream(archivo).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return archivo
    }

    private fun generarQr(contenido: String, size: Int): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (yy in 0 until size) {
                bitmap.setPixel(x, yy, if (bitMatrix[x, yy]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun dibujarTextoMultilinea(
        canvas: Canvas,
        texto: String,
        x: Float,
        yInicial: Float,
        paint: Paint,
        anchoMax: Float
    ): Float {
        val palabras = texto.split(" ")
        var linea = ""
        var y = yInicial
        for (palabra in palabras) {
            val pruebaLinea = if (linea.isEmpty()) palabra else "$linea $palabra"
            if (paint.measureText(pruebaLinea) > anchoMax && linea.isNotEmpty()) {
                canvas.drawText(linea, x, y, paint)
                linea = palabra
            } else {
                linea = pruebaLinea
            }
        }
        if (linea.isNotEmpty()) {
            canvas.drawText(linea, x, y, paint)
            y += 13f
        }
        return y
    }
}