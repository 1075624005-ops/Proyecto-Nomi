package com.example.nomi

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GenerarPQRSActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_pqrs)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        auth = Firebase.auth
        dbFirestore = Firebase.firestore

        val tipoPersona = intent.getStringExtra("tipo_persona") ?: "Natural"
        val tipoDoc     = intent.getStringExtra("tipo_doc") ?: "CC"
        val cedula      = intent.getStringExtra("cedula") ?: ""
        val nombre      = intent.getStringExtra("nombre") ?: ""
        val correo      = intent.getStringExtra("correo") ?: "anonimo@nomi.com"
        val direccion   = intent.getStringExtra("direccion") ?: ""

        val spTipoPqr   = findViewById<Spinner>(R.id.spTipoPQRS)
        val etAsunto    = findViewById<EditText>(R.id.etAsuntoPQRS)
        val etDesc      = findViewById<EditText>(R.id.etDescPQRS)
        val btnEnviar   = findViewById<Button>(R.id.btnEnviarPQRS)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarPQRS)

        // ── NUEVO: checkbox de autorización ─────────────────────
        val cbAutorizacion = findViewById<CheckBox>(R.id.cbAutorizacionPQRS)
        val tvVerAutorizacion = findViewById<TextView>(R.id.tvVerAutorizacionPQRS)

        tvVerAutorizacion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Autorización uso de datos")
                .setMessage(
                    "Al enviar esta solicitud, autoriza a Nomi a usar sus datos " +
                            "personales únicamente para gestionar y responder esta PQRS, " +
                            "conforme a la Ley 1581 de 2012 y la Ley 1755 de 2015.\n\n" +
                            "Sus datos no serán compartidos con terceros."
                )
                .setPositiveButton("Entendido y Acepto") { _, _ ->
                    cbAutorizacion.isChecked = true
                }
                .setNegativeButton("Cerrar", null)
                .show()
        }

        val opciones = arrayOf("Petición", "Queja", "Reclamo", "Sugerencia")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opciones) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(android.graphics.Color.WHITE)
                return v
            }
        }
        spTipoPqr.adapter = adapter

        btnEnviar.setOnClickListener {
            val asunto  = etAsunto.text.toString().trim()
            val desc    = etDesc.text.toString().trim()
            val tipoPqr = spTipoPqr.selectedItem.toString()

            if (asunto.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "⚠️ Completa el asunto y la descripción", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ── VALIDACIÓN AUTORIZACIÓN ──────────────────────────
            if (!cbAutorizacion.isChecked) {
                Toast.makeText(this, "⚠️ Debes autorizar el uso de tus datos para continuar", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // ── PASO 1: contar cuántas PQRS hay para sacar el consecutivo ──
            // Se bloquea el botón para evitar doble envío mientras consulta
            btnEnviar.isEnabled = false
            btnEnviar.text = "Enviando..."

            dbFirestore.collection("pqrs")
                .get()
                .addOnSuccessListener { resultado ->

                    // ── PASO 2: el consecutivo es total actual + 1 ──
                    val consecutivo = resultado.size() + 1

                    val pqrMap = hashMapOf(
                        "radicado"       to consecutivo,           // número consecutivo
                        "nombre_usuario" to nombre,
                        "correo_usuario" to correo,
                        "tipo_persona"   to tipoPersona,
                        "tipo_doc"       to tipoDoc,
                        "num_doc"        to cedula,
                        "direccion"      to direccion,
                        "tipo_pqr"       to tipoPqr,
                        "asunto"         to asunto,
                        "descripcion"    to desc,
                        "estado"         to "Pendiente",
                        "respuesta"      to "Aún no hay respuesta del administrador.",
                        "fecha"          to Timestamp.now()
                    )

                    // ── PASO 3: guardar con el consecutivo adentro ──
                    dbFirestore.collection("pqrs")
                        .add(pqrMap)
                        .addOnSuccessListener {
                            // Mostrar el número bonito con ceros: 001, 002, 003...
                            val radicadoTexto = String.format("%03d", consecutivo)

                            AlertDialog.Builder(this)
                                .setTitle("✅ Solicitud enviada")
                                .setMessage(
                                    "Su PQRS fue radicada exitosamente.\n\n" +
                                            "Número de radicado: #$radicadoTexto\n\n" +
                                            "Guarde este número para hacer seguimiento. " +
                                            "Recibirá respuesta en máximo 15 días hábiles."
                                )
                                .setCancelable(false)
                                .setPositiveButton("Aceptar") { _, _ -> finish() }
                                .show()
                        }
                        .addOnFailureListener { e ->
                            btnEnviar.isEnabled = true
                            btnEnviar.text = "ENVIAR SOLICITUD"
                            Toast.makeText(this, "❌ Error al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "ENVIAR SOLICITUD"
                    Toast.makeText(this, "❌ Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnCancelar.setOnClickListener { finish() }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}