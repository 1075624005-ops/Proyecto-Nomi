# 📱 Especificación de Frontend - Proyecto NOMI

## 1. Stack Tecnológico
- **Lenguaje:** Kotlin (JVM).
- **Motor de Renderizado:** Android XML con ConstraintLayout (optimización de jerarquía de vistas).
- **Gestión de Recursos:** Sistema de Colores Semánticos (Branding Dinámico).

## 2. Componentes y UI/UX
- **Modo Inmersivo:** Implementación de `WindowInsetsControllerCompat` para auto-ocultamiento de System Bars.
- **Feedback Visual:** Uso de `ProgressBar` y estados de botones (`isEnabled`) para manejar la latencia de red.
- **Adaptabilidad:** Diseños responsivos para diferentes densidades de píxeles y tamaños de pantalla.

## 3. Librerías y Módulos Especializados
- **Glide:** Carga asíncrona de imágenes y gestión de caché.
- **Apache POI:** Motor de generación de reportes Excel (.xlsx) en el cliente.
- **ZXing:** Generación y lectura de códigos QR para validación de pedidos.
- **PDF Engine:** Técnica "View-to-PDF" mediante renderizado asíncrono de layouts XML a PdfDocument.
