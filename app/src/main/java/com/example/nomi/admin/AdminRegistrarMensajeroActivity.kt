package com.example.nomi.admin

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilMensajero
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.launch

class AdminRegistrarMensajeroActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_mensajero)

        val scroll = findViewById<ScrollView>(R.id.scrollRegisterM)
        val container = scroll?.getChildAt(0) as? ViewGroup
        if (container != null && scroll != null) {
            activarAutoSubida(container, scroll)
        }

        val etNombre     = findViewById<EditText>(R.id.etNombreRegM)
        val etApellido   = findViewById<EditText>(R.id.etApellidoRegM)
        val etTipoDoc    = findViewById<EditText>(R.id.etTipoDocRegM)
        val etCedula     = findViewById<EditText>(R.id.etCedulaRegM)
        val etCodigoPais = findViewById<EditText>(R.id.etCodigoPaisRegM)
        val etTelefono   = findViewById<EditText>(R.id.etTelefonoRegM)
        val etCorreo     = findViewById<EditText>(R.id.etCorreoRegM)
        val etDireccion  = findViewById<EditText>(R.id.etDireccionRegM)
        val etLicencia   = findViewById<EditText>(R.id.etLicenciaRegM)
        val etTipoLic    = findViewById<EditText>(R.id.etTipoLicRegM)
        val etZona       = findViewById<EditText>(R.id.etZonaRegM)
        val etPass       = findViewById<EditText>(R.id.etPassRegM)

        val btnReg       = findViewById<Button>(R.id.btnFinalizarRegM)
        val btnCan       = findViewById<Button>(R.id.btnCancelarRegM)

        // Selectores tipo diálogo para Tipo Doc, Licencia y Tipo Licencia
        val opcionesDoc = arrayOf("CC - Cédula de Ciudadanía", "NIT - NIT", "CE - Cédula de Extranjería", "PAS - Pasaporte")
        etTipoDoc.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Documento")
                .setItems(opcionesDoc) { _, which ->
                    etTipoDoc.setText(opcionesDoc[which].split(" - ")[0])
                }
                .show()
        }

        val opcionesLicencia = arrayOf("Sí", "No")
        etLicencia.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("¿Tiene Licencia de Conducción?")
                .setItems(opcionesLicencia) { _, which ->
                    etLicencia.setText(opcionesLicencia[which])
                }
                .show()
        }

        val opcionesTipoLic = arrayOf("A1", "A2", "B1", "B2", "C1", "C2")
        etTipoLic.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Licencia")
                .setItems(opcionesTipoLic) { _, which ->
                    etTipoLic.setText(opcionesTipoLic[which])
                }
                .show()
        }

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nombre     = etNombre.text.toString().trim()
            val apellido   = etApellido.text.toString().trim()
            val tipoDoc    = etTipoDoc.text.toString().trim()
            val numDoc     = etCedula.text.toString().trim()
            val codigoPais = etCodigoPais.text.toString().trim()
            val telStr     = etTelefono.text.toString().trim()
            val correo     = etCorreo.text.toString().trim()
            val direccion  = etDireccion.text.toString().trim()
            val licencia   = etLicencia.text.toString().trim()
            val tipoLic    = etTipoLic.text.toString().trim()
            val zona       = etZona.text.toString().trim()
            val password   = etPass.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || numDoc.isEmpty() || correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Complete los campos obligatorios (Nombre, Apellido, Documento, Correo y Contraseña)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val telefonoLong = telStr.toLongOrNull() ?: 0L

            lifecycleScope.launch {
                val nuevoM = PerfilMensajero(
                    Nombre = nombre,
                    Apellido = apellido,
                    tipo_doc = if (tipoDoc.isNotEmpty()) tipoDoc else "CC",
                    numero_doc = numDoc,
                    Codigo_pais = if (codigoPais.isNotEmpty()) codigoPais else "+57",
                    Telefono = telefonoLong,
                    Correo_electronico = correo,
                    direccion = if (direccion.isNotEmpty()) direccion else "No especificada",
                    licencia_conduccion = if (licencia.isNotEmpty()) licencia else "Sí",
                    tipo_licencia = if (tipoLic.isNotEmpty()) tipoLic else "A2",
                    zona_asignada = if (zona.isNotEmpty()) zona else "Bogotá D.C.",
                    disponible = true
                )
                
                repo.registrarMensajero(correo, password, nuevoM).onSuccess {
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "✅ Mensajero registrado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                            if (childRect.top > 200) {
                                scrollView.smoothScrollTo(0, childRect.top - 80)
                            }
                        }, 100)
                    }
                }
            } else if (child is ViewGroup) {
                activarAutoSubida(child, scrollView)
            }
        }
    }
}
