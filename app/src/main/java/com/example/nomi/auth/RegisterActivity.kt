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
        val etApellido   = findViewById<EditText>(R.id.etApellido)
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
            val apellido  = etApellido.text.toString().trim()
            val tipoDoc   = etTipoDoc.text.toString().trim()
            val numDoc    = etCedula.text.toString().trim()
            val telefono  = etTelefono.text.toString().trim()
            val correo    = etCorreo.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val password  = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() ||tipoDoc.isEmpty() || numDoc.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos, incluyendo el Tipo de Documento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmar) {
                Toast.makeText(this, "⚠️ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
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

                val resultado = repo.registrarUsuario(
                    email = correo,
                    pass = password,
                    nombre = nombre,
                    apellido = apellido,
                    tipoDoc = tipoDoc,
                    numDoc = numDoc,
                    telefono = telefono,
                    direccion = direccion,
                    rol = "cliente"
                )

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
            TÉRMINOS Y CONDICIONES Y POLÍTICA DE TRATAMIENTO DE DATOS PERSONALES
            (Conforme a la Ley 1581 de 2012 y Decreto 1377 de 2013)
            Versión 1.0 — NOMI Express S.A.S.

            1. RESPONSABLE DEL TRATAMIENTO DE DATOS
            NOMI - Soluciones a tu alcance - S.A.S.
            NIT: 901.234.567-8 | Correo de atención: nomisas@nomi.com

            2. DATOS PERSONALES RECOLECTADOS
            Para la prestación de servicios de mensajería y transporte, recolectamos:
            • Nombres y apellidos completos
            • Tipo y número de documento de identidad (CC, NIT, CE, PAS, PPT)
            • Correo electrónico de acceso
            • Número telefónico y móvil de contacto
            • Dirección de residencia, recogida o entrega de envíos

            3. FINALIDAD EXCLUSIVA DEL TRATAMIENTO
            Sus datos personales serán utilizados única y exclusivamente para:
            • Gestionar el registro y autenticación en la aplicación NOMI
            • Generar las guías de transporte, Rótulos PDF y Códigos QR
            • Asignar domiciliarios en ruta para la entrega de pedidos
            • Tramitar y dar respuesta a solicitudes PQRS (Ley 1755 de 2015)
            • Cumplir las exigencias legales del Sector Transporte (Decreto 1079 de 2015)

            4. DERECHOS DEL TITULAR (ARTÍCULO 8 DE LA LEY 1581 DE 2012)
            Como titular de los datos personales, usted tiene derecho a:
            • Conocer, actualizar, rectificar y suprimir sus datos personales
            • Solicitar prueba de la autorización otorgada
            • Revocar la autorización en cualquier momento mediante solicitud formal
            • Presentar quejas ante la Superintendencia de Industria y Comercio (SIC)

            5. CANALES PARA EL EJERCICIO DE DERECHOS
            Puede enviar su solicitud a: nomisas@nomi.com
            Respuesta garantizada en un plazo máximo de 15 días hábiles.

            6. VIGENCIA DE LA POLÍTICA
            La presente política rige a partir de la fecha de su aceptación digital.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📋 Política de Protección de Datos (Ley 1581)")
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
