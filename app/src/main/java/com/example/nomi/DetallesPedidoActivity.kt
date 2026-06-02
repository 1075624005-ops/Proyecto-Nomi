package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

class DetallesPedidoActivity : AppCompatActivity() {

    private var costoSeleccionado: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_pedido)

        // 1. Datos previos
        val extras = intent.extras
        val remNombre = intent.getStringExtra("rem_nombre") ?: ""
        val remDir    = intent.getStringExtra("rem_dir") ?: ""
        val destNombre = intent.getStringExtra("dest_nombre") ?: ""
        val destDir    = intent.getStringExtra("dest_dir") ?: ""

        findViewById<TextView>(R.id.tvInfoRemitente).text = "Rem: $remNombre\nDir: $remDir"
        findViewById<TextView>(R.id.tvInfoDestinatario).text = "Dest: $destNombre\nDir: $destDir"

        // 2. Referencias
        val etDesc = findViewById<EditText>(R.id.etDescripcionPedido)
        val etAncho = findViewById<EditText>(R.id.etAnchoPedido)
        val etLargo = findViewById<EditText>(R.id.etLargoPedido)
        val etAlto = findViewById<EditText>(R.id.etAltoPedido)
        val etPeso = findViewById<EditText>(R.id.etPesoPedido)
        val etValorDec = findViewById<EditText>(R.id.etValorDeclarado)
        
        val btnCotizar = findViewById<Button>(R.id.btnCotizarInterno)
        val rgOpciones = findViewById<RadioGroup>(R.id.rgOpcionesEnvio)
        val rbEstandar = findViewById<RadioButton>(R.id.rbEstandar)
        val rbExpress = findViewById<RadioButton>(R.id.rbExpress)
        val rbPremium = findViewById<RadioButton>(R.id.rbPremium)
        
        val rgMetodoPago = findViewById<RadioGroup>(R.id.rgMetodoPago)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteDetalles)

        val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        // 3. Lógica de Cotización (3 Opciones con Tabla de Valores)
        btnCotizar.setOnClickListener {
            val ancho = etAncho.text.toString().toDoubleOrNull() ?: 0.0
            val largo = etLargo.text.toString().toDoubleOrNull() ?: 0.0
            val alto = etAlto.text.toString().toDoubleOrNull() ?: 0.0
            val pesoReal = etPeso.text.toString().toDoubleOrNull() ?: 0.0

            if (ancho > 0 && largo > 0 && alto > 0 && pesoReal > 0) {
                val pesoVol = (ancho * largo * alto) / 5000
                val pesoFinal = if (pesoReal > pesoVol) pesoReal else pesoVol
                
                // --- LÓGICA DE TARIFAS SOLICITADA ---
                val costoBase: Double = when {
                    pesoFinal <= 1.0 -> 10000.0
                    pesoFinal <= 2.0 -> 13000.0
                    pesoFinal <= 3.0 -> 16000.0
                    else -> 16000.0 + (ceil(pesoFinal - 3.0) * 3000.0)
                }

                val estandar = costoBase
                val express  = costoBase * 1.4
                val premium  = costoBase * 1.8

                rbEstandar.text = "Económico (2 dias hábiles): ${format.format(estandar)}"
                rbExpress.text  = "Express (1 día Habile): ${format.format(express)}"
                rbPremium.text  = "Premium (Menos de 24 Horas): ${format.format(premium)}"

                rgOpciones.visibility = View.VISIBLE
                rbEstandar.tag = estandar
                rbExpress.tag  = express
                rbPremium.tag  = premium
                
                Toast.makeText(this, "Tarifas actualizadas según el peso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ingrese medidas y peso para cotizar", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Ir al Resumen
        btnSiguiente.setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val selectedEnvioId = rgOpciones.checkedRadioButtonId

            if (desc.isEmpty()) {
                Toast.makeText(this, "Escriba qué contiene el paquete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedEnvioId == -1) {
                Toast.makeText(this, "Debe cotizar y elegir una opción de envío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRB = findViewById<RadioButton>(selectedEnvioId)
            costoSeleccionado = selectedRB.tag as Double

            // Determinar si es contraentrega
            val esContraentrega = rgMetodoPago.checkedRadioButtonId == R.id.rbPagoContraentrega

            val intentNext = Intent(this, FinalizarPedidoActivity::class.java)
            if (extras != null) intentNext.putExtras(extras)
            
            intentNext.putExtra("ped_desc", desc)
            intentNext.putExtra("ped_ancho", etAncho.text.toString())
            intentNext.putExtra("ped_largo", etLargo.text.toString())
            intentNext.putExtra("ped_alto", etAlto.text.toString())
            intentNext.putExtra("ped_peso", etPeso.text.toString())
            intentNext.putExtra("ped_valor_dec", etValorDec.text.toString())
            intentNext.putExtra("ped_costo", costoSeleccionado)
            intentNext.putExtra("ped_pago_contraentrega", esContraentrega)
            intentNext.putExtra("ped_tipo_envio", selectedRB.text.toString().split(":")[0])

            startActivity(intentNext)
        }
    }
}
