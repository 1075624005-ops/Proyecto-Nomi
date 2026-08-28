package com.example.nomi

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        auth = Firebase.auth
        dbFirestore = Firebase.firestore

        val etNombre     = findViewById<EditText>(R.id.etNombre)
        val spTipoDoc    = findViewById<Spinner>(R.id.spTipoDoc)
        val etCedula     = findViewById<EditText>(R.id.etCedula)
        val etTelefono   = findViewById<EditText>(R.id.etTelefono)
        val etCorreo     = findViewById<EditText>(R.id.etCorreo)
        val etDireccion  = findViewById<EditText>(R.id.etDireccion)
        val etPassword   = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmar  = findViewById<EditText>(R.id.etConfirmarPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvVolver     = findViewById<TextView>(R.id.tvVolverLogin)

        // ── NUEVOS: Habeas Data ──────────────────────────────────
        val cbHabeasData  = findViewById<CheckBox>(R.id.cbHabeasData)
        val tvVerPolitica = findViewById<TextView>(R.id.tvVerPolitica)

        // Al tocar el texto, abre el diálogo con la política completa
        tvVerPolitica.setOnClickListener {
            mostrarPoliticaDatos()
        }

        // Spinner
        val opcionesDoc = arrayOf("CC", "NIT", "CE", "PT")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opcionesDoc) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.text_primary))
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as TextView).setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.text_primary))
                v.setBackgroundColor(ContextCompat.getColor(this@RegisterActivity, R.color.app_surface_card))
                return v
            }
        }
        spTipoDoc.adapter = adapter
        tvVolver.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            val nombre    = etNombre.text.toString().trim()
            val tipoDoc   = spTipoDoc.selectedItem.toString()
            val numDoc    = etCedula.text.toString().trim()
            val telefono  = etTelefono.text.toString().trim()
            val correo    = etCorreo.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val password  = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            // Validaciones normales
            if (nombre.isEmpty() || numDoc.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmar) {
                Toast.makeText(this, "⚠️ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "⚠️ La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ── VALIDACIÓN HABEAS DATA ───────────────────────────
            if (!cbHabeasData.isChecked) {
                Toast.makeText(
                    this,
                    "⚠️ Debes aceptar la Política de Tratamiento de Datos para continuar",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            // Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user?.uid

                    // Fecha y hora de aceptación (para el registro legal)
                    val fechaAceptacion = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                    ).format(Date())

                    val datosUsuario = hashMapOf(
                        "nombre"           to nombre,
                        "tipo_doc"         to tipoDoc,
                        "num_doc"          to numDoc,
                        "telefono"         to telefono,
                        "correo"           to correo,
                        "direccion"        to direccion,
                        "rol"              to "cliente",
                        // ── HABEAS DATA: queda guardado en Firestore ──
                        "habeas_data_aceptado"  to true,
                        "habeas_data_fecha"     to fechaAceptacion,
                        "habeas_data_version"   to "v1.0"
                    )

                    if (uid != null) {
                        dbFirestore.collection("usuarios")
                            .document(uid)
                            .set(datosUsuario)
                            .addOnSuccessListener {
                                Toast.makeText(this, "✅ Registro exitoso", Toast.LENGTH_LONG).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "❌ Error en Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "❌ Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // ── DIÁLOGO CON LA POLÍTICA COMPLETA ────────────────────────
    private fun mostrarPoliticaDatos() {
        val politica = """
POLÍTICA DE TRATAMIENTO DE DATOS PERSONALES
Versión 1.0 — App Nomi

1. RESPONSABLE DEL TRATAMIENTO
NOMI - Soluciones a tu alcance - S.A.S.
Correo de contacto: [nomisas@nomi.com]

2. DATOS QUE RECOLECTAMOS
- Nombre completo
- Tipo y número de documento de identidad
- Correo electrónico
- Número de teléfono
- Dirección

3. FINALIDAD DEL TRATAMIENTO
Sus datos se usan para:
- Gestionar su cuenta en la aplicación Nomi
- Procesar pedidos y PQRS
- Enviar notificaciones del servicio
- Cumplir obligaciones legales

4. DERECHOS DEL TITULAR (Ley 1581 de 2012)
Usted puede en cualquier momento:
- Conocer, actualizar y rectificar sus datos
- Solicitar la supresión de sus datos
- Revocar la autorización otorgada
- Presentar quejas ante la SIC

5. CÓMO EJERCER SUS DERECHOS
Escriba a: [nomisas@nomi.com]
Le responderemos en máximo 15 días hábiles.

6. VIGENCIA
Esta política rige a partir de su aceptación.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📋 Política de Datos Personales")
            .setMessage(politica)
            .setPositiveButton("Entendido y Acepto") { dialog, _ ->
                // Si presiona "Acepto" en el diálogo, marca el checkbox automáticamente
                findViewById<CheckBox>(R.id.cbHabeasData).isChecked = true
                dialog.dismiss()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}