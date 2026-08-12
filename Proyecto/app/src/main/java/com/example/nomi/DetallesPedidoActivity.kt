package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
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

        val scroll = findViewById<ScrollView>(R.id.scrollDetalles)
        val rootLayout = scroll.getChildAt(0) as ViewGroup

        // --- SOLUCIÓN PARA EL TECLADO EN TODOS LOS CAMPOS ---
        configurarAutoScroll(rootLayout, scroll)

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
        format.maximumFractionDigits = 0

        // Formato moneda para Valor Declarado
        etValorDec.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etValorDec.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[^0-9]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toLong()
                            val formatted = format.format(parsed)
                            current = formatted
                            etValorDec.setText(formatted)
                            etValorDec.setSelection(formatted.length)
                        } catch (e: Exception) {}
                    } else {
                        current = ""; etValorDec.setText("")
                    }
                    etValorDec.addTextChangedListener(this)
                }
            }
        })
        btnSiguiente.setOnClickListener {
            val desc = etDesc.text.toString().trim()
            val selectedEnvioId = rgOpciones.checkedRadioButtonId

            if (desc.isEmpty()) {
                Toast.makeText(this, "Escriba qué contiene el paquete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedEnvioId == -1) {
                Toast.makeText(
                    this,
                    "Debe cotizar y elegir una opción de envío",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val selectedPagoId = rgMetodoPago.checkedRadioButtonId
            if (selectedPagoId == -1) {
                Toast.makeText(this, "Selecciona el método de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRB = findViewById<RadioButton>(selectedEnvioId)
            val costoSeleccionado = selectedRB.tag as Double

            val selectedPagoRB = findViewById<RadioButton>(selectedPagoId)
            // Detectamos por el texto del RadioButton para no depender de un ID específico.
            // IMPORTANTE: confirma que el texto de tu RadioButton de "pagar al recibir"
            // contenga la palabra "contraentrega" (sin importar mayúsculas/minúsculas).
            val esContraentrega = selectedPagoRB.text.toString().contains("contraentrega", ignoreCase = true)

            // AQUÍ ESTÁ EL TRUCO:
            val intentNext = Intent(this, FinalizarPedidoActivity::class.java)

            // 1. PASAMOS TODO LO QUE VENÍA DE LAS PANTALLAS ANTERIORES (Remitente y Destinatario)
            intent.extras?.let { intentNext.putExtras(it) }

            // 2. AGREGAMOS LO NUEVO DE ESTA PANTALLA
            intentNext.putExtra("ped_desc", desc)
            intentNext.putExtra(
                "ped_valor_dec",
                etValorDec.text.toString()
            ) // El valor que hablamos arriba
            intentNext.putExtra("ped_costo", costoSeleccionado)
            intentNext.putExtra("ped_tipo_envio", selectedRB.text.toString().split(":")[0])

            // ANTES NO SE ENVIABA: por eso FinalizarPedidoActivity siempre mostraba "0x0x0 cm"
            intentNext.putExtra("ped_ancho", etAncho.text.toString())
            intentNext.putExtra("ped_largo", etLargo.text.toString())
            intentNext.putExtra("ped_alto", etAlto.text.toString())
            intentNext.putExtra("ped_peso", etPeso.text.toString())

            // ANTES NO SE ENVIABA: por eso esContraentrega siempre quedaba en false
            intentNext.putExtra("ped_pago_contraentrega", esContraentrega)

            // 3. INICIAMOS LA ACTIVIDAD (Sin llamar a finish() para que no se cierre esta todavía)
            startActivity(intentNext)
        }
        btnCotizar.setOnClickListener {
            val ancho = etAncho.text.toString().toDoubleOrNull() ?: 0.0
            val largo = etLargo.text.toString().toDoubleOrNull() ?: 0.0
            val alto = etAlto.text.toString().toDoubleOrNull() ?: 0.0
            val pesoReal = etPeso.text.toString().toDoubleOrNull() ?: 0.0

            if (ancho > 0 && largo > 0 && alto > 0 && pesoReal > 0) {
                val pesoVol = (ancho * largo * alto) / 5000
                val pesoFinal = if (pesoReal > pesoVol) pesoReal else pesoVol

                val costoBase: Double = when {
                    pesoFinal <= 1.0 -> 10000.0
                    pesoFinal <= 2.0 -> 13000.0
                    pesoFinal <= 3.0 -> 16000.0
                    else -> 16000.0 + (ceil(pesoFinal - 3.0) * 3000.0)
                }

                rbEstandar.text = "Económico (2 dias hábiles): ${format.format(costoBase)}"
                rbExpress.text  = "Express (1 día Habile): ${format.format(costoBase * 1.4)}"
                rbPremium.text  = "Premium (Menos de 24 Horas): ${format.format(costoBase * 1.8)}"

                rgOpciones.visibility = View.VISIBLE
                rbEstandar.tag = costoBase
                rbExpress.tag  = costoBase * 1.4
                rbPremium.tag  = costoBase * 1.8

                Toast.makeText(this, "Tarifas actualizadas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ingrese medidas y peso para cotizar", Toast.LENGTH_SHORT).show()
            }
        }


    }

    /**
     * Función que recorre todos los EditText y les asigna un comportamiento
     * para que suban y queden visibles al recibir el foco.
     */
    private fun configurarAutoScroll(viewGroup: ViewGroup, scrollView: ScrollView) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is EditText) {
                child.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        scrollView.postDelayed({
                            // Obtenemos la posición del campo respecto al scroll
                            val rect = android.graphics.Rect()
                            v.getDrawingRect(rect)
                            scrollView.offsetDescendantRectToMyCoords(v, rect)
                            // Desplazamos para que el campo quede en la parte superior del teclado
                            scrollView.smoothScrollTo(0, rect.top - 100)
                        }, 300)
                    }
                }
            } else if (child is ViewGroup) {
                configurarAutoScroll(child, scrollView)
            }
        }
    }
}