package com.example.nomi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
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

    fun generar(context: Context, datos: DatosRotulo): File {
        val colorMarca = ContextCompat.getColor(context, R.color.brand_primary)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(ANCHO_PT, ALTO_PT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val margen = 14f

        // 1. FRANJA DE ENCABEZADO
        val altoFranja = 85f
        canvas.drawRect(0f, 0f, ANCHO_PT.toFloat(), altoFranja, Paint().apply { color = colorMarca })

        // 2. MARCA NOMI (MAYÚSCULAS Y DESTACADO)
        val paintLogo = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
        }
        canvas.drawText("NOMI", margen, 28f, paintLogo)

        val paintSubLogo = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            alpha = 220
        }
        canvas.drawText("LOGÍSTICA Y ENVIOS", margen, 38f, paintSubLogo)

        // 3. NÚMERO DE GUÍA EN CABECERA
        val paintGuiaLabel = Paint().apply { color = Color.WHITE; textSize = 7f; alpha = 200 }
        canvas.drawText("NÚMERO DE GUÍA", margen, 56f, paintGuiaLabel)

        val paintGuia = Paint().apply {
            color = Color.WHITE
            textSize = 17f
            isFakeBoldText = true
        }
        canvas.drawText(datos.guia, margen, 73f, paintGuia)

        // 4. CÓDIGO QR AMPLIADO Y CON URL DE RASTREO
        val qrSize = 65
        // Cambia este enlace por el dominio o deep-link real de tu app
        val urlRastreo = "https://nomi.com.co/rastreo?guia=${datos.guia}"
        val qrBitmap = generarQr(urlRastreo, qrSize)

        // Fondo blanco redondeado para el QR
        val qrMarginX = ANCHO_PT - margen - qrSize - 4f
        val rectQrFondo = RectF(qrMarginX, 10f, qrMarginX + qrSize + 8f, 10f + qrSize + 8f)
        canvas.drawRoundRect(rectQrFondo, 6f, 6f, Paint().apply { color = Color.WHITE })

        // Dibujar QR cubriendo la tarjeta
        canvas.drawBitmap(qrBitmap, qrMarginX + 4f, 14f, null)

        var y = altoFranja + 18f

        // PINCELES DE CONTENIDO
        val paintLabel = Paint().apply { color = colorMarca; textSize = 8f; isFakeBoldText = true }
        val paintTexto = Paint().apply { color = Color.parseColor("#222222"); textSize = 10f }
        val paintTextoBold = Paint(paintTexto).apply { isFakeBoldText = true }
        val paintNombreGrande = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }
        val paintLinea = Paint().apply { color = Color.parseColor("#DDDDDD"); strokeWidth = 1f }

        // 5. BLOQUE DE (REMITENTE)
        canvas.drawText("DE (REMITENTE)", margen, y, paintLabel)
        y += 12f
        canvas.drawText(datos.remNombre.uppercase(), margen, y, paintTextoBold)
        y += 12f
        y = dibujarTextoMultilinea(canvas, datos.remDir, margen, y, paintTexto, ANCHO_PT - 2 * margen)
        y += 4f
        canvas.drawLine(margen, y, ANCHO_PT - margen, y, paintLinea)
        y += 14f

        // 6. BLOQUE PARA (DESTINATARIO)
        canvas.drawText("PARA (DESTINATARIO)", margen, y, paintLabel)
        y += 16f
        canvas.drawText(datos.destNombre.uppercase(), margen, y, paintNombreGrande)
        y += 14f
        y = dibujarTextoMultilinea(canvas, "Dirección: ${datos.destDir}", margen, y, paintTexto, ANCHO_PT - 2 * margen)
        if (datos.nomLocalidad.isNotEmpty()) {
            canvas.drawText("Localidad/Ciudad: ${datos.nomLocalidad}", margen, y, paintTexto)
            y += 12f
        }
        canvas.drawText("Teléfono: ${datos.destTel}", margen, y, paintTextoBold)
        y += 14f
        canvas.drawLine(margen, y, ANCHO_PT - margen, y, paintLinea)
        y += 14f

        // 7. DETALLES DE CARGA
        canvas.drawText("CONTENIDO Y DETALLES", margen, y, paintLabel)
        y += 12f
        y = dibujarTextoMultilinea(canvas, datos.descripcion, margen, y, paintTexto, ANCHO_PT - 2 * margen)
        canvas.drawText("Servicio: ${datos.tipoEnvio} | Peso: ${datos.peso} kg", margen, y, paintTexto)
        y += 18f

        // 8. PIE DE PÁGINA (ESTADO DE COBRO / CONTRAENTREGA)
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val colorFondo = if (datos.esContraentrega) ContextCompat.getColor(context, R.color.status_pending_bg) else ContextCompat.getColor(context, R.color.status_resolved_bg)
        val colorTexto = if (datos.esContraentrega) ContextCompat.getColor(context, R.color.status_pending_text) else ContextCompat.getColor(context, R.color.status_resolved_text)
        val altoBadge = 55f

        // Borde y fondo del estado de cobro
        val rectBadge = RectF(0f, y, ANCHO_PT.toFloat(), y + altoBadge)
        canvas.drawRect(rectBadge, Paint().apply { color = colorFondo })

        val centroX = ANCHO_PT / 2f
        val paintBadgeTexto = Paint().apply {
            color = colorTexto; textSize = 9f; isFakeBoldText = true; textAlign = Paint.Align.CENTER
        }
        val paintBadgeMonto = Paint().apply {
            color = colorTexto; textSize = 16f; isFakeBoldText = true; textAlign = Paint.Align.CENTER
        }

        if (datos.esContraentrega) {
            canvas.drawText("PAGO CONTRAENTREGA", centroX, y + 20f, paintBadgeTexto)
            canvas.drawText("COBRAR ${format.format(datos.costo)}", centroX, y + 40f, paintBadgeMonto)
        } else {
            canvas.drawText("PAGO INMEDIATO CONFIRMADO", centroX, y + 20f, paintBadgeTexto)
            canvas.drawText("PAGADO - NO COBRAR", centroX, y + 40f, paintBadgeMonto)
        }

        pdfDocument.finishPage(page)

        // Guardar archivo en memoria caché
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
                y += 12f
            } else {
                linea = pruebaLinea
            }
        }
        if (linea.isNotEmpty()) {
            canvas.drawText(linea, x, y, paint)
            y += 12f
        }
        return y
    }
}