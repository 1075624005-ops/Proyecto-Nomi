package com.example.nomi.pedidos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

class CotizarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cotizar)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

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

        // --- FORMATO DE MONEDA ---
        etValorDec.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                if (str != current) {
                    etValorDec.removeTextChangedListener(this)
                    val cleanString = str.replace("[^0-9]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                        formatter.maximumFractionDigits = 0
                        current = formatter.format(parsed)
                        etValorDec.setText(current)
                        etValorDec.setSelection(current.length)
                    }
                    etValorDec.addTextChangedListener(this)
                }
            }
        })

        btnCalcular.setOnClickListener {
            val ancho = etAncho.text.toString().toDoubleOrNull() ?: 0.0
            val largo = etLargo.text.toString().toDoubleOrNull() ?: 0.0
            val alto = etAlto.text.toString().toDoubleOrNull() ?: 0.0
            val pesoReal = etPeso.text.toString().toDoubleOrNull() ?: 0.0

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
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
