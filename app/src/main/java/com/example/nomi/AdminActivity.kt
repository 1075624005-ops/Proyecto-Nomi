package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class AdminActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        db = DatabaseHelper(this)
        drawerLayout = findViewById(R.id.drawer_layout_admin)
        val navView = findViewById<NavigationView>(R.id.nav_view_admin)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuAdmin)

        navView.setNavigationItemSelectedListener(this)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // BOTÓN GESTIONAR PQRS
        findViewById<Button>(R.id.btnGestionarPQRS).setOnClickListener {
            val intent = Intent(this, AdminListaPQRSActivity::class.java)
            startActivity(intent)
        }
        
        // BOTÓN REALIZAR PEDIDOS
        findViewById<Button>(R.id.btnRealizarPedidos).setOnClickListener {
            val intent = Intent(this, RemitenteActivity::class.java)
            startActivity(intent)
        }
        
        // BOTÓN VER TODOS LOS PEDIDOS (Nueva funcionalidad)
        findViewById<Button>(R.id.btnVerPedidos).setOnClickListener {
            val intent = Intent(this, AdminListaPedidosActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.admin_pqrs -> {
                startActivity(Intent(this, AdminListaPQRSActivity::class.java))
            }
            R.id.admin_backup -> {
                val ruta = db.generarBackupExcel(this)
                Toast.makeText(this, "✅ Backup creado", Toast.LENGTH_LONG).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }
}
