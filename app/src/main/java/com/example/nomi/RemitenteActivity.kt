package com.example.nomi

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RemitenteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remitente)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val scroll = findViewById<ScrollView>(R.id.scrollRemitente)
        val container = findViewById<ViewGroup>(R.id.main_remitente)

        // --- SOLUCIÓN DEFINITIVA: AUTO-SUBIDA INTELIGENTE ---
        activarAutoSubida(container, scroll)

        val etNombre = findViewById<EditText>(R.id.etNombreRemitente)
        val etTel = findViewById<EditText>(R.id.etTelefonoRemitente)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRemitente)
        val etDir = findViewById<EditText>(R.id.etDireccionRemitente)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteRemitente)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarRemitente)

        btnSiguiente.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tel = etTel.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val dir = etDir.text.toString().trim()

            if (nombre.isEmpty() || tel.isEmpty() || correo.isEmpty() || dir.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DestinatarioActivity::class.java)
            intent.putExtra("rem_nombre", nombre)
            intent.putExtra("rem_tel", tel)
            intent.putExtra("rem_correo", correo)
            intent.putExtra("rem_dir", dir)
            startActivity(intent)
        }

        btnCancelar.setOnClickListener { finish() }
    }

    private fun activarAutoSubida(root: ViewGroup, scrollView: ScrollView) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is EditText) {
                child.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        scrollView.postDelayed({
                            val rect = Rect()
                            v.getGlobalVisibleRect(rect)
                            
                            // Obtenemos la posición del cuadro respecto al ScrollView
                            val childRect = Rect()
                            v.getDrawingRect(childRect)
                            scrollView.offsetDescendantRectToMyCoords(v, childRect)
                            
                            // Desplazamos para que el cuadro quede en la parte superior
                            scrollView.smoothScrollTo(0, childRect.top - 100)
                        }, 300)
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
