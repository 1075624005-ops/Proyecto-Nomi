package com.example.nomi

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

// ── IMPORTACIONES DE FIREBASE ────────────────────────────
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminListaPQRSActivity : AppCompatActivity() {

    private lateinit var dbFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pqrs)

        dbFirestore = Firebase.firestore
        val container = findViewById<LinearLayout>(R.id.containerAdminPQRS)
        val btnVolver = findViewById<Button>(R.id.btnVolverLista)

        cargarListaDesdeFirebase(container)

        btnVolver.setOnClickListener { finish() }
    }

    private fun cargarListaDesdeFirebase(container: LinearLayout) {
        // Consultamos todas las PQRS de la colección en la nube
        dbFirestore.collection("pqrs")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documentos ->
                container.removeAllViews()
                
                if (documentos.isEmpty) {
                    val tv = TextView(this)
                    tv.text = "No hay PQRS registradas en la nube."
                    tv.setTextColor(Color.GRAY)
                    tv.gravity = android.view.Gravity.CENTER
                    container.addView(tv)
                    return@addOnSuccessListener
                }

                for (doc in documentos) {
                    val idDoc = doc.id
                    val asunto = doc.getString("asunto") ?: "Sin asunto"
                    val estado = doc.getString("estado") ?: "Pendiente"
                    val nombre = doc.getString("nombre_usuario") ?: "Usuario"

                    val card = CardView(this).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 32)
                        layoutParams = p
                        setCardBackgroundColor(Color.parseColor("#111111"))
                        radius = 15f
                        setContentPadding(25, 25, 25, 25)
                        isClickable = true
                        isFocusable = true
                    }

                    val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

                    val tvRad = TextView(this).apply {
                        text = "RADICADO #${idDoc.take(6).uppercase()} - $nombre"
                        setTextColor(Color.parseColor("#00AEEF"))
                        setTypeface(null, Typeface.BOLD)
                    }

                    val tvAsu = TextView(this).apply {
                        text = "Asunto: $asunto"
                        setTextColor(Color.WHITE)
                    }

                    val tvEst = TextView(this).apply {
                        text = "Estado: $estado"
                        setTextColor(if (estado == "Pendiente") Color.YELLOW else Color.GREEN)
                    }

                    layout.addView(tvRad)
                    layout.addView(tvAsu)
                    layout.addView(tvEst)
                    card.addView(layout)

                    // Al tocar la tarjeta, pasamos el ID real de Firebase
                    card.setOnClickListener {
                        val intent = Intent(this, AdminPQRSDetalleActivity::class.java)
                        intent.putExtra("pqr_id_nube", idDoc)
                        startActivity(intent)
                    }

                    container.addView(card)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos de la nube", Toast.LENGTH_SHORT).show()
            }
    }
}