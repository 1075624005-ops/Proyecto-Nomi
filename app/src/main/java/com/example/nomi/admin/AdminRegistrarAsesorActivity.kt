package com.example.nomi.admin

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminRegistrarAsesorActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_asesor)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etNombre       = findViewById<EditText>(R.id.etNombreRegA)
        val etApellido     = findViewById<EditText>(R.id.etApellidoRegA)
        val etTipoDoc      = findViewById<EditText>(R.id.etTipoDocRegA)
        val etCedula       = findViewById<EditText>(R.id.etCedulaRegA)
        val etTelefono     = findViewById<EditText>(R.id.etTelefonoRegA)
        val etCorreo       = findViewById<EditText>(R.id.etCorreoRegA)
        val etDireccion    = findViewById<EditText>(R.id.etDireccionRegA)
        val etCodigoAsesor = findViewById<EditText>(R.id.etCodigoAsesorReg)
        val etAreaAtencion = findViewById<EditText>(R.id.etAreaAtencionReg)
        val etPass         = findViewById<EditText>(R.id.etPassRegA)
        val btnReg         = findViewById<Button>(R.id.btnFinalizarRegA)
        val btnCan         = findViewById<Button>(R.id.btnCancelarRegA)

        btnCan.setOnClickListener { finish() }

        // Selector Tipo de Documento
        val opcionesDoc = arrayOf("CC - Cédula de Ciudadanía", "CE - Cédula de Extranjería", "PAS - Pasaporte", "PPT - Permiso por Protección Temporal")
        etTipoDoc.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Documento")
                .setItems(opcionesDoc) { _, which ->
                    val codigo = opcionesDoc[which].split(" - ")[0]
                    etTipoDoc.setText(codigo)
                }
                .show()
        }

        // Selector Área de Atención
        val opcionesArea = arrayOf("Servicio al Cliente", "Soporte Técnico y PQRS", "Gestión Comercial y Ventas", "Facturación y Cartera")
        etAreaAtencion.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Área de Atención")
                .setItems(opcionesArea) { _, which ->
                    etAreaAtencion.setText(opcionesArea[which])
                }
                .show()
        }

        btnReg.setOnClickListener {
            val nom       = etNombre.text.toString().trim()
            val ape       = etApellido.text.toString().trim()
            val tipDoc    = etTipoDoc.text.toString().trim()
            val ced       = etCedula.text.toString().trim()
            val tel       = etTelefono.text.toString().trim()
            val cor       = etCorreo.text.toString().trim()
            val dir       = etDireccion.text.toString().trim()
            val codAsesor = etCodigoAsesor.text.toString().trim()
            val area      = etAreaAtencion.text.toString().trim()
            val pas       = etPass.text.toString().trim()

            if (nom.isEmpty() || ape.isEmpty() || tipDoc.isEmpty() || ced.isEmpty() || tel.isEmpty() ||
                cor.isEmpty() || dir.isEmpty() || area.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos obligatorios del asesor", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (pas.length < 6) {
                Toast.makeText(this, "⚠️ La contraseña provisional debe contener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnReg.isEnabled = false
            btnReg.text = "Registrando..."

            lifecycleScope.launch {
                repo.registrarUsuario(
                    email = cor,
                    pass = pas,
                    nombre = nom,
                    apellido = ape,
                    tipoDoc = tipDoc,
                    numDoc = ced,
                    telefono = tel,
                    direccion = dir,
                    rol = "asesor",
                    codigoAsesor = if (codAsesor.isNotEmpty()) codAsesor else "ASE-${(100..999).random()}",
                    areaAtencion = area
                ).onSuccess {
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "✅ Asesor registrado con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    btnReg.isEnabled = true
                    btnReg.text = "REGISTRAR ASESOR"
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "❌ Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
