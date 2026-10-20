package com.example.nomi.admin

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.auth.LoginActivity
import com.example.nomi.auth.PerfilActivity
import com.example.nomi.data.SupabaseClient
import com.example.nomi.main.ContactenosActivity
import com.example.nomi.pedidos.CotizarActivity
import com.example.nomi.pedidos.RastrearActivity
import com.example.nomi.pedidos.RemitenteActivity
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsesorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var nombreUsuario: String? = null
    private var correoUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asesor)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        drawerLayout = findViewById(R.id.drawer_layout_asesor)
        val navView = findViewById<NavigationView>(R.id.nav_view_asesor)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuAsesor)

        navView.setNavigationItemSelectedListener(this)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        nombreUsuario = intent.getStringExtra("nombre")
        correoUsuario = intent.getStringExtra("correo")

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeAsesor)
        if (!nombreUsuario.isNullOrEmpty()) {
            tvWelcome.text = "Bienvenido, $nombreUsuario"
        }

        // 1. BOTÓN CREAR NUEVO PEDIDO
        findViewById<Button>(R.id.btnCrearPedidoAsesor).setOnClickListener {
            startActivity(Intent(this, RemitenteActivity::class.java))
        }

        // 2. BOTÓN COTIZAR ENVÍO
        findViewById<Button>(R.id.btnCotizarAsesor).setOnClickListener {
            startActivity(Intent(this, CotizarActivity::class.java))
        }

        // 3. BOTÓN GESTIONAR Y RESPONDER PQRS
        findViewById<Button>(R.id.btnGestionarPQRSAsesor).setOnClickListener {
            val intent = Intent(this, AdminListaPQRSActivity::class.java)
            intent.putExtra("correo", correoUsuario)
            startActivity(intent)
        }

        // 4. BOTÓN RASTREAR PEDIDO
        findViewById<Button>(R.id.btnRastrearAsesor).setOnClickListener {
            val intent = Intent(this, RastrearActivity::class.java).apply {
                putExtra("correo", correoUsuario)
                putExtra("nombre", nombreUsuario)
            }
            startActivity(intent)
        }

        // 5. BOTÓN CENTRO DE ATENCIÓN / CONTACTO
        findViewById<Button>(R.id.btnContactoAsesor).setOnClickListener {
            startActivity(Intent(this, ContactenosActivity::class.java))
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.asesor_nuevo_pedido -> {
                startActivity(Intent(this, RemitenteActivity::class.java))
            }
            R.id.asesor_cotizar -> {
                startActivity(Intent(this, CotizarActivity::class.java))
            }
            R.id.asesor_pqrs -> {
                val intent = Intent(this, AdminListaPQRSActivity::class.java)
                intent.putExtra("correo", correoUsuario)
                startActivity(intent)
            }
            R.id.asesor_rastrear -> {
                val intent = Intent(this, RastrearActivity::class.java).apply {
                    putExtra("correo", correoUsuario)
                    putExtra("nombre", nombreUsuario)
                }
                startActivity(intent)
            }
            R.id.asesor_logout -> {
                lifecycleScope.launch {
                    try {
                        SupabaseClient.client.auth.signOut()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val intent = Intent(this@AsesorActivity, LoginActivity::class.java)
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
