package com.example.nomi

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AdminRegistrarMensajeroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_mensajero)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        val etNombre = findViewById<EditText>(R.id.etNombreRegM)
        val etCedula = findViewById<EditText>(R.id.etCedulaRegM)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegM)
        val etPlaca  = findViewById<EditText>(R.id.etPlacaRegM)
        val etArea   = findViewById<EditText>(R.id.etAreaRegM)
        val etPass   = findViewById<EditText>(R.id.etPassRegM)
        val btnReg   = findViewById<Button>(R.id.btnFinalizarRegM)
        val btnCan   = findViewById<Button>(R.id.btnCancelarRegM)

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nom = etNombre.text.toString().trim()
            val ced = etCedula.text.toString().trim()
            val cor = etCorreo.text.toString().trim()
            val pla = etPlaca.text.toString().trim()
            val are = etArea.text.toString().trim()
            val pas = etPass.text.toString().trim()

            if (nom.isEmpty() || ced.isEmpty() || cor.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Nombre, Cédula, Correo y Clave son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. CREAR CUENTA EN FIREBASE AUTH
            auth.createUserWithEmailAndPassword(cor, pas)
                .addOnSuccessListener { res ->
                    val uid = res.user?.uid
                    if (uid != null) {
                        // 2. GUARDAR DATOS TÉCNICOS EN FIRESTORE
                        val datos = hashMapOf(
                            "nombre" to nom,
                            "num_doc" to ced,
                            "correo" to cor,
                            "placa" to pla,
                            "area" to are,
                            "rol" to "mensajero",
                            "estado" to "Activo"
                        )
                        
                        db.collection("usuarios").document(uid).set(datos)
                            .addOnSuccessListener {
                                Toast.makeText(this, "✅ Mensajero registrado con éxito", Toast.LENGTH_LONG).show()
                                finish()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
