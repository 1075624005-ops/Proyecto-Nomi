package com.example.nomi.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomi.R
import com.example.nomi.data.SupabaseRepository
import com.example.nomi.data.UsuarioPostgres
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminUsuariosActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var listaUsuariosCompleta: List<UsuarioPostgres> = emptyList()
    private var listaUsuariosFiltrada: MutableList<UsuarioPostgres> = mutableListOf()

    private lateinit var lvUsuarios: ListView
    private lateinit var pbCarga: ProgressBar
    private lateinit var tvSinUsuarios: TextView
    private lateinit var tvFooter: TextView
    private lateinit var etBuscar: EditText

    private var filtroRolActual: String = "todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_usuarios)

        lifecycleScope.launch {
            delay(2000)
            hideSystemUI()
        }

        findViewById<ImageButton>(R.id.btnVolverUsuarios).setOnClickListener { finish() }

        lvUsuarios = findViewById(R.id.lvUsuarios)
        pbCarga = findViewById(R.id.pbCargaUsuarios)
        tvSinUsuarios = findViewById(R.id.tvSinUsuarios)
        tvFooter = findViewById(R.id.tvFooterUsuarios)
        etBuscar = findViewById(R.id.etBuscarUsuario)

        // BOTONES DE FILTRO RÁPIDO
        val btnTodos = findViewById<Button>(R.id.btnFiltroTodos)
        val btnClientes = findViewById<Button>(R.id.btnFiltroClientes)
        val btnAsesores = findViewById<Button>(R.id.btnFiltroAsesores)
        val btnMensajeros = findViewById<Button>(R.id.btnFiltroMensajeros)
        val btnAdmins = findViewById<Button>(R.id.btnFiltroAdmins)

        btnTodos.setOnClickListener { aplicarFiltroRol("todos") }
        btnClientes.setOnClickListener { aplicarFiltroRol("cliente") }
        btnAsesores.setOnClickListener { aplicarFiltroRol("asesor") }
        btnMensajeros.setOnClickListener { aplicarFiltroRol("mensajero") }
        btnAdmins.setOnClickListener { aplicarFiltroRol("admin") }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarUsuariosDesdeSupabase()
    }

    private fun cargarUsuariosDesdeSupabase() {
        pbCarga.visibility = View.VISIBLE
        lvUsuarios.visibility = View.GONE
        tvSinUsuarios.visibility = View.GONE

        lifecycleScope.launch {
            val res = repo.obtenerTodosLosUsuarios()
            pbCarga.visibility = View.GONE

            res.onSuccess { usuarios ->
                listaUsuariosCompleta = usuarios
                filtrarLista()
            }.onFailure { err ->
                Toast.makeText(this@AdminUsuariosActivity, "❌ Error al cargar usuarios: ${err.message}", Toast.LENGTH_LONG).show()
                tvSinUsuarios.visibility = View.VISIBLE
            }
        }
    }

    private fun aplicarFiltroRol(rol: String) {
        filtroRolActual = rol
        filtrarLista()
    }

    private fun filtrarLista() {
        val query = etBuscar.text.toString().lowercase().trim()

        listaUsuariosFiltrada = listaUsuariosCompleta.filter { u ->
            val coincideRol = if (filtroRolActual == "todos") true else u.rol.lowercase().trim() == filtroRolActual
            val coincideTexto = query.isEmpty() ||
                    u.nombre.lowercase().contains(query) ||
                    u.correo.lowercase().contains(query) ||
                    (u.num_doc ?: "").lowercase().contains(query) ||
                    u.rol.lowercase().contains(query)

            coincideRol && coincideTexto
        }.toMutableList()

        actualizarUI()
    }

    private fun actualizarUI() {
        tvFooter.text = "Total Registrados: ${listaUsuariosCompleta.size} | Mostrando: ${listaUsuariosFiltrada.size}"

        if (listaUsuariosFiltrada.isEmpty()) {
            lvUsuarios.visibility = View.GONE
            tvSinUsuarios.visibility = View.VISIBLE
        } else {
            tvSinUsuarios.visibility = View.GONE
            lvUsuarios.visibility = View.VISIBLE

            val adapter = object : ArrayAdapter<UsuarioPostgres>(this, R.layout.item_usuario_card, listaUsuariosFiltrada) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_usuario_card, parent, false)
                    val usuario = getItem(position)

                    if (usuario != null) {
                        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuarioCard)
                        val tvRol = view.findViewById<TextView>(R.id.tvRolUsuarioCard)
                        val tvCorreo = view.findViewById<TextView>(R.id.tvCorreoUsuarioCard)
                        val tvDoc = view.findViewById<TextView>(R.id.tvDocUsuarioCard)
                        val tvTel = view.findViewById<TextView>(R.id.tvTelUsuarioCard)

                        tvNombre.text = usuario.nombre
                        tvRol.text = usuario.rol.uppercase()
                        tvCorreo.text = usuario.correo
                        tvDoc.text = "${usuario.tipo_doc ?: "CC"}: ${usuario.num_doc ?: "-"}"
                        tvTel.text = "Tel: ${usuario.telefono ?: "-"}"

                        view.setOnClickListener {
                            mostrarDetalleUsuario(usuario)
                        }
                    }
                    return view
                }
            }
            lvUsuarios.adapter = adapter
        }
    }

    private fun mostrarDetalleUsuario(u: UsuarioPostgres) {
        val mensajeDetalle = StringBuilder()
        mensajeDetalle.append("👤 **Información General**\n")
        mensajeDetalle.append("• Nombre: ${u.nombre}\n")
        mensajeDetalle.append("• Correo: ${u.correo}\n")
        mensajeDetalle.append("• Rol: ${u.rol.uppercase()}\n\n")

        mensajeDetalle.append("🪪 **Identificación y Contacto**\n")
        mensajeDetalle.append("• Documento: ${u.tipo_doc ?: "CC"} ${u.num_doc ?: "No registrado"}\n")
        mensajeDetalle.append("• Teléfono: ${u.telefono ?: "No registrado"}\n")
        mensajeDetalle.append("• Dirección: ${u.direccion ?: "No registrada"}\n\n")

        if (u.rol.lowercase() == "mensajero") {
            mensajeDetalle.append("🛵 **Datos de Domiciliario**\n")
            mensajeDetalle.append("• Placa Vehículo: ${u.placa ?: "No asignada"}\n")
            mensajeDetalle.append("• Zona / Área: ${u.area ?: "No asignada"}\n\n")
        }

        if (u.habeas_data_aceptado) {
            mensajeDetalle.append("🔒 **Cumplimiento Legal (Habeas Data - Ley 1581)**\n")
            mensajeDetalle.append("• Consentimiento: ACEPTADO\n")
            mensajeDetalle.append("• Fecha: ${u.habeas_data_fecha ?: "Registrada"}\n")
            mensajeDetalle.append("• Versión Política: ${u.habeas_data_version ?: "v1.0"}\n\n")
        }

        mensajeDetalle.append("🔐 **Seguridad de Acceso**\n")
        mensajeDetalle.append("• Contraseña: [Cifrada de forma segura en servidor Supabase Auth]")

        AlertDialog.Builder(this)
            .setTitle("📋 Detalle Completo de Usuario")
            .setMessage(mensajeDetalle.toString())
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
