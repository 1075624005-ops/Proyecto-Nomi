package com.example.nomi.pedidos

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
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
        val etDetalleDest = findViewById<EditText>(R.id.etDetalleDestinatario)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteDestinatario)
        val btnVolver    = findViewById<Button>(R.id.btnVolverRemitente)

        btnSiguiente.setOnClickListener {
            val nombreD  = etNombreDest.text.toString().trim()
            val telD     = etTelDest.text.toString().trim()
            val correoD  = etCorreoDest.text.toString().trim()
            val dirD     = etDirDest.text.toString().trim()
            val detalleD = etDetalleDest?.text?.toString()?.trim() ?: ""

            if (nombreD.isEmpty() || telD.isEmpty() || dirD.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete los datos obligatorios del destinatario (Nombre, Teléfono y Dirección)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val direccionCompleta = if (detalleD.isNotEmpty()) "$dirD, $detalleD" else dirD

            val intent = Intent(this, DetallesPedidoActivity::class.java)
            intent.putExtra("rem_nombre", remNombre)
            intent.putExtra("rem_tel", remTel)
            intent.putExtra("rem_correo", remCorreo)
            intent.putExtra("rem_dir", remDir)
            intent.putExtra("dest_nombre", nombreD)
            intent.putExtra("dest_tel", telD)
            intent.putExtra("dest_correo", correoD)
            intent.putExtra("dest_dir", direccionCompleta)
            intent.putExtra("dest_localidad_cod", "01")
            intent.putExtra("dest_localidad_nom", "Bogotá D.C.")
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
