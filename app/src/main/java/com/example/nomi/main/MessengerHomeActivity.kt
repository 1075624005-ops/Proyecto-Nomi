package com.example.nomi.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.admin.AdminListaPedidosActivity
import com.example.nomi.auth.LoginActivity
import com.example.nomi.auth.PerfilActivity
import com.example.nomi.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessengerHomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var nombreMensajero: String? = null
    private var correoMensajero: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger_home)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        drawerLayout = findViewById(R.id.drawer_layout_messenger)
        val navView = findViewById<NavigationView>(R.id.nav_view_messenger)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuMessenger)

        navView.setNavigationItemSelectedListener(this)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        nombreMensajero = intent.getStringExtra("nombre")
        correoMensajero = intent.getStringExtra("correo")

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeMensajeroText)
        if (!nombreMensajero.isNullOrEmpty()) {
            tvWelcome.text = "Bienvenido, $nombreMensajero"
        }

        // 1. BOTÓN PEDIDOS ASIGNADOS (RUTA DE HOY)
        findViewById<Button>(R.id.btnPedidosAsignados).setOnClickListener {
            val intent = Intent(this, AdminListaPedidosActivity::class.java)
            intent.putExtra("correo_mensajero", correoMensajero)
            startActivity(intent)
        }

        // 2. BOTÓN HISTORIAL DE ENTREGAS FINALIZADAS
        findViewById<Button>(R.id.btnHistorialEntregas).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("📋 Historial de Entregas")
                .setMessage("Resumen de Envíos Finalizados:\n\nMuestra la lista de paquetes entregados con éxito por el domiciliario en la jornada.")
                .setPositiveButton("Cerrar", null)
                .show()
        }

        // 3. BOTÓN MI PERFIL DE DOMICILIARIO
        findViewById<Button>(R.id.btnPerfilMensajero).setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("nombre", nombreMensajero)
            startActivity(intent)
        }

        // 4. BOTÓN CENTRO DE SOPORTE
        findViewById<Button>(R.id.btnSoporteMensajero).setOnClickListener {
            startActivity(Intent(this, ContactenosActivity::class.java))
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_messenger_home -> {
                // Ya estamos aquí
            }
            R.id.nav_messenger_profile -> {
                val intent = Intent(this, PerfilActivity::class.java)
                intent.putExtra("nombre", nombreMensajero)
                startActivity(intent)
            }
            R.id.nav_messenger_assignments -> {
                val intent = Intent(this, AdminListaPedidosActivity::class.java)
                intent.putExtra("correo_mensajero", correoMensajero)
                startActivity(intent)
            }
            R.id.nav_messenger_history -> {
                AlertDialog.Builder(this)
                    .setTitle("📋 Historial de Entregas")
                    .setMessage("Resumen de Envíos Finalizados por el Domiciliario.")
                    .setPositiveButton("Cerrar", null)
                    .show()
            }
            R.id.nav_messenger_logout -> {
                lifecycleScope.launch {
                    try {
                        SupabaseClient.client.auth.signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this@MessengerHomeActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
