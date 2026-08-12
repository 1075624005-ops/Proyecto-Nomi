package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class PQRSMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pqrs_menu)

        val btnIrGenerar = findViewById<CardView>(R.id.btnIrGenerar)
        val btnIrConsultar = findViewById<CardView>(R.id.btnIrConsultar)
        val btnVolver = findViewById<Button>(R.id.btnVolverPQRS)
        
        val correoUsuario = intent.getStringExtra("correo")

        btnIrGenerar.setOnClickListener {
            val intent = Intent(this, DatosPQRSActivity::class.java)
            intent.putExtra("correo", correoUsuario)
            startActivity(intent)
        }

        btnIrConsultar.setOnClickListener {
            val intent = Intent(this, ConsultarPQRSActivity::class.java)
            intent.putExtra("correo", correoUsuario)
            startActivity(intent)
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }
}