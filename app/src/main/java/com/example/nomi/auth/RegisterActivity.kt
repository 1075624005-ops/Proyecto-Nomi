package com.example.nomi.auth

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilCliente
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val scroll = findViewById<ScrollView>(R.id.scrollRegister)
        val container = findViewById<ViewGroup>(R.id.containerRegister)

        if (scroll != null && container != null) {
            activarAutoSubida(container, scroll)
        }

        val etNombre     = findViewById<EditText>(R.id.etNombre)
        val etApellido   = findViewById<EditText>(R.id.etApellido)
        val etTipoDoc    = findViewById<EditText>(R.id.etTipoDoc)
        val etCedula     = findViewById<EditText>(R.id.etCedula)
        val etCodigoPais = findViewById<EditText>(R.id.etCodigoPais)
        val etTelefono   = findViewById<EditText>(R.id.etTelefono)
        val etCorreo     = findViewById<EditText>(R.id.etCorreo)
        val etDireccion  = findViewById<EditText>(R.id.etDireccion)
        val etPassword   = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmar  = findViewById<EditText>(R.id.etConfirmarPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvVolver     = findViewById<TextView>(R.id.tvVolverLogin)

        val cbHabeasData  = findViewById<CheckBox>(R.id.cbHabeasData)
        val tvVerPolitica = findViewById<TextView>(R.id.tvVerPolitica)

        tvVerPolitica.setOnClickListener {
            mostrarPoliticaDatos()
        }

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
            val telefonoStr = etTelefono.text.toString().trim()
            val correo    = etCorreo.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()
            val password  = etPassword.text.toString().trim()
            val confirmar = etConfirmar.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || tipoDoc.isEmpty() || numDoc.isEmpty() || telefonoStr.isEmpty() ||
                correo.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos (Nombre, Apellido, Documento, etc.)", Toast.LENGTH_SHORT).show()
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

            val codigoPais = etCodigoPais.text.toString().trim()
            val telefonoLong = telefonoStr.toLongOrNull() ?: 0L

            lifecycleScope.launch {
                val nuevoCliente = PerfilCliente(
                    Nombre = nombre,
                    Apellido = apellido,
                    tipo_doc = tipoDoc,
                    numero_doc = numDoc,
                    Codigo_pais = if (codigoPais.isNotEmpty()) codigoPais else "+57",
                    Telefono = telefonoLong,
                    Correo_electronico = correo,
                    direccion = direccion
                )

                val resultado = repo.registrarCliente(correo, password, nuevoCliente)
                
                resultado.onSuccess {
                    Toast.makeText(this@RegisterActivity, "✅ Registro de cliente exitoso", Toast.LENGTH_LONG).show()
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
                                scrollView.smoothScrollTo(0, 0)
                            } else {
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
            POLÍTICA DE TRATAMIENTO DE DATOS PERSONALES DE NOMI S.A.S.
            
            En cumplimiento de la Ley 1581 de 2012 y el Decreto 1377 de 2013:
            
            1. FINALIDAD: Nomi recolecta sus datos personales para la prestación del servicio de envíos, mensajería y paquetes, facturación y soporte.
            
            2. DERECHOS: Como titular puede conocer, actualizar, rectificar y solicitar la supresión de sus datos personales.
            
            3. CONTACTO: Puede enviar sus solicitudes al correo nomi@nomi.com.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("📄 Política de Tratamiento de Datos")
            .setMessage(politica)
            .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
