package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DestinatarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destinatario)

        // 1. Recibimos los datos del remitente
        val remNombre = intent.getStringExtra("rem_nombre") ?: ""
        val remTel    = intent.getStringExtra("rem_tel") ?: ""
        val remCorreo = intent.getStringExtra("rem_correo") ?: ""
        val remDir    = intent.getStringExtra("rem_dir") ?: ""

        val etNombreDest = findViewById<EditText>(R.id.etNombreDestinatario)
        val etTelDest    = findViewById<EditText>(R.id.etTelefonoDestinatario)
        val etCorreoDest = findViewById<EditText>(R.id.etcorreoPedido)
        val etDirDest    = findViewById<EditText>(R.id.etDireccionDestinatario)
        val spLocalidad  = findViewById<Spinner>(R.id.spLocalidadDestino)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguienteDestinatario)
        val btnVolver    = findViewById<Button>(R.id.btnVolverRemitente)

        // 2. Configurar Spinner de Localidades de Bogotá
        val localidades = arrayOf(
            "01 - Usaquén", "02 - Chapinero", "03 - Santa Fe", "04 - San Cristóbal", 
            "05 - Usme", "06 - Tunjuelito", "07 - Bosa", "08 - Kennedy", 
            "09 - Fontibón", "10 - Engativá", "11 - Suba", "12 - Barrios Unidos", 
            "13 - Teusaquillo", "14 - Los Mártires", "15 - Antonio Nariño", 
            "16 - Puente Aranda", "17 - La Candelaria", "18 - Rafael Uribe Uribe", 
            "19 - Ciudad Bolívar", "20 - Sumapaz"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, localidades)
        spLocalidad.adapter = adapter

        btnSiguiente.setOnClickListener {
            val nombreD = etNombreDest.text.toString().trim()
            val telD    = etTelDest.text.toString().trim()
            val correoD = etCorreoDest.text.toString().trim()
            val dirD    = etDirDest.text.toString().trim()
            val localidadSeleccionada = spLocalidad.selectedItem.toString()
            val codLocalidad = localidadSeleccionada.substring(0, 2) // Extrae los 2 números

            if (nombreD.isEmpty() || telD.isEmpty() || dirD.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor llene los datos del destinatario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ── PASO 3: Mandamos TODO a la pantalla de Detalles del Pedido ──
            val intent = Intent(this, DetallesPedidoActivity::class.java)
            
            // Datos Remitente
            intent.putExtra("rem_nombre", remNombre)
            intent.putExtra("rem_tel", remTel)
            intent.putExtra("rem_correo", remCorreo)
            intent.putExtra("rem_dir", remDir)
            
            // Datos Destinatario
            intent.putExtra("dest_nombre", nombreD)
            intent.putExtra("dest_tel", telD)
            intent.putExtra("dest_correo", correoD)
            intent.putExtra("dest_dir", dirD)
            intent.putExtra("dest_localidad_cod", codLocalidad)
            intent.putExtra("dest_localidad_nom", localidadSeleccionada)
            
            startActivity(intent)
        }

        btnVolver.setOnClickListener { finish() }
    }
}