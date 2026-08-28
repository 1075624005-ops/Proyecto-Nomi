# Guía de Auditoría Técnica - Proyecto NOMI

Este documento describe la arquitectura y los puntos críticos del sistema para una auditoría profesional.

## 1. Arquitectura de Software
- **Patrón:** Basado en Actividades con Repositorios Híbridos.
- **UI Layer:** XML con ConstraintLayout, optimizado para diferentes tamaños de pantalla y usando variables semánticas en `colors.xml`.
- **Data Layer:**
    - **Persistencia Local:** `DatabaseHelper` (SQLite) para operaciones rápidas y generación de reportes offline.
    - **Persistencia Remota:** Firebase (Authentication para identidad y Firestore para documentos).

## 2. Puntos Críticos de Seguridad
- **Habeas Data:** Registro obligatorio de aceptación de términos (Ley 1581). La app captura fecha, hora y versión de la política aceptada.
- **Navegación Segura:** Uso de `FLAG_ACTIVITY_NEW_TASK` y `FLAG_ACTIVITY_CLEAR_TASK` en procesos de Login/Logout para prevenir el acceso no autorizado a través del botón físico "atrás".

## 3. Manejo de Recursos
- **Imágenes:** Implementación de Glide para evitar `OutOfMemoryError` al cargar recursos visuales.
- **Reportes:** Uso de Apache POI para generación de archivos Excel (.xlsx) directamente desde la base de datos SQLite.
- **Asincronía:** Implementación de Coroutines para evitar el bloqueo del Hilo Principal (UI Thread).
