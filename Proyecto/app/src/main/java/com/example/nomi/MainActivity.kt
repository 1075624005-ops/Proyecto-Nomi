package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Recibir nombre del usuario desde LoginActivity
        val nombreUsuario = intent.getStringExtra("nombre") ?: "Usuario"
        val tvBienvenido  = findViewById<TextView>(R.id.tvBienvenidoMain)
        tvBienvenido.text = "Bienvenido, $nombreUsuario"

        // Cards del menú
        findViewById<CardView>(R.id.cardCotiza).setOnClickListener {
            // TODO: abrir pantalla Cotiza
        }

        findViewById<CardView>(R.id.cardPQRS).setOnClickListener {
            // TODO: abrir pantalla PQRS
        }

        findViewById<CardView>(R.id.cardContactenos).setOnClickListener {
            // TODO: abrir pantalla Contáctenos
        }

        findViewById<CardView>(R.id.cardRastrear).setOnClickListener {
            // TODO: abrir pantalla Rastrear Guía
        }

        findViewById<CardView>(R.id.cardMisPedidos).setOnClickListener {
            // TODO: abrir pantalla Mis Pedidos
        }

        findViewById<CardView>(R.id.cardPerfil).setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("nombre", nombreUsuario)
            startActivity(intent)
        }

        // Cerrar sesión
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}