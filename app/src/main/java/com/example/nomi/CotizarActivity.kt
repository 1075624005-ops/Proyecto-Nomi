package com.example.nomi

import android.os.Bundle
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
        val tvResultado = findViewById<TextView>(R.id.tvResultado)
        val btnCalcular = findViewById<Button>(R.id.btnCalcular)
        val etOrigen = findViewById<AutoCompleteTextView>(R.id.etOrigen)
        val etDestino = findViewById<AutoCompleteTextView>(R.id.etDestino)

        // Lista de ciudades para el auto-completado
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

                if (ancho == 0.0 || largo == 0.0 || alto == 0.0 || pesoReal == 0.0) {
                    Toast.makeText(this, "Por favor ingrese todos los valores", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val pesoVolumetrico = (ancho * largo * alto) / 5000
                etPesoVol.setText(String.format("%.2f kg", pesoVolumetrico))

                val pesoFinal = if (pesoReal > pesoVolumetrico) pesoReal else pesoVolumetrico
                
                // LÓGICA DE TARIFAS SOLICITADA
                var precio = 0.0
                if (pesoFinal <= 1.0) {
                    precio = 10000.0
                } else if (pesoFinal <= 2.0) {
                    precio = 13000.0
                } else if (pesoFinal <= 3.0) {
                    precio = 16000.0
                } else {
                    val adicional = ceil(pesoFinal - 3.0)
                    precio = 16000.0 + (adicional * 3000.0)
                }

                val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                tvResultado.text = format.format(precio)

            } catch (e: Exception) {
                Toast.makeText(this, "Ingrese valores válidos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
