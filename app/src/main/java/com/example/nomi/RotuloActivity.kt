package com.example.nomi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.text.NumberFormat
import java.util.Locale

class RotuloActivity : AppCompatActivity() {

    private lateinit var datos: DatosRotulo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotulo_pedido)

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
                esContraentrega = esContraentrega
        )

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        findViewById<TextView>(R.id.tvGuia).text = guia
        findViewById<TextView>(R.id.tvDestinatario).text =
                "$destNombre\n$destDir\n$nomLocalidad\nTel: $destTel"
        findViewById<TextView>(R.id.tvContenido).text =
                "$desc\n$tipoEnvio - $peso kg"

        val tvEstadoPago = findViewById<TextView>(R.id.tvEstadoPago)
        val tvMontoPago = findViewById<TextView>(R.id.tvMontoPago)
        if (esContraentrega) {
            tvEstadoPago.text = "PAGO CONTRAENTREGA"
            tvMontoPago.text = "COBRAR ${format.format(costo)}"
        } else {
            tvEstadoPago.text = "PAGO INMEDIATO CONFIRMADO"
            tvMontoPago.text = "PAGADO - NO COBRAR"
        }

        findViewById<Button>(R.id.btnImprimirRotulo).setOnClickListener {
            compartirRotulo()
        }

        findViewById<Button>(R.id.btnIrInicio).setOnClickListener {
            val intentHome = Intent(this, HomeActivity::class.java)
            intentHome.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentHome)
            finish()
        }
    }

    private fun compartirRotulo() {
        val archivo = RotuloPdfGenerator.generar(this, datos)
        val uri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                archivo
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Imprimir o compartir rótulo"))
    }
}
