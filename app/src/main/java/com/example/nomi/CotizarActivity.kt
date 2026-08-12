package com.example.nomi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

class CotizarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cotizar)

        val etAncho = findViewById<EditText>(R.id.etAncho)
        val etLargo = findViewById<EditText>(R.id.etLargo)
        val etAlto = findViewById<EditText>(R.id.etAlto)
        val etPeso = findViewById<EditText>(R.id.etPeso)
        val etPesoVol = findViewById<EditText>(R.id.etPesoVol)
        val etValorDec = findViewById<EditText>(R.id.etValorDec)
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)
        val etOrigen = findViewById<AutoCompleteTextView>(R.id.etOrigen)
        val etDestino = findViewById<AutoCompleteTextView>(R.id.etDestino)

        // --- FORMATO DE MONEDA SEGURO ---
        etValorDec.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                if (str != current) {
                    etValorDec.removeTextChangedListener(this)
                    
                    // Limpiamos la cadena dejando solo números
                    val cleanString = str.replace("[^0-9]".toRegex(), "")
                    
                    if (cleanString.isNotEmpty()) {
                        try {
                            val parsed = cleanString.toDouble()
                            val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                            // Configuramos el formateador para que NO use decimales
                            // Esto evita que al escribir miles (ej: 2.000) el reemplazo de texto borre los ceros
                            formatter.maximumFractionDigits = 0
                            
                            val formatted = formatter.format(parsed)
                            current = formatted
                            
                            etValorDec.setText(current)
                            etValorDec.setSelection(current.length)
                        } catch (e: Exception) {
                            etValorDec.setText("")
                        }
                    } else {
                        current = ""
                        etValorDec.setText("")
                    }
                    etValorDec.addTextChangedListener(this)
                }
            }
        })

        val ciudades = arrayOf(
            "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena",
            "Soledad", "Cúcuta", "Ibagué", "Soacha", "Bucaramanga",
            "Villavicencio", "Santa Marta", "Valledupar", "Bello", "Pereira",
            "Montería", "Pastos", "Manizales", "Neiva", "Palmira"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ciudades)
        etOrigen.setAdapter(adapter)
        etDestino.setAdapter(adapter)

        btnCalcular.setOnClickListener {
            try {
                val ancho = etAncho.text.toString().toDoubleOrNull() ?: 0.0
                val largo = etLargo.text.toString().toDoubleOrNull() ?: 0.0
                val alto = etAlto.text.toString().toDoubleOrNull() ?: 0.0
                val pesoReal = etPeso.text.toString().toDoubleOrNull() ?: 0.0

                if (ancho <= 0 || largo <= 0 || alto <= 0 || pesoReal <= 0) {
                    Toast.makeText(this, "Por favor ingrese todos los valores", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val pesoVolumetrico = (ancho * largo * alto) / 5000
                etPesoVol.setText(String.format(Locale.US, "%.2f kg", pesoVolumetrico))
                val pesoFinal = if (pesoReal > pesoVolumetrico) pesoReal else pesoVolumetrico
                
                val precio = when {
                    pesoFinal <= 1.0 -> 10000.0
                    pesoFinal <= 2.0 -> 13000.0
                    pesoFinal <= 3.0 -> 16000.0
                    else -> 16000.0 + (ceil(pesoFinal - 3.0) * 3000.0)
                }

                val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                format.maximumFractionDigits = 0
                tvResultado.text = format.format(precio)
            } catch (e: Exception) {
                Toast.makeText(this, "Error en el cálculo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
