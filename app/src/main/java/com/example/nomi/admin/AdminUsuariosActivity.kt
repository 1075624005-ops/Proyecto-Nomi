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
import com.example.nomi.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class UsuarioGenerico(
    val id: String?,
    val nombreCompleto: String,
    val correo: String,
    val rol: String,
    val tipoDoc: String?,
    val numDoc: String?,
    val telefono: String?,
    val direccion: String?,
    val extra1: String? = null,
    val extra2: String? = null
)

class AdminUsuariosActivity : AppCompatActivity() {

    private val repo = SupabaseRepository()
    private var listaUsuariosCompleta: MutableList<UsuarioGenerico> = mutableListOf()
    private var listaUsuariosFiltrada: MutableList<UsuarioGenerico> = mutableListOf()

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
            listaUsuariosCompleta.clear()

            repo.obtenerTodosLosClientes().onSuccess { clientes ->
                clientes.forEach { c ->
                    listaUsuariosCompleta.add(
                        UsuarioGenerico(
                            id = c.id_usuario,
                            nombreCompleto = "${c.Nombre} ${c.Apellido ?: ""}".trim(),
                            correo = c.Correo_electronico,
                            rol = "CLIENTE",
                            tipoDoc = c.tipo_doc,
                            numDoc = c.numero_doc,
                            telefono = c.Telefono?.toString(),
                            direccion = c.direccion
                        )
                    )
                }
            }

            repo.obtenerTodosLosMensajeros().onSuccess { mensajeros ->
                mensajeros.forEach { m ->
                    listaUsuariosCompleta.add(
                        UsuarioGenerico(
                            id = m.id_usuario,
                            nombreCompleto = "${m.Nombre} ${m.Apellido ?: ""}".trim(),
                            correo = m.Correo_electronico,
                            rol = "MENSAJERO",
                            tipoDoc = m.tipo_doc,
                            numDoc = m.numero_doc,
                            telefono = m.Telefono?.toString(),
                            direccion = m.direccion,
                            extra1 = "Licencia: ${m.licencia_conduccion ?: "No"} (${m.tipo_licencia ?: "A2"})",
                            extra2 = "Zona: ${m.zona_asignada ?: "Bogotá D.C."}"
                        )
                    )
                }
            }

            repo.obtenerTodosLosAsesores().onSuccess { asesores ->
                asesores.forEach { a ->
                    listaUsuariosCompleta.add(
                        UsuarioGenerico(
                            id = a.id_usuario,
                            nombreCompleto = "${a.Nombre} ${a.Apellido ?: ""}".trim(),
                            correo = a.Correo_electronico,
                            rol = "ASESOR",
                            tipoDoc = a.tipo_doc,
                            numDoc = a.numero_doc,
                            telefono = a.Telefono?.toString(),
                            direccion = a.direccion,
                            extra1 = "Código: ${a.codigo_asesor ?: "N/A"}",
                            extra2 = "Área: ${a.area_atencion ?: "Servicio al Cliente"}"
                        )
                    )
                }
            }

            pbCarga.visibility = View.GONE
            filtrarLista()
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
                    u.nombreCompleto.lowercase().contains(query) ||
                    u.correo.lowercase().contains(query) ||
                    (u.numDoc ?: "").lowercase().contains(query) ||
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

            val adapter = object : ArrayAdapter<UsuarioGenerico>(this, R.layout.item_usuario_card, listaUsuariosFiltrada) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_usuario_card, parent, false)
                    val usuario = getItem(position)

                    if (usuario != null) {
                        val tvNombre = view.findViewById<TextView>(R.id.tvNombreUsuarioCard)
                        val tvRol = view.findViewById<TextView>(R.id.tvRolUsuarioCard)
                        val tvCorreo = view.findViewById<TextView>(R.id.tvCorreoUsuarioCard)
                        val tvDoc = view.findViewById<TextView>(R.id.tvDocUsuarioCard)
                        val tvTel = view.findViewById<TextView>(R.id.tvTelUsuarioCard)

                        tvNombre.text = usuario.nombreCompleto
                        tvRol.text = usuario.rol
                        tvCorreo.text = usuario.correo
                        tvDoc.text = "${usuario.tipoDoc ?: "CC"}: ${usuario.numDoc ?: "-"}"
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

    private fun mostrarDetalleUsuario(u: UsuarioGenerico) {
        val mensajeDetalle = StringBuilder()
        mensajeDetalle.append("👤 **Información General**\n")
        mensajeDetalle.append("• Nombre: ${u.nombreCompleto}\n")
        mensajeDetalle.append("• Correo: ${u.correo}\n")
        mensajeDetalle.append("• Rol: ${u.rol}\n\n")

        mensajeDetalle.append("🪪 **Identificación y Contacto**\n")
        mensajeDetalle.append("• Documento: ${u.tipoDoc ?: "CC"} ${u.numDoc ?: "No registrado"}\n")
        mensajeDetalle.append("• Teléfono: ${u.telefono ?: "No registrado"}\n")
        mensajeDetalle.append("• Dirección: ${u.direccion ?: "No registrada"}\n\n")

        if (u.extra1 != null) {
            mensajeDetalle.append("ℹ️ **Información Adicional**\n")
            mensajeDetalle.append("• ${u.extra1}\n")
            if (u.extra2 != null) mensajeDetalle.append("• ${u.extra2}\n\n")
        }

        mensajeDetalle.append("🔐 **Seguridad de Acceso**\n")
        mensajeDetalle.append("• Autenticación gestionada en Supabase Auth")

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
