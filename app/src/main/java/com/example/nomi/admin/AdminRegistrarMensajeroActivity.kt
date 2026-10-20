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

class AdminRegistrarMensajeroActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_mensajero)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        val etNombre       = findViewById<EditText>(R.id.etNombreRegM)
        val etApellido     = findViewById<EditText>(R.id.etApellidoRegM)
        val etTipoDoc      = findViewById<EditText>(R.id.etTipoDocRegM)
        val etCedula       = findViewById<EditText>(R.id.etCedulaRegM)
        val etTelefono     = findViewById<EditText>(R.id.etTelefonoRegM)
        val etCorreo       = findViewById<EditText>(R.id.etCorreoRegM)
        val etDireccion    = findViewById<EditText>(R.id.etDireccionRegM)
        val etLicencia     = findViewById<EditText>(R.id.etLicenciaRegM)
        val etTipoLicencia = findViewById<EditText>(R.id.etTipoLicenciaRegM)
        val etArea         = findViewById<EditText>(R.id.etAreaRegM)
        val etPass         = findViewById<EditText>(R.id.etPassRegM)
        val btnReg         = findViewById<Button>(R.id.btnFinalizarRegM)
        val btnCan         = findViewById<Button>(R.id.btnCancelarRegM)

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

        // Selector Tipo de Licencia
        val opcionesLicencia = arrayOf("A1 - Motocicletas hasta 125cc", "A2 - Motocicletas mayores a 125cc", "B1 - Automóviles particulares", "C1 - Vehículos de servicio público")
        etTipoLicencia.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Licencia")
                .setItems(opcionesLicencia) { _, which ->
                    val codigo = opcionesLicencia[which].split(" - ")[0]
                    etTipoLicencia.setText(codigo)
                }
                .show()
        }

        btnReg.setOnClickListener {
            val nom        = etNombre.text.toString().trim()
            val ape        = etApellido.text.toString().trim()
            val tipDoc     = etTipoDoc.text.toString().trim()
            val ced        = etCedula.text.toString().trim()
            val tel        = etTelefono.text.toString().trim()
            val cor        = etCorreo.text.toString().trim()
            val dir        = etDireccion.text.toString().trim()
            val lic        = etLicencia.text.toString().trim()
            val tipLic     = etTipoLicencia.text.toString().trim()
            val zona       = etArea.text.toString().trim()
            val pas        = etPass.text.toString().trim()

            if (nom.isEmpty() || ape.isEmpty() || tipDoc.isEmpty() || ced.isEmpty() || tel.isEmpty() ||
                cor.isEmpty() || dir.isEmpty() || lic.isEmpty() || tipLic.isEmpty() || zona.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos obligatorios del mensajero", Toast.LENGTH_LONG).show()
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
                    rol = "mensajero",
                    licencia = lic,
                    tipoLicencia = tipLic,
                    zona = zona
                ).onSuccess {
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "✅ Mensajero registrado con éxito", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    btnReg.isEnabled = true
                    btnReg.text = "REGISTRAR MENSAJERO"
                    Toast.makeText(this@AdminRegistrarMensajeroActivity, "❌ Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
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
