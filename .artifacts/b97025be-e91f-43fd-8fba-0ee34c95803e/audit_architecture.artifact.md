# 🏗️ Especificación de Arquitectura - Proyecto NOMI

## 1. Modelo de Arquitectura
- **Tipo:** Cliente-Servidor Híbrido enfocado en **Optimización de Última Milla**.
- **Paradigma:** Programación Asíncrona basada en Eventos (Kotlin Coroutines).
- **Notificaciones:** Integración con Firebase Cloud Messaging (FCM) para lógica push proactiva.
- **Patrón de UI:** Basado en Actividades (Context-Aware) con transiciones mediante Intent Flags para gestión de pila de tareas.

## 2. Gestión de Datos (Persistence Layer)
- **Capa Local (Edge):** SQLite (SQLiteOpenHelper). Optimizada con el patrón `.use {}` de Kotlin para garantizar el cierre de cursores y prevenir fugas de memoria (Memory Leaks).
- **Capa Nube (Cloud):** Integración con Firebase Firestore para sincronización NoSQL en tiempo real.

## 3. Seguridad y Criptografía
- **Data at Rest (Local):** Implementación de Hashing SHA-256 para la protección de credenciales en la base de datos local.
- **Control de Acceso:** Sistema de Control de Acceso Basado en Roles (RBAC) dinámico gestionado desde el Backend.
- **Navegación:** Uso de `FLAG_ACTIVITY_CLEAR_TASK` para prevenir ataques de secuestro de sesión vía botón físico "back".
