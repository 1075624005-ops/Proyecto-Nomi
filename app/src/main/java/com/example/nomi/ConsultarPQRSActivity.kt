package com.example.nomi

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConsultarPQRSActivity : AppCompatActivity() {

    private lateinit var dbFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultar_pqrs)

        dbFirestore = Firebase.firestore

        val correo    = intent.getStringExtra("correo") ?: ""
        val container = findViewById<LinearLayout>(R.id.containerPQRS)
        val btnVolver = findViewById<Button>(R.id.btnVolverConsultar)

        val etBuscarRadicado = EditText(this).apply {
            hint = "Ej: 1, 2, 002 — su número de radicado"
            setTextColor(Color.BLACK)
            setBackgroundResource(android.R.drawable.editbox_background_normal)
            setPadding(30, 30, 30, 30)
        }
        container.addView(etBuscarRadicado, 0)

        etBuscarRadicado.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                val texto = etBuscarRadicado.text.toString().trim()
                if (texto.isNotEmpty()) {
                    // ── CORRECCIÓN: convertir a número para buscar el campo "radicado" ──
                    val numero = texto.toIntOrNull()
                    if (numero != null) {
                        buscarPorRadicado(numero, container, etBuscarRadicado)
                    } else {
                        Toast.makeText(this, "⚠️ Ingresa solo el número. Ej: 1, 2, 15", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cargarTodasDeLaNube(correo, container, etBuscarRadicado)
                }
                true
            } else false
        }

        cargarTodasDeLaNube(correo, container, etBuscarRadicado)
        btnVolver.setOnClickListener { finish() }
    }

    private fun cargarTodasDeLaNube(correo: String, container: LinearLayout, buscador: View) {
        container.removeAllViews()
        container.addView(buscador)

        dbFirestore.collection("pqrs")
            .whereEqualTo("correo_usuario", correo)
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    mostrarMensajeVacio(container)
                } else {
                    for (doc in documentos) {
                        renderizarTarjeta(doc.data, container)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al conectar con la nube", Toast.LENGTH_SHORT).show()
            }
    }

    // ── CORRECCIÓN: busca por el CAMPO "radicado", no por el ID del documento ──
    private fun buscarPorRadicado(numero: Int, container: LinearLayout, buscador: View) {
        container.removeAllViews()
        container.addView(buscador)

        dbFirestore.collection("pqrs")
            .whereEqualTo("radicado", numero)   // busca el campo que guardamos
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(
                        this,
                        "No se encontró el radicado #${String.format("%03d", numero)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    val correo = intent.getStringExtra("correo") ?: ""
                    cargarTodasDeLaNube(correo, container, buscador)
                } else {
                    for (doc in documentos) {
                        renderizarTarjeta(doc.data, container)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderizarTarjeta(datos: Map<String, Any>, container: LinearLayout) {
        val tipoPqr  = datos["tipo_pqr"]?.toString() ?: "PQR"
        val asunto   = datos["asunto"]?.toString() ?: "Sin asunto"
        val desc     = datos["descripcion"]?.toString() ?: ""
        val estado   = datos["estado"]?.toString() ?: "Pendiente"
        val respuesta = datos["respuesta"]?.toString() ?: "Aún no hay respuesta."

        // ── CORRECCIÓN: usar el campo "radicado" guardado, no el ID de Firestore ──
        val numRadicado = datos["radicado"]?.toString()?.toIntOrNull() ?: 0
        val radicadoTexto = String.format("%03d", numRadicado)  // muestra 001, 002, 003...

        val card = CardView(this).apply {
            val p = LinearLayout.LayoutParams(-1, -2)
            p.setMargins(0, 30, 0, 0)
            layoutParams = p
            setCardBackgroundColor(Color.parseColor("#111111"))
            radius = 15f
            setContentPadding(25, 25, 25, 25)
        }

        val lay = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val tvR = TextView(this).apply {
            text = "RADICADO #$radicadoTexto — $tipoPqr"
            setTextColor(Color.parseColor("#00AEEF"))
            setTypeface(null, Typeface.BOLD)
            textSize = 16f
        }

        val tvA = TextView(this).apply {
            text = "Asunto: $asunto"
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 0)
        }

        val tvD = TextView(this).apply {
            text = "Descripción: $desc"
            setTextColor(Color.LTGRAY)
            setPadding(0, 4, 0, 0)
        }

        val tvE = TextView(this).apply {
            text = "Estado: $estado"
            setTextColor(if (estado == "Pendiente") Color.YELLOW else Color.GREEN)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 12, 0, 0)
        }

        val tvRes = TextView(this).apply {
            text = "Respuesta: $respuesta"
            setTextColor(Color.parseColor("#00AEEF"))
            setPadding(0, 15, 0, 0)
        }

        lay.addView(tvR); lay.addView(tvA); lay.addView(tvD); lay.addView(tvE); lay.addView(tvRes)
        card.addView(lay)
        container.addView(card)
    }

    private fun mostrarMensajeVacio(container: LinearLayout) {
        val tv = TextView(this).apply {
            text = "No se encontraron solicitudes registradas."
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 50, 0, 0)
        }
        container.addView(tv)
    }
}