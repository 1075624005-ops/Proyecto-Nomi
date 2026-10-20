# 📋 Cierre de Auditoría Técnica — Proyecto NOMI
**Rol evaluador:** Lead Software Architect / Especialista en Sistemas de Tiempo Real
**Alcance del proyecto:** MVP académico — optimización de última milla

---

## 1. Resumen Ejecutivo

Esta auditoría inversa evaluó la arquitectura, backend, frontend y lógica proactiva de NOMI sobre cuatro ejes críticos: escalabilidad, resiliencia offline, seguridad de datos sensibles (geolocalización y evidencias) y manejo de errores en la reprogramación automática.

Tras dos rondas de preguntas y respuestas, el proyecto cierra con **10 decisiones de arquitectura defendibles**, ajustadas correctamente al nivel de un MVP académico. Las brechas identificadas en la primera ronda fueron resueltas o formalmente delimitadas como fuera de alcance, con su justificación documentada.

---

## 2. Decisiones Finales por Punto Crítico

| # | Tema | Decisión Final |
|---|------|-----------------|
| 1 | **Escalabilidad de escritura** | Escrituras aisladas por pedido; sin documentos compartidos que generen *hotspotting*. Contadores distribuidos quedan reservados como mejora futura si se requieren totales agregados por zona. |
| 2 | **Motor de notificaciones** | Cloud Function tipo cron cada 15 min. Campo booleano `notificado_T2h` evita duplicidad. Chunking implementado para respetar el límite de 500 tokens por lote de FCM. |
| 3 | **Sincronización offline** | WorkManager con `NetworkType.CONNECTED` + opción de admin para restringir subida de fotos a Wi-Fi. Nombre de archivo `guia_NUMERODEGUIA.jpg` permite recuperar evidencias huérfanas aunque se pierda la base SQLite local. |
| 4 | **Resolución de conflictos** | Concurrencia optimista basada en `FieldValue.serverTimestamp()` (no reloj del dispositivo), eliminando desfases por zona horaria o manipulación del reloj local. |
| 5 | **Control de roles (RBAC)** | Custom Claims de Firebase Auth para validación instantánea y gratuita en Storage Rules. Limitación conocida: la actualización de rol requiere cierre y reinicio de sesión del usuario (aceptable para el volumen de usuarios del MVP). |
| 6 | **Privacidad de geolocalización** | Bóveda de evidencia temporal: coordenadas exactas cifradas por 30 días (ventana PQRS), luego anonimizadas a nivel de barrio, en línea con la Ley 1581 de Habeas Data. |
| 7 | **Seguridad de credenciales locales** | Hashing con PBKDF2-HmacSHA256, salt único por usuario, 100,000 iteraciones — muy por encima del estándar típico de un proyecto académico. |
| 8 | **Consistencia local/nube en reprogramación** | Transacciones atómicas de Firestore + persistencia offline del SDK. La app del mensajero muestra un indicador de "Sincronización Pendiente" mientras no recibe la confirmación en tiempo real. |
| 9 | **Silencio del cliente ante T-2h** | Para el MVP, la notificación se simula vía FCM push (sin IVR/SMS por costos de proveedor externo). El flujo de decisión (confirmar / portería / reprogramar) es el foco académico, no el canal de entrega. |
| 10 | **Cobros por reprogramaciones repetidas** | Límite de 3 strikes por pedido. El cobro por reintento se documenta como **propuesta de modelo de negocio**, sin procesamiento de pagos reales en la app — evita implicaciones legales de cobros no transparentes. |

---

## 3. Alcance y Limitaciones del MVP

Las siguientes decisiones son **exclusiones conscientes de alcance**, no vacíos de diseño, y deben presentarse como tales ante el jurado:

- **Alta disponibilidad y contadores distribuidos:** no se implementan porque el volumen de usuarios del prototipo no lo requiere; la arquitectura ya contempla el camino de escalamiento (Distributed Counters) si el proyecto avanza a fase comercial.
- **Notificación T-2h vía IVR/SMS:** se simula con FCM push. Un proveedor externo (ej. Twilio) implica costos recurrentes no justificables en fase académica.
- **Actualización de rol en tiempo real:** requiere cierre de sesión manual del usuario; aceptable para el número de usuarios de prueba, pendiente de automatizar (`getIdToken(true)`) en fase comercial.
- **Cobros por reintento de entrega:** modelado como propuesta de negocio en la documentación, sin flujo de pago real ni cláusula contractual activa.
- **Georreferenciación avanzada:** fuera de alcance para esta fase; queda registrada como mejora futura.

---

## 4. Conclusión de la Auditoría

La arquitectura híbrida SQLite/Firebase, la lógica de reprogramación automática y las decisiones de seguridad (Custom Claims, PBKDF2, bóveda de evidencia temporal) están **bien fundamentadas y son defendibles** para el nivel de un MVP de semillero de investigación. Las limitaciones declaradas en la Sección 3 refuerzan la sustentación en lugar de debilitarla, siempre que se presenten de forma explícita y no como omisiones descubiertas por el jurado.

**Estado de la auditoría: ✅ Cerrada.**
