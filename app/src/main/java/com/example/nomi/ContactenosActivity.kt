package com.example.nomi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ContactenosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactenos)

        val btnWhatsapp = findViewById<CardView>(R.id.btnWhatsapp)
        val btnLlamar = findViewById<CardView>(R.id.btnLlamar)
        val btnEmail = findViewById<CardView>(R.id.btnEmail)
        val btnVolver = findViewById<Button>(R.id.btnVolverContacto)

        // COMANDO PARA WHATSAPP
        btnWhatsapp.setOnClickListener {
            val numero = "573138150074" 
            val mensaje = "Hola Nomi, necesito soporte con mi pedido."
            val url = "https://wa.me/$numero?text=${Uri.encode(mensaje)}"
            
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // COMANDO PARA LLAMADA
        btnLlamar.setOnClickListener {
            val numero = "tel:3138150074"
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse(numero)
            startActivity(intent)
        }

        // COMANDO PARA EMAIL
        btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:soporte@nomi.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Soporte App Nomi")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No hay apps de correo instaladas", Toast.LENGTH_SHORT).show()
            }
        }

        btnVolver.setOnClickListener { finish() }
    }
}