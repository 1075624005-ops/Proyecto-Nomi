package com.example.nomi.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.main.HomeActivity
import com.example.nomi.pedidos.RemitenteActivity
import com.example.nomi.auth.LoginActivity
import com.example.nomi.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import com.example.nomi.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        drawerLayout = findViewById(R.id.drawer_layout_admin)
        val navView = findViewById<NavigationView>(R.id.nav_view_admin)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuAdmin)

        navView.setNavigationItemSelectedListener(this)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        val nombre = intent.getStringExtra("nombre")
        val rol = intent.getStringExtra("rol") ?: "admin"

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeAdmin)
        if (tvWelcome != null && !nombre.isNullOrEmpty()) {
            tvWelcome.text = "Bienvenido, $nombre"
        }

        val vFooter = findViewById<TextView>(R.id.vFooterAdmin)
        if (vFooter != null) {
            vFooter.text = if (rol == "asesor") "Modo Asesor - NOMI" else "Modo Administrador - NOMI"
        }

        // 1. BOTÓN VER Y SUPERVISAR TODOS LOS PEDIDOS
        findViewById<Button>(R.id.btnVerPedidos)?.setOnClickListener {
            val intent = Intent(this, AdminListaPedidosActivity::class.java)
            startActivity(intent)
        }

        // 2. BOTÓN GESTIONAR MENSAJEROS
        findViewById<Button>(R.id.btnGestionarMensajeros)?.setOnClickListener {
            val intent = Intent(this, AdminMensajerosActivity::class.java)
            startActivity(intent)
        }

        // 3. BOTÓN AUDITORÍA DE PQRS
        findViewById<Button>(R.id.btnGestionarPQRS)?.setOnClickListener {
            val intent = Intent(this, AdminListaPQRSActivity::class.java)
            startActivity(intent)
        }

        // 4. BOTÓN GESTIÓN DE USUARIOS (REAL)
        findViewById<Button>(R.id.btnGestionarUsuarios)?.setOnClickListener {
            val intent = Intent(this, AdminUsuariosActivity::class.java)
            startActivity(intent)
        }

        // 5. BOTÓN PANEL DE INDICADORES GENERALES (REAL)
        findViewById<Button>(R.id.btnIndicadoresAdmin)?.setOnClickListener {
            val intent = Intent(this, AdminIndicadoresActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.admin_pqrs -> {
                startActivity(Intent(this, AdminListaPQRSActivity::class.java))
            }
            R.id.admin_backup -> {
                // TODO: Implementar backup para Postgres si es necesario
                Toast.makeText(this, "Función en migración", Toast.LENGTH_LONG).show()
            }
            R.id.admin_logout -> {
                lifecycleScope.launch {
                    try {
                        com.example.nomi.data.SupabaseClient.client.auth.signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this@AdminActivity, com.example.nomi.auth.LoginActivity::class.java)
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
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
