package com.example.nomi.pedidos

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DestinatarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinatario)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val scroll = findViewById<ScrollView>(R.id.scrollDestinatario)
        val container = findViewById<ViewGroup>(R.id.main_destinatario)

        // ACTIVAR AUTO-SUBIDA AL TOCAR CAMPOS
        activarAutoSubida(container, scroll)

        // 1. Recibimos los datos del remitente
        val remNombre = intent.getStringExtra("rem_nombre") ?: ""
        val remTel    = intent.getStringExtra("rem_tel") ?: ""
        val remCorreo = intent.getStringExtra("rem_correo") ?: ""
        val remDir    = intent.getStringExtra("rem_dir") ?: ""

        val etNombreDest = findViewById<EditText>(R.id.etNombreDestinatario)
        val etTelDest    = findViewById<EditText>(R.id.etTelefonoDestinatario)
        val etCorreoDest = findViewById<EditText>(R.id.etcorreoPedido)
        val etDirDest    = findViewById<EditText>(R.id.etDireccionDestinatario)
        val etLocalidad  = findViewById<EditText>(R.id.spLocalidadDestino)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteDestinatario)
        val btnVolver    = findViewById<Button>(R.id.btnVolverRemitente)

        // 2. Configurar Diálogo de Selección de Localidad de Bogotá (Sin pre-seleccionar nada)
        val localidades = arrayOf(
            "01 - Usaquén", "02 - Chapinero", "03 - Santa Fe", "04 - San Cristóbal", 
            "05 - Usme", "06 - Tunjuelito", "07 - Bosa", "08 - Kennedy", 
            "09 - Fontibón", "10 - Engativá", "11 - Suba", "12 - Barrios Unidos", 
            "13 - Teusaquillo", "14 - Los Mártires", "15 - Antonio Nariño", 
            "16 - Puente Aranda", "17 - La Candelaria", "18 - Rafael Uribe Uribe", 
            "19 - Ciudad Bolívar", "20 - Sumapaz"
        )

        etLocalidad.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Localidad de Entrega")
                .setItems(localidades) { _, which ->
                    etLocalidad.setText(localidades[which])
                }
                .show()
        }

        btnSiguiente.setOnClickListener {
            val nombreD = etNombreDest.text.toString().trim()
            val telD    = etTelDest.text.toString().trim()
            val correoD = etCorreoDest.text.toString().trim()
            val dirD    = etDirDest.text.toString().trim()
            val localidadSeleccionada = etLocalidad.text.toString().trim()

            if (nombreD.isEmpty() || telD.isEmpty() || dirD.isEmpty() || localidadSeleccionada.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los datos del destinatario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val codLocalidad = if (localidadSeleccionada.length >= 2) localidadSeleccionada.substring(0, 2) else "01"

            val intent = Intent(this, DetallesPedidoActivity::class.java)
            intent.putExtra("rem_nombre", remNombre)
            intent.putExtra("rem_tel", remTel)
            intent.putExtra("rem_correo", remCorreo)
            intent.putExtra("rem_dir", remDir)
            intent.putExtra("dest_nombre", nombreD)
            intent.putExtra("dest_tel", telD)
            intent.putExtra("dest_correo", correoD)
            intent.putExtra("dest_dir", dirD)
            intent.putExtra("dest_localidad_cod", codLocalidad)
            intent.putExtra("dest_localidad_nom", localidadSeleccionada)
            startActivity(intent)
        }

        btnVolver.setOnClickListener { finish() }
    }

    private fun activarAutoSubida(root: ViewGroup, scrollView: ScrollView) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is EditText) {
                child.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        scrollView.postDelayed({
                            val childRect = Rect()
                            v.getDrawingRect(childRect)
                            scrollView.offsetDescendantRectToMyCoords(v, childRect)
                            if (childRect.top < 250) {
                                // Si es el primer campo (Nombre) -> Se mantiene arriba sin saltos
                                scrollView.smoothScrollTo(0, 0)
                            } else {
                                // Si es un campo inferior (Teléfono, Dirección, etc.) -> Sube por encima del teclado
                                scrollView.smoothScrollTo(0, childRect.top - 80)
                            }
                        }, 200)
                    }
                }
            } else if (child is ViewGroup) {
                activarAutoSubida(child, scrollView)
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
