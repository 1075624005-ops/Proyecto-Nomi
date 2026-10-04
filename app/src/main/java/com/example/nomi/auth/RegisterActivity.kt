package com.example.nomi.auth

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val scroll = findViewById<ScrollView>(R.id.scrollRegister)
        val container = findViewById<ViewGroup>(R.id.containerRegister)

        // ACTIVAR AUTO-SUBIDA AL TOCAR CAMPOS EN REGISTRO DE USUARIO
        activarAutoSubida(container, scroll)

        val etNombre     = findViewById<EditText>(R.id.etNombre)
        val etTipoDoc    = findViewById<EditText>(R.id.etTipoDoc)
        val etCedula     = findViewById<EditText>(R.id.etCedula)
        val etTelefono   = findViewById<EditText>(R.id.etTelefono)
        val etCorreo     = findViewById<EditText>(R.id.etCorreo)
        val etDireccion  = findViewById<EditText>(R.id.etDireccion)
        val etPassword   = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmar  = findViewById<EditText>(R.id.etConfirmarPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvVolver     = findViewById<TextView>(R.id.tvVolverLogin)

        // ── Habeas Data ──────────────────────────────────
        val cbHabeasData  = findViewById<CheckBox>(R.id.cbHabeasData)
        val tvVerPolitica = findViewById<TextView>(R.id.tvVerPolitica)

        tvVerPolitica.setOnClickListener {
            mostrarPoliticaDatos()
        }

        // Opciones ampliadas de tipo de documento
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

        tvVolver.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            val nombre    = etNombre.text.toString().trim()
            val tipoDoc   = etTipoDoc.text.toString().trim()
            val numDoc    = etCedula.text.toString().trim()
            val telefono  = etTelefono.text.toString().trim()
            val correo    = etCorreo.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val password  = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nombre.isEmpty() || tipoDoc.isEmpty() || numDoc.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos, incluyendo el Tipo de Documento", Toast.LENGTH_SHORT).show()
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

            if (!cbHabeasData.isChecked) {
                Toast.makeText(this, "⚠️ Debes aceptar la Política de Tratamiento de Datos para continuar", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // 🚀 REGISTRO CON POSTGRESQL (SUPABASE)
            lifecycleScope.launch {
                val fechaAceptacion = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                
                val nuevoUsuario = UsuarioPostgres(
                    id = "", // Se actualizará en el repositorio con el ID de Auth
                    nombre = nombre,
                    correo = correo,
                    tipo_doc = tipoDoc,
                    num_doc = numDoc,
                    telefono = telefono,
                    direccion = direccion,
                    rol = "cliente",
                    habeas_data_aceptado = true,
                    habeas_data_fecha = fechaAceptacion,
                    habeas_data_version = "v1.0"
                )

                val resultado = repo.registrarUsuario(correo, password, nuevoUsuario)
                
                resultado.onSuccess {
                    Toast.makeText(this@RegisterActivity, "✅ Registro exitoso", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { error ->
                    Toast.makeText(this@RegisterActivity, "❌ Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
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
                                // Si es un campo superior (como Nombre) -> Se mantiene arriba sin saltos
                                scrollView.smoothScrollTo(0, 0)
                            } else {
                                // Si es un campo inferior (Correo, Dirección, Contraseña, Confirmar) -> Sube por encima del teclado
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
