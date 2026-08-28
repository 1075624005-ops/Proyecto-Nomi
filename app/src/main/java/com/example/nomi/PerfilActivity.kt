package com.example.nomi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── IMPORTACIONES DE FIREBASE ────────────────────────────
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbFirestore: FirebaseFirestore
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        // Inicializamos Firebase
        auth = Firebase.auth
        dbFirestore = Firebase.firestore
        userId = auth.currentUser?.uid // Obtenemos el ID del usuario actual

        val etNombre = findViewById<EditText>(R.id.etNombrePerfil)
        val spTipoDoc = findViewById<Spinner>(R.id.spTipoDocPerfil)
        val etNumDoc = findViewById<EditText>(R.id.etCedulaPerfil)
        val etTel = findViewById<EditText>(R.id.etTelefonoPerfil)
        val etCorreo = findViewById<EditText>(R.id.etCorreoPerfil)
        
        val tilNombre = findViewById<TextInputLayout>(R.id.tilNombre)
        val tilCedula = findViewById<TextInputLayout>(R.id.tilCedula)
        val tilTel = findViewById<TextInputLayout>(R.id.tilTel)
        val tilCorreo = findViewById<TextInputLayout>(R.id.tilCorreo)

        val btnGuardar = findViewById<Button>(R.id.btnGuardarPerfil)
        val btnVolver = findViewById<Button>(R.id.btnVolverPerfil)

        // Configuración Spinner
        val opciones = arrayOf("CC", "NIT", "CE", "PT")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                return v
            }
        }
        spTipoDoc.adapter = adapter

        // --- 1. CARGAR DATOS DESDE FIRESTORE ---
        if (userId != null) {
            dbFirestore.collection("usuarios").document(userId!!)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etNombre.setText(doc.getString("nombre"))
                        etNumDoc.setText(doc.getString("num_doc"))
                        etTel.setText(doc.getString("telefono"))
                        etCorreo.setText(doc.getString("correo"))
                        
                        val tipo = doc.getString("tipo_doc") ?: "CC"
                        val pos = when(tipo) { "NIT" -> 1; "CE" -> 2; "PT" -> 3; else -> 0 }
                        spTipoDoc.setSelection(pos)
                    }
                }
        }

        // --- 2. LÓGICA DE LOS LÁPICES (EDICIÓN) ---
        tilNombre.setEndIconOnClickListener { mostrarDialogoEditar("Editar Nombre", etNombre) }
        tilCedula.setEndIconOnClickListener { mostrarDialogoEditar("Editar Documento", etNumDoc) }
        tilTel.setEndIconOnClickListener { mostrarDialogoEditar("Editar Teléfono", etTel) }
        
        tilCorreo.setEndIconOnClickListener {
            mostrarDialogoVerificacion {
                mostrarDialogoEditar("Editar Correo", etCorreo)
            }
        }

        // --- 3. ACTUALIZAR EN FIRESTORE ---
        btnGuardar.setOnClickListener {
            if (userId == null) return@setOnClickListener

            val datosActualizados = hashMapOf(
                "nombre" to etNombre.text.toString(),
                "tipo_doc" to spTipoDoc.selectedItem.toString(),
                "num_doc" to etNumDoc.text.toString(),
                "telefono" to etTel.text.toString(),
                "correo" to etCorreo.text.toString(),
                "direccion" to "No definida" // Puedes añadir este campo si quieres
            )

            dbFirestore.collection("usuarios").document(userId!!)
                .update(datosActualizados as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "✅ Nube actualizada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "❌ Falló la conexión a la nube", Toast.LENGTH_SHORT).show()
                }
        }

        btnVolver.setOnClickListener { finish() }
    }

    private fun mostrarDialogoEditar(titulo: String, campo: EditText) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(titulo)
        val input = EditText(this)
        input.setText(campo.text.toString())
        builder.setView(input)
        builder.setPositiveButton("Hecho") { _, _ -> campo.setText(input.text.toString()) }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoVerificacion(onSuccess: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seguridad")
        builder.setMessage("Para editar el correo, ingrese el código '1234':")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("Verificar") { _, _ ->
            if (input.text.toString() == "1234") onSuccess()
            else Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}