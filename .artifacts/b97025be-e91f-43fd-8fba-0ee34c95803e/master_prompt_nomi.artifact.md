# Master Prompt: Presentación y Estudio del Proyecto NOMI

Este documento es una guía exhaustiva diseñada para explicar el funcionamiento técnico y conceptual de la aplicación **NOMI**. Puedes usarlo como base para estudiar o como contexto para otras inteligencias artificiales.

---

## 1. RESUMEN DEL PROYECTO
**NOMI** es una aplicación móvil de logística y gestión de envíos desarrollada para Android. Permite a los usuarios cotizar envíos, rastrear pedidos, gestionar PQRS (Peticiones, Quejas, Reclamos y Sugerencias) y, para los administradores, gestionar toda la operación y generar respaldos en Excel.

### Tecnologías Utilizadas:
- **Lenguaje Principal:** Kotlin (Lógica de negocio).
- **Interfaz de Usuario (UI):** XML (Layouts adaptativos).
- **Base de Datos Remota:** Firebase (Authentication y Firestore).
- **Base de Datos Local:** SQLite (Persistencia de datos y modo offline).
- **Librerías Adicionales:** Apache POI (Excel), Glide (Imágenes), ZXing (Códigos QR).

---

## 2. ARQUITECTURA TÉCNICA

### Lógica de Navegación (Intents y Flags)
Para garantizar la seguridad, se utiliza un sistema de navegación que limpia la memoria al cerrar sesión.
- **Concepto:** `Intent.FLAG_ACTIVITY_CLEAR_TASK`.
- **Explicación:** Cuando un usuario o administrador cierra sesión, no solo navegamos a la pantalla de inicio, sino que "vaciamos la pila de platos" (la memoria de actividades abiertas). Esto evita que alguien pueda presionar el botón "atrás" y regresar a un panel privado sin contraseña.

### Gestión de Datos (Híbrida)
1. **Firebase Firestore:** Es la fuente de verdad en la nube. Guarda los perfiles de usuario y las PQRS globales.
2. **SQLite (`DatabaseHelper.kt`):** Se usa para procesos rápidos y locales, como la creación de archivos Excel y el almacenamiento de datos que el usuario necesita consultar sin internet.

---

## 3. CONCEPTOS FUNDAMENTALES PARA ESTUDIO

### Diseño UI (XML)
- **`layout_margin`:** Es el espacio **fuera** del elemento (separación con otros componentes).
- **`padding`:** Es el espacio **dentro** del elemento (separación entre el borde y el contenido, como el texto).
- **`ConstraintLayout`:** Permite posicionar elementos de forma relativa a otros (ej. "Pon este botón debajo del logo y centrado"). Es el más eficiente para Android.

### Lógica Kotlin
- **`ViewBinding`:** Forma moderna de conectar el código Kotlin con los elementos del XML. Evita errores de "referencia nula".
- **Listeners (`setOnClickListener`):** Funciones que "escuchan" las acciones del usuario. Son el punto de entrada para cualquier funcionalidad interactiva.
- **Seguridad de Tipos:** Kotlin obliga a manejar los valores nulos (`?`), lo que hace que la app sea mucho más estable y no se cierre (crash) inesperadamente.

---

## 4. ESTRUCTURA DE ARCHIVOS CLAVE
- **`LoginActivity.kt`:** Punto de entrada con Aviso de Privacidad y autenticación con Firebase.
- **`AdminActivity.kt`:** Panel con permisos especiales, gestión de PQRS y generación de backups.
- **`HomeActivity.kt`:** Pantalla principal con menú lateral (Drawer) y acceso a servicios públicos.
- **`RegisterActivity.kt`:** Registro de usuarios con cumplimiento de la Ley de Habeas Data (Colombia).

---

## 5. CUMPLIMIENTO LEGAL (Habeas Data)
La aplicación integra flujos obligatorios para la protección de datos personales (Ley 1581 de 2012):
- Avisos de privacidad en Login y Registro.
- Registro en base de datos de la fecha y hora exacta en la que el usuario aceptó los términos.
- Opción clara para que el usuario conozca sus derechos dentro de la app.
