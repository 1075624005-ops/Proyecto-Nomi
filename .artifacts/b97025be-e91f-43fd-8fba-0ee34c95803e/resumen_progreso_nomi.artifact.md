# 📋 Resumen de Progreso Técnico y Metodológico - Proyecto NOMI

Este documento consolida todo el trabajo realizado hasta la fecha, organizado por Sprints para su integración en el Documento de Producto oficial.

## 🏗️ 1. Especificaciones de Arquitectura Actual
- **Modelo:** Arquitectura Cliente-Servidor Híbrida.
- **Backend:** Firebase (Cloud) - Gestiona identidad, roles y sincronización NoSQL.
- **Frontend:** Kotlin Nativo (Android) + SQLite (Local) - Optimizado para rendimiento offline y gestión de memoria.

## 🚀 2. Registro de Sprints Ejecutados

### Sprint 1: Cimientos y MVP (Finalizado)
- Implementación de la navegación base (25 layouts XML).
- Configuración de Firebase Authentication y Firestore.
- Lógica de registro con cumplimiento legal de **Habeas Data (Ley 1581)**.

### Sprint 2: Seguridad y Auditoría de Datos (Finalizado)
- Implementación de **Hashing SHA-256** para protección de datos locales.
- Eliminación de fugas de memoria (Memory Leaks) mediante el patrón `.use{}` en cursores de SQLite.
- Sistema de **Control de Acceso (RBAC)** dinámico (Admin, Mensajero, Cliente) desde la nube.

### Sprint 3: Funcionalidades Especializadas (Finalizado)
- **Capa Logística:** Calculadora de peso real vs. volumétrico.
- **Capa de Documentación:** Motor de generación de **Rótulos PDF** mediante renderizado de vistas.
- **Capa Gerencial:** Exportación de backups masivos a **Excel** (Apache POI).

### Sprint 4: UX e Interfaz Profesional (Finalizado)
- Sistema de **Colores Semánticos** en `colors.xml` para mantenimiento global.
- Implementación de **Modo Inmersivo** (Auto-hiding de barras del sistema).
- Feedback de usuario mediante `ProgressBar` y estados de botones asíncronos.

### Sprint 5: Ecosistema del Mensajero (En Curso)
- Diseño del **Messenger Home** con menú lateral (hamburguesita).
- Definición de la lógica de **Última Milla** (Notificaciones T-2h y Reprogramación).
- Planificación de la rama de administración para escritorio (PC).

## 🛡️ 3. Estatus de Calidad
- **Código:** Refactorizado y auditado.
- **Seguridad:** Nivel Prototipo de Ingeniería (Defendible ante jurado).
- **UI:** 100% Adaptativa (ConstraintLayout).
