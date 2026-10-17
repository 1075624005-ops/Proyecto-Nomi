package com.example.nomi.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.PerfilMensajeroPostgres
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminMensajerosActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_mensajeros)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        container = findViewById(R.id.containerMensajeros)
        val btnAgregar = findViewById<Button>(R.id.btnAgregarMensajero)
        val btnVolver  = findViewById<Button>(R.id.btnVolverAdmin)

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, AdminRegistrarMensajeroActivity::class.java))
        }

        btnVolver.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        cargarPlanilla(container)
    }

    private fun cargarPlanilla(container: LinearLayout) {
        lifecycleScope.launch {
            try {
                // Obtener todos los usuarios y filtrar los que tengan rol mensajero (insensible a mayúsculas/minúsculas)
                val res = repo.obtenerTodosLosUsuarios()
                
                container.removeAllViews()

                res.onSuccess { todos ->
                    val listaMensajeros = todos.filter { it.rol.lowercase().trim() == "mensajero" }

                    if (listaMensajeros.isEmpty()) {
                        val tv = TextView(this@AdminMensajerosActivity).apply {
                            text = "No hay mensajeros registrados."
                            setTextColor(Color.GRAY)
                            gravity = android.view.Gravity.CENTER
                            setPadding(0, 50, 0, 0)
                        }
                        container.addView(tv)
                        return@launch
                    }

                    for (m in listaMensajeros) {
                        val card = com.google.android.material.card.MaterialCardView(this@AdminMensajerosActivity).apply {
                            val p = LinearLayout.LayoutParams(-1, -2)
                            p.setMargins(0, 0, 0, 24)
                            layoutParams = p
                            setCardBackgroundColor(ContextCompat.getColor(context, R.color.app_surface_card))
                            radius = 16f
                            setContentPadding(30, 30, 30, 30)
                            strokeColor = ContextCompat.getColor(context, R.color.brand_primary)
                            strokeWidth = 2
                        }

                        val layout = LinearLayout(this@AdminMensajerosActivity).apply { orientation = LinearLayout.VERTICAL }

                        // Fila 1: Nombre y Estado Badge
                        val rowHeader = LinearLayout(this@AdminMensajerosActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = android.view.Gravity.CENTER_VERTICAL
                        }

                        val tvNombre = TextView(this@AdminMensajerosActivity).apply {
                            val p = LinearLayout.LayoutParams(0, -2, 1f)
                            layoutParams = p
                            text = "🛵 ${m.nombre.uppercase()}"
                            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                            setTypeface(null, Typeface.BOLD)
                            textSize = 16f
                        }

                        val tvEstadoBadge = TextView(this@AdminMensajerosActivity).apply {
                            text = "🟢 ACTIVO"
                            setTextColor(Color.parseColor("#28A745"))
                            setTypeface(null, Typeface.BOLD)
                            textSize = 12f
                        }

                        rowHeader.addView(tvNombre)
                        rowHeader.addView(tvEstadoBadge)

                        // Fila 2: Correo
                        val tvCorreo = TextView(this@AdminMensajerosActivity).apply {
                            text = "📧 ${m.Correo_electronico ?: "Sin correo"}"
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            textSize = 13f
                            setPadding(0, 6, 0, 0)
                        }

                        // Fila 3: Documento
                        val tvDoc = TextView(this@AdminMensajerosActivity).apply {
                            text = "🪪 ${m.tipo_doc ?: "CC"}: ${m.numero_doc ?: "Sin cédula"}"
                            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                            textSize = 13f
                            setPadding(0, 4, 0, 0)
                        }

                        // Fila 4: Vehículo, Placa y Zona
                        val tvInfo = TextView(this@AdminMensajerosActivity).apply {
                            val area = m.zona_asignada.ifEmpty { "Todas las Zonas" }
                            val placa = m.placa_vehiculo.ifEmpty { "Sin Placa" }
                            text = "🚘 Placa/Vehículo: $placa\n📍 Área/Zona: $area"
                            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                            setTypeface(null, Typeface.BOLD)
                            textSize = 13f
                            setPadding(0, 8, 0, 0)
                        }

                        val tvAccion = TextView(this@AdminMensajerosActivity).apply {
                            text = "Toca para gestionar estado y entregas ➔"
                            setTextColor(ContextCompat.getColor(context, R.color.brand_primary))
                            textSize = 12f
                            gravity = android.view.Gravity.END
                            setPadding(0, 10, 0, 0)
                        }

                        layout.addView(rowHeader)
                        layout.addView(tvCorreo)
                        layout.addView(tvDoc)
                        layout.addView(tvInfo)
                        layout.addView(tvAccion)

                        card.addView(layout)

                        card.setOnClickListener {
                            mostrarGestionMensajero(m)
                        }

                        container.addView(card)
                    }
                }.onFailure {
                    Toast.makeText(this@AdminMensajerosActivity, "❌ Error al cargar lista", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@AdminMensajerosActivity, "Error al cargar planilla", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarGestionMensajero(m: PerfilMensajeroPostgres) {
        val opciones = arrayOf(
            "🟢 Estado: Activo / Disponible",
            "🟡 Estado: En Ruta de Entrega",
            "🔴 Estado: En Vacaciones / Inactivo",
            "📋 Ver Historial de Entregas del Domiciliario"
        )

        val nom = "${m.Nombre ?: "Mensajero"} ${m.Apellido ?: ""}".trim()

        AlertDialog.Builder(this)
            .setTitle("🛵 Gestión de Domiciliario: $nom")
            .setMessage("• Correo: ${m.Correo_electronico ?: "-"}\n• Cédula: ${m.numero_doc ?: "-"}\n• Placa: ${m.placa_vehiculo.ifEmpty { "-" }}\n• Zona: ${m.zona_asignada.ifEmpty { "-" }}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "✅ Mensajero $nom marcado como ACTIVO", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "🟡 Mensajero $nom en RUTA", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "🔴 Mensajero $nom en VACACIONES", Toast.LENGTH_SHORT).show()
                    3 -> {
                        val intent = Intent(this, AdminListaPedidosActivity::class.java)
                        intent.putExtra("correo_mensajero", m.Correo_electronico)
                        startActivity(intent)
                    }
                }
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
