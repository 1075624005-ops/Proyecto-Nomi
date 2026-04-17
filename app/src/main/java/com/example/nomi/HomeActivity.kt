package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var nombreUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val tvWelcome = findViewById<TextView>(R.id.tvUserWelcome)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)

        // Botones rápidos de la pantalla principal
        val btnCotiza = findViewById<Button>(R.id.btnCotizaHome)
        val btnPQRS = findViewById<Button>(R.id.btnPQRHome)
        val btnContacto = findViewById<Button>(R.id.btnContactenosHome)

        // Configurar el listener del menú lateral
        navView.setNavigationItemSelectedListener(this)

        // Abrir menú al tocar las tres rayas
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // --- LÓGICA DEL BUSCADOR DE GUÍA ---
        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                val textoGuia = etBuscar.text.toString().trim()
                if (textoGuia.isNotEmpty()) {
                    val intent = Intent(this, RastrearActivity::class.java)
                    intent.putExtra("guia", textoGuia)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Ingrese un número de guía", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // --- LÓGICA DE LOS BOTONES PRINCIPALES CON VALIDACIÓN ---
        btnCotiza.setOnClickListener {
            if (nombreUsuario != null) {
                startActivity(Intent(this, CotizarActivity::class.java))
            } else {
                Toast.makeText(this, "⚠️ Inicia sesión para realizar una cotización", Toast.LENGTH_SHORT).show()
            }
        }

        btnPQRS.setOnClickListener { 
            Toast.makeText(this, "Función PQRS próximamente", Toast.LENGTH_SHORT).show() 
        }

        btnContacto.setOnClickListener { 
            Toast.makeText(this, "Función Contacto próximamente", Toast.LENGTH_SHORT).show() 
        }

        // Verificar sesión (Recibir nombre del Login)
        nombreUsuario = intent.getStringExtra("nombre")
        
        val headerView = navView.getHeaderView(0)
        val tvNavName = headerView.findViewById<TextView>(R.id.tvNavName)
        val menu = navView.menu

        if (nombreUsuario != null && nombreUsuario!!.isNotEmpty()) {
            tvWelcome.text = "Bienvenido, $nombreUsuario"
            tvNavName.text = nombreUsuario
            menu.findItem(R.id.nav_login).isVisible = false
            menu.findItem(R.id.nav_logout).isVisible = true
        } else {
            tvWelcome.text = "Bienvenido a NOMI"
            tvNavName.text = "Invitado"
            menu.findItem(R.id.nav_login).isVisible = true
            menu.findItem(R.id.nav_logout).isVisible = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                if (nombreUsuario != null) {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("nombre", nombreUsuario)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "⚠️ Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_cotizar -> {
                if (nombreUsuario != null) {
                    startActivity(Intent(this, CotizarActivity::class.java))
                } else {
                    Toast.makeText(this, "⚠️ Inicia sesión para realizar una cotización", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.nav_logout -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Función: ${item.title}", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}