package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RemitenteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remitente)

        // 1. Buscamos los componentes con los IDs correctos del XML
        val etNombre = findViewById<EditText>(R.id.etNombreRemitente)
        val etTel = findViewById<EditText>(R.id.etTelefonoRemitente)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRemitente)
        val etDir = findViewById<EditText>(R.id.etDireccionRemitente)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteRemitente)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarRemitente)

        // 2. Programamos el salto a la siguiente pantalla
        btnSiguiente.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tel = etTel.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val dir = etDir.text.toString().trim()

            if (nombre.isEmpty() || tel.isEmpty() || correo.isEmpty() || dir.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor llene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviamos los datos a la pantalla de Destinatario
            val intent = Intent(this, DestinatarioActivity::class.java)
            intent.putExtra("rem_nombre", nombre)
            intent.putExtra("rem_tel", tel)
            intent.putExtra("rem_correo", correo)
            intent.putExtra("rem_dir", dir)
            startActivity(intent)
        }

        // 3. Botón para volver al Panel Admin
        btnCancelar.setOnClickListener {
            finish()
        }
    }
}