# 📘 MEGAPROMPT MAESTRO NOMI v3.0 (ESTADO ACTUAL Y ACTUALIZACIÓN)

Este documento es el resumen técnico definitivo para alinear el software real con el documento de investigación y la matriz del proyecto.

---

## 🏗️ 1. IDENTIDAD Y ARQUITECTURA
- **Propósito:** Digitalización de servicios logísticos (Cotización, Rastreo, PQRS y Administración).
- **Modelo:** Arquitectura Cliente-Servidor Híbrida.
- **Backend (Nube):** Firebase (Authentication y Firestore) para gestión de usuarios, roles y sincronización global.
- **Frontend (Local):** SQLite para persistencia rápida, modo offline y generación de reportes.
- **Lenguaje:** Kotlin 100% con Corrutinas para procesos asíncronos.

## 🛡️ 2. SEGURIDAD Y ESTÁNDARES (Diferenciadores Técnicos)
- **Hashing SHA-256:** Las contraseñas en la base de datos local no se guardan como texto, sino como un código cifrado irreversible.
- **Control de Roles:** La distinción entre "Admin" y "Cliente" es dinámica y se controla desde la nube, no desde el código de la app.
- **Gestión de Memoria:** Uso del bloque `.use { }` en todos los accesos a datos para cerrar conexiones automáticamente y evitar que la app se ponga lenta.
- **Navegación Segura:** Implementación de `Intent.FLAG_ACTIVITY_CLEAR_TASK` para borrar la memoria al cerrar sesión.

## 🎨 3. INTERFAZ Y EXPERIENCIA (UI/UX)
- **Diseño Semántico:** Los colores se definen por función (`brand_primary`, `app_background`) en `colors.xml`, facilitando cambios de marca globales.
- **Modo Inmersivo:** Ocultamiento automático de barras del sistema (Atrás/Inicio) tras 2 segundos para usar el 100% de la pantalla.
- **Diseño Adaptativo:** Uso exclusivo de `ConstraintLayout` para asegurar que la app se vea bien en cualquier tamaño de celular.

## 📦 4. FUNCIONALIDADES CLAVE IMPLEMENTADAS
1. **Módulo Legal:** Cumplimiento de Ley 1581 (Habeas Data) con captura de fecha/hora de consentimiento.
2. **Generador PDF:** Técnica "View-to-PDF" para crear rótulos de envío profesionales desde diseños XML.
3. **Backup Excel:** Exportación masiva de datos usando la librería Apache POI.
4. **Calculadora Logística:** Algoritmo de peso real vs. volumétrico con tabla de tarifas dinámica.

---

## 📝 INSTRUCCIONES PARA LA IA RECEPTORA:
1. Analiza este resumen y actualiza el capítulo de "Metodología" y "Resultados" del documento de investigación.
2. Redacta la justificación técnica explicando por qué el uso de SHA-256 y Corrutinas hace que este proyecto sea de nivel profesional.
3. Asegura que la Matriz de la profesora refleje que el sistema ya es híbrido y cuenta con un control de acceso basado en la nube.
