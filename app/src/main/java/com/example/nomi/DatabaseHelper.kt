package com.example.nomi

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NomiDB.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "usuarios"
        private const val TABLE_ORDERS = "pedidos"
        
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_CORREO = "correo"
        private const val COLUMN_PASSWORD = "password"

        private const val COLUMN_GUIA = "num_guia"
        private const val COLUMN_ESTADO = "estado"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE $TABLE_USERS ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NOMBRE TEXT, $COLUMN_CORREO TEXT UNIQUE, $COLUMN_PASSWORD TEXT, tipo_doc TEXT, num_doc TEXT, telefono TEXT, direccion TEXT)")
        db?.execSQL("CREATE TABLE $TABLE_ORDERS ($COLUMN_GUIA TEXT PRIMARY KEY, $COLUMN_ESTADO INTEGER)")
        db?.execSQL("INSERT INTO $TABLE_ORDERS VALUES ('12345', 1)")
        db?.execSQL("INSERT INTO $TABLE_ORDERS VALUES ('77777777', 2)")
        db?.execSQL("INSERT INTO $TABLE_ORDERS VALUES ('99999', 4)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    fun registrarUsuario(nombre: String, tipoDoc: String, numDoc: String, tel: String, correo: String, dir: String, pass: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COLUMN_NOMBRE, nombre); put("tipo_doc", tipoDoc); put("num_doc", numDoc)
            put("telefono", tel); put(COLUMN_CORREO, correo); put("direccion", dir); put(COLUMN_PASSWORD, pass)
        }
        return db.insert(TABLE_USERS, null, v) != -1L
    }

    fun actualizarUsuario(correoOriginal: String, nuevoNombre: String, nuevoTipoDoc: String, nuevoNumDoc: String, nuevoTel: String, nuevoCorreo: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put(COLUMN_NOMBRE, nuevoNombre); put("tipo_doc", nuevoTipoDoc); put("num_doc", nuevoNumDoc); put("telefono", nuevoTel); put(COLUMN_CORREO, nuevoCorreo)
        }
        return db.update(TABLE_USERS, v, "$COLUMN_CORREO = ?", arrayOf(correoOriginal)) > 0
    }

    // --- NUEVA FUNCIÓN PARA RECUPERAR CONTRASEÑA ---
    fun cambiarPassword(correo: String, nuevaPass: String): Boolean {
        val db = this.writableDatabase
        val v = ContentValues()
        v.put(COLUMN_PASSWORD, nuevaPass)
        return db.update(TABLE_USERS, v, "$COLUMN_CORREO = ?", arrayOf(correo)) > 0
    }

    fun validarLogin(correo: String, pass: String): String {
        val cursor = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_CORREO = ?", arrayOf(correo))
        if (cursor.moveToFirst()) {
            val passDB = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE))
            cursor.close()
            return if (passDB == pass) "ok|$nombre|$correo" else "no_password"
        }
        cursor.close()
        return "no_usuario"
    }

    fun existeCorreo(correo: String) = readableDatabase.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_CORREO = ?", arrayOf(correo)).count > 0

    fun obtenerEstadoPedido(guia: String): Int {
        val cursor = readableDatabase.rawQuery("SELECT $COLUMN_ESTADO FROM $TABLE_ORDERS WHERE $COLUMN_GUIA = ?", arrayOf(guia))
        var estado = -1
        if (cursor.moveToFirst()) estado = cursor.getInt(0)
        cursor.close()
        return estado
    }
}