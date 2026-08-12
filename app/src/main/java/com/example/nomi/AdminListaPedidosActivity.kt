package com.example.nomi

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdminListaPedidosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var container: LinearLayout
    private var fechaInicio: Date? = null
    private var fechaFin: Date? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_lista_pedidos)

        db = FirebaseFirestore.getInstance()
        container = findViewById(R.id.containerPedidos)
        val etBuscar = findViewById<EditText>(R.id.etBuscarGuia)
        val btnFechaInicio = findViewById<Button>(R.id.btnFechaInicio)
        val btnFechaFin = findViewById<Button>(R.id.btnFechaFin)
        val btnFiltrar = findViewById<Button>(R.id.btnFiltrar)
        val btnLimpiar = findViewById<Button>(R.id.btnLimpiar)
        val btnVolver = findViewById<Button>(R.id.btnVolverPedidos)

        btnFechaInicio.setOnClickListener { mostrarDatePicker { date ->
            fechaInicio = date
            btnFechaInicio.text = sdf.format(date)
        }}

        btnFechaFin.setOnClickListener { mostrarDatePicker { date ->
            fechaFin = date
            btnFechaFin.text = sdf.format(date)
        }}

        btnFiltrar.setOnClickListener {
            val guiaBusqueda = etBuscar.text.toString().trim().uppercase()
            cargarPedidos(guiaBusqueda)
        }

        btnLimpiar.setOnClickListener {
            etBuscar.setText("")
            fechaInicio = null
            fechaFin = null
            btnFechaInicio.text = "DESDE FECHA"
            btnFechaFin.text = "HASTA FECHA"
            cargarPedidos("")
        }

        btnVolver.setOnClickListener { finish() }

        cargarPedidos("")
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
        container.removeAllViews()
        db.collection("pedidos").orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
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
                                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59) 
                            }
                            if (fechaPedido.after(calFin.time)) cumpleFiltro = false
                        }
                    }

                    if (cumpleFiltro) {
                        val view = LayoutInflater.from(this).inflate(R.layout.item_pedido, container, false)
                        view.findViewById<TextView>(R.id.tvItemGuia).text = "GUÍA: $guia"
                        
                        val dest = doc.getString("dest_nombre") ?: "N/A"
                        val costo = doc.getDouble("costo") ?: 0.0
                        val fechaStr = if (fechaPedido != null) sdf.format(fechaPedido) else "S/F"
                        
                        view.findViewById<TextView>(R.id.tvItemDetalle).text = 
                            "Fecha: $fechaStr\nDestinatario: $dest\nTotal: ${format.format(costo)}"
                        
                        val estado = doc.getString("estado") ?: "Pendiente"
                        val tvEstado = view.findViewById<TextView>(R.id.tvItemEstado)
                        tvEstado.text = "Estado: $estado"
                        tvEstado.setTextColor(if (estado == "Pendiente") Color.YELLOW else Color.GREEN)
                        
                        container.addView(view)
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
                    container.addView(tv)
                }
            }
    }
}
