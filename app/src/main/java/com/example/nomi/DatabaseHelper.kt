package com.example.nomi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NomiDB.db"
        private const val DATABASE_VERSION = 4 // Subimos a 4 para añadir los nuevos campos de datos personales
        private const val TABLE_USERS = "usuarios"
        private const val TABLE_ORDERS = "pedidos"
        private const val TABLE_PQRS = "pqrs"
        
        // Columnas PQRS (Ampliadas para el Admin)
        private const val COL_PQRS_USER_CORREO = "correo_usuario"
        private const val COL_PQRS_USER_NOMBRE = "nombre_usuario"
        private const val COL_PQRS_USER_TIPO_PERS = "tipo_persona"
        private const val COL_PQRS_USER_TIPO_DOC = "tipo_doc"
        private const val COL_PQRS_USER_NUM_DOC = "num_doc"
        private const val COL_PQRS_USER_DIR = "direccion"
        private const val COL_PQRS_TIPO = "tipo_pqr"
        private const val COL_PQRS_ASUNTO = "asunto"
        private const val COL_PQRS_DESC = "descripcion"
        private const val COL_PQRS_ESTADO = "estado"
        private const val COL_PQRS_RESPUESTA = "respuesta"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_USERS (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, correo TEXT UNIQUE, password TEXT, tipo_doc TEXT, num_doc TEXT, telefono TEXT, direccion TEXT)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (num_guia TEXT PRIMARY KEY, estado INTEGER)")
        
        // TABLA PQRS ACTUALIZADA CON TODOS LOS DATOS
        db?.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_PQRS (id INTEGER PRIMARY KEY AUTOINCREMENT, $COL_PQRS_USER_CORREO TEXT, $COL_PQRS_USER_NOMBRE TEXT, $COL_PQRS_USER_TIPO_PERS TEXT, $COL_PQRS_USER_TIPO_DOC TEXT, $COL_PQRS_USER_NUM_DOC TEXT, $COL_PQRS_USER_DIR TEXT, $COL_PQRS_TIPO TEXT, $COL_PQRS_ASUNTO TEXT, $COL_PQRS_DESC TEXT, $COL_PQRS_ESTADO TEXT, $COL_PQRS_RESPUESTA TEXT)")

        db?.execSQL("INSERT OR IGNORE INTO $TABLE_ORDERS VALUES ('12345', 1)")
        db?.execSQL("INSERT OR IGNORE INTO $TABLE_ORDERS VALUES ('77777777', 2)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_PQRS")
            onCreate(db)
        }
    }

    // --- FUNCIÓN GUARDAR PQR CON DATOS COMPLETOS ---
    fun insertarPQRSCompleta(correo: String, nombre: String, tipoP: String, tDoc: String, nDoc: String, dir: String, tPqr: String, asunto: String, desc: String): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_PQRS_USER_CORREO, correo)
            put(COL_PQRS_USER_NOMBRE, nombre)
            put(COL_PQRS_USER_TIPO_PERS, tipoP)
            put(COL_PQRS_USER_TIPO_DOC, tDoc)
            put(COL_PQRS_USER_NUM_DOC, nDoc)
            put(COL_PQRS_USER_DIR, dir)
            put(COL_PQRS_TIPO, tPqr)
            put(COL_PQRS_ASUNTO, asunto)
            put(COL_PQRS_DESC, desc)
            put(COL_PQRS_ESTADO, "Pendiente")
            put(COL_PQRS_RESPUESTA, "Aún no hay respuesta.")
        }
        return db.insert(TABLE_PQRS, null, v)
    }

    fun obtenerTodasLasPQRS(): Cursor {
        // NOTA: El llamador DEBE cerrar este cursor usando .use { } o .close()
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_PQRS ORDER BY id DESC", null)
    }
    
    fun obtenerPQRPorId(id: Int): Cursor {
        // NOTA: El llamador DEBE cerrar este cursor usando .use { } o .close()
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_PQRS WHERE id = ?", arrayOf(id.toString()))
    }

    fun responderPQRS(id: Int, respuesta: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COL_PQRS_ESTADO, "Respondido")
            put(COL_PQRS_RESPUESTA, respuesta)
        }
        // Usamos parámetros para evitar inyección SQL (implícito en update)
        return db.update(TABLE_PQRS, v, "id = ?", arrayOf(id.toString())) > 0
    }

    fun obtenerMisPQRS(correo: String): Cursor {
        // NOTA: El llamador DEBE cerrar este cursor usando .use { } o .close()
        return readableDatabase.rawQuery("SELECT * FROM $TABLE_PQRS WHERE $COL_PQRS_USER_CORREO = ? ORDER BY id DESC", arrayOf(correo))
    }

    // --- SEGURIDAD: Hashing de contraseñas ---
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun registrarUsuario(nombre: String, tipoDoc: String, numDoc: String, tel: String, correo: String, dir: String, pass: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("nombre", nombre); put("tipo_doc", tipoDoc); put("num_doc", numDoc)
            put("telefono", tel); put("correo", correo); put("direccion", dir)
            put("password", hashPassword(pass)) // Guardamos el HASH, no el texto plano
        }
        return db.insert(TABLE_USERS, null, v) != -1L
    }

    fun validarLogin(correo: String, pass: String): String {
        return readableDatabase.rawQuery("SELECT nombre, password FROM $TABLE_USERS WHERE correo = ?", arrayOf(correo)).use { cursor ->
            if (cursor.moveToFirst()) {
                val passDB = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                
                // Comparamos el hash de la entrada con el hash guardado
                if (passDB == hashPassword(pass)) "ok|$nombre|$correo" else "no_password"
            } else {
                "no_usuario"
            }
        }
    }

    fun existeCorreo(correo: String): Boolean {
        return readableDatabase.rawQuery("SELECT 1 FROM $TABLE_USERS WHERE correo = ?", arrayOf(correo)).use { cursor ->
            cursor.count > 0
        }
    }

    fun actualizarUsuario(correoOri: String, n: String, t: String, d: String, tel: String, c: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("nombre", n); put("tipo_doc", t); put("num_doc", d); put("telefono", tel); put("correo", c)
        }
        return db.update(TABLE_USERS, v, "correo = ?", arrayOf(correoOri)) > 0
    }

    fun cambiarPassword(correo: String, nuevaPass: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply { 
            put("password", hashPassword(nuevaPass)) // Hasheamos la nueva contraseña
        }
        return db.update(TABLE_USERS, v, "correo = ?", arrayOf(correo)) > 0
    }

    fun obtenerEstadoPedido(guia: String): Int {
        return readableDatabase.rawQuery("SELECT estado FROM $TABLE_ORDERS WHERE num_guia = ?", arrayOf(guia)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else -1
        }
    }

    fun generarBackupExcel(context: Context): String {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Usuarios_Nomi")
            val header = sheet.createRow(0)
            val columnas = arrayOf("ID", "Nombre", "Correo", "Teléfono", "Documento")
            columnas.forEachIndexed { i, s -> header.createCell(i).setCellValue(s) }
            
            readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS", null).use { cursor ->
                var rowIdx = 1
                while (cursor.moveToNext()) {
                    val row = sheet.createRow(rowIdx++)
                    row.createCell(0).setCellValue(cursor.getInt(0).toDouble())
                    row.createCell(1).setCellValue(cursor.getString(1))
                    row.createCell(2).setCellValue(cursor.getString(2))
                    row.createCell(3).setCellValue(cursor.getString(6))
                    row.createCell(4).setCellValue(cursor.getString(5))
                }
            }
            
            val file = File(context.getExternalFilesDir(null), "Backup_Nomi.xlsx")
            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()
            return file.absolutePath
        } catch (e: Exception) { return "Error: ${e.message}" }
    }
}