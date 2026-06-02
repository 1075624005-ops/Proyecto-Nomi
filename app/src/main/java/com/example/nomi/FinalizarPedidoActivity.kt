package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.*

class FinalizarPedidoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finalizar_pedido)

        db = FirebaseFirestore.getInstance()

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
        
        tvResPedido.text = "CONTENIDO: $desc\n" +
                           "SERVICIO: $tipoEnvio\n" +
                           "DIMENSIONES: ${ancho}x${largo}x${alto} cm\n" +
                           "PESO: $peso kg"

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        val modalidadTexto = if (esContraentrega) "Pagar al recibir (Contraentrega)" else "Pago Inmediato (Transferencia/Link)"
        tvResPago.text = "MODALIDAD DE PAGO: $modalidadTexto"
        tvTotal.text = "TOTAL A PAGAR: ${format.format(costo)}"

        // 4. Confirmación Final
        btnFinalizar.setOnClickListener {
            btnFinalizar.isEnabled = false
            btnFinalizar.text = "PROCESANDO..."

            // Generar número de guía dinámico por localidad
            db.collection("pedidos")
                .whereEqualTo("cod_localidad", codLocalidad)
                .get()
                .addOnSuccessListener { documents ->
                    val consecutivo = documents.size() + 1
                    val guiaGenerada = "N-$codLocalidad-${String.format("%06d", consecutivo)}"

                    val pedidoMap = hashMapOf(
                        "guia" to guiaGenerada,
                        "rem_nombre" to remNombre,
                        "rem_dir" to remDir,
                        "dest_nombre" to destNombre,
                        "dest_dir" to destDir,
                        "cod_localidad" to codLocalidad,
                        "nom_localidad" to nomLocalidad,
                        "descripcion" to desc,
                        "tipo_envio" to tipoEnvio,
                        "costo" to costo,
                        "contraentrega" to esContraentrega,
                        "estado" to "Pendiente",
                        "fecha" to Calendar.getInstance().time
                    )

                    db.collection("pedidos")
                        .add(pedidoMap)
                        .addOnSuccessListener {
                            mostrarMensajeExito(guiaGenerada)
                        }
                        .addOnFailureListener {
                            btnFinalizar.isEnabled = true
                            btnFinalizar.text = "CONFIRMAR PEDIDO"
                            Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    btnFinalizar.isEnabled = true
                    btnFinalizar.text = "CONFIRMAR PEDIDO"
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
                }
        }

        btnModificar.setOnClickListener {
            finish() // Regresa a DetallesPedidoActivity
        }
    }

    private fun mostrarMensajeExito(guia: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("✅ PEDIDO REGISTRADO")
        builder.setMessage("Su pedido ha sido creado con éxito.\n\nNúmero de Guía:\n$guia\n\nEl domiciliario se pondrá en contacto pronto.")
        builder.setCancelable(false)
        builder.setPositiveButton("ACEPTAR") { _, _ ->
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        builder.show()
    }
}
