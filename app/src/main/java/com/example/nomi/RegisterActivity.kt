package com.example.nomi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        val etNombre         = findViewById<EditText>(R.id.etNombre)
        val spTipoDoc        = findViewById<Spinner>(R.id.spTipoDoc)
        val etCedula         = findViewById<EditText>(R.id.etCedula)
        val etTelefono       = findViewById<EditText>(R.id.etTelefono)
        val etCorreo         = findViewById<EditText>(R.id.etCorreo)
        val etDireccion      = findViewById<EditText>(R.id.etDireccion)
        val etPassword       = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmar      = findViewById<EditText>(R.id.etConfirmarPassword)
        val btnRegistrar     = findViewById<Button>(R.id.btnRegistrar)
        val tvVolver         = findViewById<TextView>(R.id.tvVolverLogin)

        // Opciones para el Spinner
        val opcionesDoc = arrayOf("CC", "NIT", "CE", "PT")
        
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opcionesDoc) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                v.textSize = 18f
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE)
                v.setBackgroundColor(Color.parseColor("#333333"))
                return v
            }
        }
        spTipoDoc.adapter = adapter

        tvVolver.setOnClickListener {
            finish()
        }

        btnRegistrar.setOnClickListener {
            val nombre      = etNombre.text.toString().trim()
            val tipoDoc     = spTipoDoc.selectedItem.toString()
            val numDoc      = etCedula.text.toString().trim()
            val telefono    = etTelefono.text.toString().trim()
            val correo      = etCorreo.text.toString().trim()
            val direccion   = etDireccion.text.toString().trim()
            val password    = etPassword.text.toString().trim()
            val confirmar   = etConfirmar.text.toString().trim()

            if (nombre.isEmpty() || numDoc.isEmpty() || telefono.isEmpty() || 
                correo.isEmpty() || direccion.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmar) {
                Toast.makeText(this, "⚠️ Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.existeCorreo(correo)) {
                Toast.makeText(this, "❌ El correo ya está registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exito = db.registrarUsuario(nombre, tipoDoc, numDoc, telefono, correo, direccion, password)

            if (exito) {
                Toast.makeText(this, "✅ Usuario registrado correctamente", Toast.LENGTH_LONG).show()
                // Pequeña pausa para que se vea el mensaje y luego cerrar
                btnRegistrar.postDelayed({
                    finish()
                }, 1000)
            } else {
                Toast.makeText(this, "❌ Error al registrar en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}