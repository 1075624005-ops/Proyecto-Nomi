package com.example.nomi.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.auth.PerfilActivity
import com.example.nomi.auth.LoginActivity
import com.example.nomi.pedidos.RastrearActivity
import com.example.nomi.pedidos.CotizarActivity
import com.example.nomi.pqrs.PQRSMenuActivity
import com.example.nomi.main.ContactenosActivity
import com.example.nomi.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var nombreUsuario: String? = null
    private var correoUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val tvWelcome = findViewById<TextView>(R.id.tvUserWelcome)
        val etBuscar = findViewById<EditText>(R.id.etBuscar)

        val btnCotiza = findViewById<CardView>(R.id.btnCotizaHome)
        val btnPQRS = findViewById<CardView>(R.id.btnPQRHome)
        val btnContacto = findViewById<CardView>(R.id.btnContactenosHome)

        navView.setNavigationItemSelectedListener(this)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        etBuscar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                val textoGuia = etBuscar.text.toString().trim()
                if (textoGuia.isNotEmpty()) {
                    val intent = Intent(this, RastrearActivity::class.java).apply {
                        putExtra("guia", textoGuia)
                        putExtra("correo", correoUsuario)
                        putExtra("nombre", nombreUsuario)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Ingrese un número de guía", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        btnCotiza.setOnClickListener {
            startActivity(Intent(this, CotizarActivity::class.java))
        }

        btnPQRS.setOnClickListener { 
            val intent = Intent(this, PQRSMenuActivity::class.java)
            intent.putExtra("correo", correoUsuario)
            startActivity(intent)
        }

        btnContacto.setOnClickListener { 
            startActivity(Intent(this, ContactenosActivity::class.java))
        }

        // Recibir datos de sesión
        nombreUsuario = intent.getStringExtra("nombre")
        correoUsuario = intent.getStringExtra("correo")

        val headerView = navView.getHeaderView(0)
        if (headerView != null) {
            val tvNavName = headerView.findViewById<TextView>(R.id.tvNavName)
            val tvNavEmail = headerView.findViewById<TextView>(R.id.tvNavEmail)
            val menu = navView.menu

            if (!nombreUsuario.isNullOrEmpty()) {
                tvWelcome.text = "Bienvenido, $nombreUsuario"
                tvNavName.text = nombreUsuario
                tvNavEmail.text = correoUsuario ?: "Nomi User"
                menu.findItem(R.id.nav_perfil)?.isVisible = true
                menu.findItem(R.id.nav_pedidos)?.isVisible = true
                menu.findItem(R.id.nav_logout)?.isVisible = true
            } else {
                tvWelcome.text = "Bienvenido a NOMI"
                tvNavName.text = "Invitado"
                tvNavEmail.text = "Inicia sesión para más funciones"
                menu.findItem(R.id.nav_perfil)?.isVisible = false
                menu.findItem(R.id.nav_pedidos)?.isVisible = false
                menu.findItem(R.id.nav_logout)?.isVisible = false
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra("nombre", nombreUsuario)
                startActivity(intent)
            }
            R.id.nav_cotizar -> {
                startActivity(Intent(this, CotizarActivity::class.java))
            }
            R.id.nav_contactenos -> {
                startActivity(Intent(this, ContactenosActivity::class.java))
            }
            R.id.nav_pqrs -> {
                val intent = Intent(this, PQRSMenuActivity::class.java)
                intent.putExtra("correo", correoUsuario)
                startActivity(intent)
            }
            R.id.nav_rastrear, R.id.nav_pedidos -> {
                val intentRastreo = Intent(this, RastrearActivity::class.java).apply {
                    putExtra("correo", correoUsuario)
                    putExtra("nombre", nombreUsuario)
                }
                startActivity(intentRastreo)
            }

            R.id.nav_logout -> {
                lifecycleScope.launch {
                    try {
                        com.example.nomi.data.SupabaseClient.client.auth.signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
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

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}