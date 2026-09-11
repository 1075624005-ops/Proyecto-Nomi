package com.example.nomi

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessengerHomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger_home)

        // MODO INMERSIVO
        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout_messenger)
        val navView = findViewById<NavigationView>(R.id.nav_view_messenger)
        val btnMenu = findViewById<ImageView>(R.id.btnMenuMessenger)
        val tvStatus = findViewById<TextView>(R.id.tvMessengerStatus)

        navView.setNavigationItemSelectedListener(this)
        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        // CARGAR DATOS EN EL HEADER DEL MENÚ
        val headerView = navView.getHeaderView(0)
        val tvNavName = headerView.findViewById<TextView>(R.id.tvNavName)
        val tvNavEmail = headerView.findViewById<TextView>(R.id.tvNavEmail)

        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: "Mensajero"
                    tvNavName.text = nombre
                    tvNavEmail.text = user.email
                    findViewById<TextView>(R.id.tvMessengerWelcome).text = "HOLA, ${nombre.uppercase()}"
                }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_messenger_home -> {
                // Ya estamos aquí
            }
            R.id.nav_messenger_profile -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_messenger_assignments -> {
                Toast.makeText(this, "Próximamente: Pedidos Asignados", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_messenger_history -> {
                Toast.makeText(this, "Próximamente: Historial", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_messenger_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
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
