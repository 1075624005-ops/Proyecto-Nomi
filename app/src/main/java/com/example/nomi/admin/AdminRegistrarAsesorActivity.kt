package com.example.nomi.admin

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilAsesor
import com.example.nomi.data.SupabaseRepository
import kotlinx.coroutines.launch

class AdminRegistrarAsesorActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_registrar_asesor)

        val scroll       = findViewById<ScrollView>(R.id.scrollRegisterA)
        val container    = scroll?.getChildAt(0) as? ViewGroup
        if (container != null && scroll != null) {
            activarAutoSubida(container, scroll)
        }

        val etNombre     = findViewById<EditText>(R.id.etNombreRegA)
        val etApellido   = findViewById<EditText>(R.id.etApellidoRegA)
        val etTipoDoc    = findViewById<EditText>(R.id.etTipoDocRegA)
        val etCedula     = findViewById<EditText>(R.id.etCedulaRegA)
        val etCodigoPais = findViewById<EditText>(R.id.etCodigoPaisRegA)
        val etTel        = findViewById<EditText>(R.id.etTelefonoRegA)
        val etCorreo     = findViewById<EditText>(R.id.etCorreoRegA)
        val etDir        = findViewById<EditText>(R.id.etDireccionRegA)
        val etCodigo     = findViewById<EditText>(R.id.etCodigoAsesorRegA)
        val etArea       = findViewById<EditText>(R.id.etAreaAtencionRegA)
        val etPass       = findViewById<EditText>(R.id.etPassRegA)

        val btnReg = findViewById<Button>(R.id.btnFinalizarRegA)
        val btnCan = findViewById<Button>(R.id.btnCancelarRegA)

        // Generar código automático consecutivo
        val codigoAuto = "AS-${(100..999).random()}"
        etCodigo.setText(codigoAuto)

        // Selectores tipo diálogo
        val opcionesDoc = arrayOf("CC - Cédula de Ciudadanía", "NIT - NIT", "CE - Cédula de Extranjería", "PAS - Pasaporte")
        etTipoDoc.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Tipo de Documento")
                .setItems(opcionesDoc) { _, which ->
                    etTipoDoc.setText(opcionesDoc[which].split(" - ")[0])
                }
                .show()
        }

        val opcionesArea = arrayOf("Servicio al Cliente", "Gestión Logística", "Soporte Técnico", "Ventas Corporativas")
        etArea.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Seleccionar Área de Atención")
                .setItems(opcionesArea) { _, which ->
                    etArea.setText(opcionesArea[which])
                }
                .show()
        }

        btnCan.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val nombre     = etNombre.text.toString().trim()
            val apellido   = etApellido.text.toString().trim()
            val tipoDoc    = etTipoDoc.text.toString().trim()
            val ced        = etCedula.text.toString().trim()
            val codigoPais = etCodigoPais.text.toString().trim()
            val telStr     = etTel.text.toString().trim()
            val cor        = etCorreo.text.toString().trim()
            val dir        = etDir.text.toString().trim()
            val cod        = etCodigo.text.toString().trim()
            val are        = etArea.text.toString().trim()
            val pas        = etPass.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || ced.isEmpty() || cor.isEmpty() || pas.isEmpty()) {
                Toast.makeText(this, "⚠️ Complete los campos obligatorios (Nombre, Apellido, Cédula, Correo y Contraseña)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val telLong = telStr.toLongOrNull() ?: 0L

            lifecycleScope.launch {
                val nuevoAsesor = PerfilAsesor(
                    Nombre = nombre,
                    Apellido = apellido,
                    tipo_doc = if (tipoDoc.isNotEmpty()) tipoDoc else "CC",
                    numero_doc = ced,
                    Codigo_pais = if (codigoPais.isNotEmpty()) codigoPais else "+57",
                    Telefono = telLong,
                    Correo_electronico = cor,
                    direccion = if (dir.isNotEmpty()) dir else "No registrada",
                    codigo_asesor = if (cod.isNotEmpty()) cod else codigoAuto,
                    area_atencion = if (are.isNotEmpty()) are else "Servicio al Cliente"
                )

                repo.registrarAsesor(cor, pas, nuevoAsesor).onSuccess {
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "✅ Asesor registrado exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@AdminRegistrarAsesorActivity, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
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
                            val scrollY = scrollView.scrollY
                            val viewTop = childRect.top
                            val viewBottom = childRect.bottom
                            val scrollHeight = scrollView.height
                            if (viewTop < scrollY || viewBottom > (scrollY + scrollHeight - 100)) {
                                scrollView.smoothScrollTo(0, maxOf(0, viewTop - 100))
                            }
                        }, 200)
                    }
                }
            } else if (child is ViewGroup) {
                activarAutoSubida(child, scrollView)
            }
        }
    }
}
