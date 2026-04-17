package com.example.nomi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class PerfilActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var correoOriginal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        db = DatabaseHelper(this)
        val busqueda = intent.getStringExtra("nombre") ?: ""

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

        // --- 1. CARGAR DATOS ---
        cargarDatos(busqueda, etNombre, spTipoDoc, etNumDoc, etTel, etCorreo)

        // --- 2. LÓGICA DE LOS LÁPICES (EDICIÓN) ---
        tilNombre.setEndIconOnClickListener { mostrarDialogoEditar("Editar Nombre", etNombre) }
        tilCedula.setEndIconOnClickListener { mostrarDialogoEditar("Editar Documento", etNumDoc) }
        tilTel.setEndIconOnClickListener { mostrarDialogoEditar("Editar Teléfono", etTel) }
        
        tilCorreo.setEndIconOnClickListener {
            mostrarDialogoVerificacion {
                mostrarDialogoEditar("Editar Correo", etCorreo)
            }
        }

        // --- 3. ACTUALIZAR EN BD ---
        btnGuardar.setOnClickListener {
            val exito = db.actualizarUsuario(
                correoOriginal!!,
                etNombre.text.toString(),
                spTipoDoc.selectedItem.toString(),
                etNumDoc.text.toString(),
                etTel.text.toString(),
                etCorreo.text.toString()
            )
            if (exito) {
                Toast.makeText(this, "✅ Cambios guardados de forma permanente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnVolver.setOnClickListener { finish() }
    }

    private fun cargarDatos(busqueda: String, etN: EditText, sp: Spinner, etD: EditText, etT: EditText, etC: EditText) {
        val sqlite = db.readableDatabase
        val cursor = sqlite.rawQuery("SELECT * FROM usuarios WHERE nombre = ? OR correo = ?", arrayOf(busqueda, busqueda))
        if (cursor.moveToFirst()) {
            correoOriginal = cursor.getString(cursor.getColumnIndexOrThrow("correo"))
            etN.setText(cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
            etD.setText(cursor.getString(cursor.getColumnIndexOrThrow("num_doc")))
            etT.setText(cursor.getString(cursor.getColumnIndexOrThrow("telefono")))
            etC.setText(correoOriginal)
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo_doc"))
            val pos = when(tipo) { "NIT" -> 1; "CE" -> 2; "PT" -> 3; else -> 0 }
            sp.setSelection(pos)
        }
        cursor.close()
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
}