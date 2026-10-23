package com.example.nomi.pqrs

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nomi.R

class DatosPQRSActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pqrs)

        val scroll = findViewById<ScrollView>(R.id.scrollDatosPQR)
        val container = scroll?.getChildAt(0) as? ViewGroup
        if (container != null && scroll != null) {
            activarAutoSubida(container, scroll)
        }

        val correoIntent = intent.getStringExtra("correo") ?: ""
        val etNombrePQR = findViewById<EditText>(R.id.etNombrePQR)
        val etCorreoPQR = findViewById<EditText>(R.id.etCorreoPQR)
        val etTipoDoc   = findViewById<EditText>(R.id.etTipoDocPQR)

        if (correoIntent.isNotEmpty() && etCorreoPQR != null) {
            etCorreoPQR.setText(correoIntent)
        }

        val opcionesDoc = arrayOf(
            "CC - Cédula de Ciudadanía",
            "NIT - Número de Identificación Tributaria",
            "CE - Cédula de Extranjería",
            "PAS - Pasaporte",
            "PEP - Permiso Especial de Permanencia",
            "PPT - Permiso por Protección Temporal"
        )

        etTipoDoc.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Documento")
                .setItems(opcionesDoc) { _, which ->
                    val codigo = opcionesDoc[which].split(" - ")[0]
                    etTipoDoc.setText(codigo)
                }
                .show()
        }

        val btnSiguiente = findViewById<Button>(R.id.btnSiguientePQR)
        btnSiguiente.setOnClickListener {
            val nombreStr = etNombrePQR?.text?.toString()?.trim() ?: ""
            val correoStr = etCorreoPQR?.text?.toString()?.trim() ?: correoIntent

            val intent = Intent(this, GenerarPQRSActivity::class.java)
            intent.putExtra("nombre", nombreStr)
            intent.putExtra("correo", if (correoStr.isNotEmpty()) correoStr else "usuario.pqr@nomi.com")
            startActivity(intent)
        }
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
                                scrollView.smoothScrollTo(0, 0)
                            } else {
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
}
