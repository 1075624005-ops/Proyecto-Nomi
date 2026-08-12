package com.example.nomi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class DatosPQRSActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pqrs)

        // 1. Vincular los componentes del diseño
        val spTipoDoc = findViewById<Spinner>(R.id.spTipoDocPQR)
        val etCedula = findViewById<EditText>(R.id.etCedulaPQR)
        val etNombre = findViewById<EditText>(R.id.etNombrePQR)
        val etCorreo = findViewById<EditText>(R.id.etCorreoPQR)
        val etDireccion = findViewById<EditText>(R.id.etDireccion)
        val rgTipoPersona = findViewById<RadioGroup>(R.id.rgTipoPersona)
        val btnSiguiente = findViewById<Button>(R.id.btnSiguientePQR)

        // 2. Configurar las opciones del Spinner (CC, CE, NIT, PT)
        val opcionesDoc = arrayOf("CC", "NIT", "CE", "PT")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, opcionesDoc) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE) // Texto blanco cuando está cerrado
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                (v as TextView).setTextColor(Color.WHITE) // Texto blanco en la lista desplegable
                v.setBackgroundColor(Color.parseColor("#333333")) // Fondo oscuro para que se vea el blanco
                return v
            }
        }
        spTipoDoc.adapter = adapter

        // 3. Programar el botón SIGUIENTE
        btnSiguiente.setOnClickListener {
            val tipoPersona = if (findViewById<RadioButton>(R.id.rbNatural).isChecked) "Natural" else "Jurídica"
            val tipoDoc = spTipoDoc.selectedItem.toString()
            val cedula = etCedula.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()

            // Validación simple
            if (cedula.isEmpty() || nombre.isEmpty() || correo.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(this, "⚠️ Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- EXPLICACIÓN: ¿CÓMO SE GUARDAN LOS DATOS? ---
            // En un formulario de pasos, los datos NO se guardan en la base de datos todavía.
            // Se van "arrastrando" de pantalla en pantalla usando 'putExtra'.
            // Se guardarán finalmente en la última pantalla cuando le des a "ENVIAR SOLICITUD".
            
            val intent = Intent(this, GenerarPQRSActivity::class.java)
            intent.putExtra("tipo_persona", tipoPersona)
            intent.putExtra("tipo_doc", tipoDoc)
            intent.putExtra("cedula", cedula)
            intent.putExtra("nombre", nombre)
            intent.putExtra("correo", correo)
            intent.putExtra("direccion", direccion)
            startActivity(intent)
        }
    }
}