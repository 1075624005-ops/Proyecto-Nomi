package com.example.nomi

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminMensajerosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_mensajeros)

        db = Firebase.firestore
        val container = findViewById<LinearLayout>(R.id.containerMensajeros)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarMensajero)
        val btnVolver  = findViewById<Button>(R.id.btnVolverAdmin)

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, AdminRegistrarMensajeroActivity::class.java))
        }

        btnVolver.setOnClickListener { finish() }

        cargarPlanilla(container)
    }

    private fun cargarPlanilla(container: LinearLayout) {
        db.collection("usuarios")
            .whereEqualTo("rol", "mensajero")
            .get()
            .addOnSuccessListener { documentos ->
                container.removeAllViews()
                if (documentos.isEmpty) {
                    val tv = TextView(this).apply {
                        text = "No hay mensajeros registrados."
                        setTextColor(Color.GRAY)
                        gravity = android.view.Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    container.addView(tv)
                    return@addOnSuccessListener
                }

                for (doc in documentos) {
                    val card = com.google.android.material.card.MaterialCardView(this).apply {
                        val p = LinearLayout.LayoutParams(-1, -2)
                        p.setMargins(0, 0, 0, 24)
                        layoutParams = p
                        setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_surface_card))
                        radius = 16f
                        setContentPadding(30, 30, 30, 30)
                        strokeColor = ContextCompat.getColor(context, R.color.brand_primary)
                        strokeWidth = 2
                    }

                    val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                    
                    val tvNombre = TextView(this).apply {
                        text = doc.getString("nombre")?.uppercase() ?: "SIN NOMBRE"
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        setTypeface(null, Typeface.BOLD)
                        textSize = 16f
                    }

                    val tvInfo = TextView(this).apply {
                        val area = doc.getString("area") ?: "N/A"
                        val placa = doc.getString("placa") ?: "N/A"
                        text = "Área: $area | Vehículo: $placa"
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        textSize = 13f
                        setPadding(0, 8, 0, 0)
                    }

                    layout.addView(tvNombre)
                    layout.addView(tvInfo)
                    card.addView(layout)
                    container.addView(card)
                }
            }
    }
}
