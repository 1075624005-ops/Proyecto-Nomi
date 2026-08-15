package com.example.nomi

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminListaPedidosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var fechaInicio: Date? = null
    private var fechaFin: Date? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pedidos)

        db = FirebaseFirestore.getInstance()

        // 1. LIMPIEZA INICIAL (A TRAVÉS DE LONG CLICK EN EL TÍTULO)
        findViewById<TextView>(R.id.tvTituloPedidos).setOnLongClickListener {
            AlertDialog.Builder(this)
                .setTitle("Limpiar Base de Datos")
                .setMessage("¿Desea eliminar TODOS los pedidos de prueba? Esta acción es irreversible.")
                .setPositiveButton("Eliminar Todo") { _, _ -> limpiarPedidosDePrueba() }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        // 2. CONFIGURACIÓN DE FILTROS
        findViewById<Button>(R.id.btnFechaInicio).setOnClickListener {
            mostrarDatePicker { date ->
                fechaInicio = date
                findViewById<Button>(R.id.btnFechaInicio).text = sdf.format(date)
            }
        }

        findViewById<Button>(R.id.btnFechaFin).setOnClickListener {
            mostrarDatePicker { date ->
                fechaFin = date
                findViewById<Button>(R.id.btnFechaFin).text = sdf.format(date)
            }
        }

        findViewById<Button>(R.id.btnFiltrar).setOnClickListener {
            val guiaBusqueda = findViewById<EditText>(R.id.etBuscarGuia).text.toString().trim().uppercase()
            cargarPedidos(guiaBusqueda)
        }

        findViewById<Button>(R.id.btnLimpiar).setOnClickListener {
            findViewById<EditText>(R.id.etBuscarGuia).setText("")
            fechaInicio = null
            fechaFin = null
            findViewById<Button>(R.id.btnFechaInicio).text = "DESDE FECHA"
            findViewById<Button>(R.id.btnFechaFin).text = "HASTA FECHA"
            cargarPedidos("")
        }

        findViewById<Button>(R.id.btnVolverPedidos).setOnClickListener {
            finish()
        }

        cargarPedidos("")
    }

    private fun limpiarPedidosDePrueba() {
        db.collection("pedidos").get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnSuccessListener {
                Toast.makeText(this, "Base de datos limpia", Toast.LENGTH_SHORT).show()
                cargarPedidos("")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al limpiar base de datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            onDateSelected(cal.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun cargarPedidos(filtroGuia: String) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarPedidos)
        progressBar?.visibility = View.VISIBLE
        
        val container = findViewById<LinearLayout>(R.id.containerPedidos)
        container?.removeAllViews()

        db.collection("pedidos").orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar?.visibility = View.GONE
                var encontrados = 0
                val format = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                format.maximumFractionDigits = 0

                for (doc in documents) {
                    val guia = doc.getString("guia") ?: ""
                    val fechaTimestamp = doc.get("fecha") as? Timestamp
                    val fechaPedido = fechaTimestamp?.toDate()

                    var cumpleFiltro = true
                    if (filtroGuia.isNotEmpty() && !guia.contains(filtroGuia)) cumpleFiltro = false

                    if (fechaPedido != null) {
                        if (fechaInicio != null && fechaPedido.before(fechaInicio)) cumpleFiltro = false
                        if (fechaFin != null) {
                            val calFin = Calendar.getInstance().apply {
                                time = fechaFin!!
                                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
                            }
                            if (fechaPedido.after(calFin.time)) cumpleFiltro = false
                        }
                    }

                    if (cumpleFiltro) {
                        val view = layoutInflater.inflate(R.layout.item_pedido, container, false)
                        
                        view.findViewById<TextView>(R.id.tvItemGuia).text = "GUÍA: $guia"
                        val dest = doc.getString("dest_nombre") ?: "N/A"
                        val costo = doc.getDouble("costo") ?: 0.0
                        val fechaStr = if (fechaPedido != null) sdf.format(fechaPedido) else "S/F"

                        view.findViewById<TextView>(R.id.tvItemDetalle).text = "Fecha: $fechaStr\nDestinatario: $dest\nTotal: ${format.format(costo)}"

                        val estado = doc.getString("estado") ?: "Pendiente"
                        val tvEstado = view.findViewById<TextView>(R.id.tvItemEstado)
                        tvEstado.text = "Estado: $estado"
                        tvEstado.setTextColor(when (estado) {
                            "Pendiente" -> Color.YELLOW
                            "En camino" -> Color.CYAN
                            "Entregado" -> Color.GREEN
                            "Cancelado" -> Color.RED
                            else -> Color.WHITE
                        })

                        view.setOnClickListener {
                            val intent = Intent(this, RastrearActivity::class.java)
                            intent.putExtra("guia", guia)
                            startActivity(intent)
                        }

                        val btnOpciones = view.findViewById<ImageView>(R.id.btnOpcionesPedido)
                        btnOpciones?.setOnClickListener { v ->
                            val popup = PopupMenu(this, v)
                            popup.menu.add("Re-descargar Rótulo PDF")
                            popup.menu.add("Editar Pedido")
                            popup.menu.add("Eliminar Pedido")

                            popup.setOnMenuItemClickListener { item ->
                                when (item.title) {
                                    "Re-descargar Rótulo PDF" -> reDescargarPdf(doc)
                                    "Editar Pedido" -> mostrarDialogoEditar(doc)
                                    "Eliminar Pedido" -> confirmarEliminacion(doc)
                                }
                                true
                            }
                            popup.show()
                        }

                        container?.addView(view)
                        encontrados++
                    }
                }

                if (encontrados == 0) {
                    val tv = TextView(this).apply {
                        text = "No se encontraron pedidos"
                        setTextColor(Color.GRAY)
                        gravity = android.view.Gravity.CENTER
                        setPadding(0, 50, 0, 0)
                    }
                    container?.addView(tv)
                }
            }
            .addOnFailureListener {
                progressBar?.visibility = View.GONE
                Toast.makeText(this, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reDescargarPdf(doc: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val datos = DatosRotulo(
                guia = doc.getString("guia") ?: "",
                remNombre = doc.getString("rem_nombre") ?: "",
                remDir = doc.getString("rem_dir") ?: "",
                destNombre = doc.getString("dest_nombre") ?: "",
                destDir = doc.getString("dest_dir") ?: "",
                destTel = doc.getString("dest_tel") ?: "",
                nomLocalidad = doc.getString("nom_localidad") ?: "",
                descripcion = doc.getString("descripcion") ?: "",
                tipoEnvio = doc.getString("tipo_envio") ?: "",
                peso = doc.getString("peso") ?: "0",
                costo = doc.getDouble("costo") ?: 0.0,
                esContraentrega = doc.getBoolean("contraentrega") ?: false
            )
            val file = RotuloPdfGenerator.generar(this, datos)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEditar(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val etDir = EditText(this).apply { 
            hint = "Dirección de Entrega"
            setText(doc.getString("dest_dir")) 
        }
        val etTel = EditText(this).apply { 
            hint = "Teléfono Destinatario"
            setText(doc.getString("dest_tel")) 
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val etCosto = EditText(this).apply { 
            hint = "Valor Contraentrega"
            setText(doc.getDouble("costo")?.toInt().toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val spinner = Spinner(this)
        val estados = arrayOf("Pendiente", "En camino", "Entregado", "Cancelado")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinner.setSelection(estados.indexOf(doc.getString("estado") ?: "Pendiente"))

        layout.addView(TextView(this).apply { text = "Estado:"; setPadding(0, 10, 0, 0) })
        layout.addView(spinner)
        layout.addView(etDir)
        layout.addView(etTel)
        layout.addView(etCosto)

        AlertDialog.Builder(this)
            .setTitle("Editar Pedido")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val updates = mapOf(
                    "dest_dir" to etDir.text.toString(),
                    "dest_tel" to etTel.text.toString(),
                    "costo" to (etCosto.text.toString().toDoubleOrNull() ?: 0.0),
                    "estado" to spinner.selectedItem.toString()
                )
                db.collection("pedidos").document(doc.id).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pedido actualizado", Toast.LENGTH_SHORT).show()
                        cargarPedidos("")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminacion(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val guia = doc.getString("guia") ?: ""
        AlertDialog.Builder(this)
            .setTitle("Eliminar Pedido")
            .setMessage("¿Desea eliminar el pedido $guia?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("pedidos").document(doc.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Pedido eliminado", Toast.LENGTH_SHORT).show()
                        cargarPedidos("")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
