package com.example.nomi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

// ── IMPORTACIONES DE FIREBASE ────────────────────────────
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminPQRSDetalleActivity : AppCompatActivity() {

    private lateinit var dbFirestore: FirebaseFirestore
    private var pqrIdNube: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pqrs_detalle)

        dbFirestore = Firebase.firestore
        
        // Recibimos el ID largo de Firebase desde la lista anterior
        pqrIdNube = intent.getStringExtra("pqr_id_nube")

        val tvNombre    = findViewById<TextView>(R.id.detNombre)
        val tvTipoPers  = findViewById<TextView>(R.id.detTipoPers)
        val tvDoc       = findViewById<TextView>(R.id.detDoc)
        val tvCorreo    = findViewById<TextView>(R.id.detCorreo)
        val tvDir       = findViewById<TextView>(R.id.detDir)
        val tvDesc      = findViewById<TextView>(R.id.detDesc)
        val etRespuesta = findViewById<EditText>(R.id.etAdminRespuesta)
        val btnEnviar   = findViewById<Button>(R.id.btnAdminEnviarResp)
        val btnVolver   = findViewById<Button>(R.id.btnAdminVolver)

        // ── 1. CARGAR DETALLES DESDE LA NUBE ─────────────────────
        if (pqrIdNube != null) {
            dbFirestore.collection("pqrs").document(pqrIdNube!!)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        tvNombre.text   = "Solicitante: ${doc.getString("nombre_usuario")}"
                        tvTipoPers.text = "Tipo Persona: ${doc.getString("tipo_persona")}"
                        tvDoc.text      = "Documento: ${doc.getString("tipo_doc")} ${doc.getString("num_doc")}"
                        tvCorreo.text   = "Correo: ${doc.getString("correo_usuario")}"
                        tvDir.text      = "Dirección: ${doc.getString("direccion")}"
                        tvDesc.text     = doc.getString("descripcion")
                        
                        val respuestaActual = doc.getString("respuesta")
                        if (respuestaActual != "Aún no hay respuesta del administrador.") {
                            etRespuesta.setText(respuestaActual)
                        }
                    }
                }
        }

        // ── 2. ENVIAR RESPUESTA A LA NUBE ────────────────────────
        btnEnviar.setOnClickListener {
            val resp = etRespuesta.text.toString().trim()
            if (resp.isNotEmpty()) {
                if (pqrIdNube != null) {
                    val actualizacion = hashMapOf(
                        "respuesta" to resp,
                        "estado" to "Respondido"
                    )

                    dbFirestore.collection("pqrs").document(pqrIdNube!!)
                        .update(actualizacion as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ Respuesta enviada a la nube", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "❌ Falló la conexión", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Escriba una respuesta", Toast.LENGTH_SHORT).show()
            }
        }

        btnVolver.setOnClickListener { finish() }
    }
}