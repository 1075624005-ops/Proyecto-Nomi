# 🎨 MASTER PROMPTS: DISEÑO FRONTEND NOMI (25 MÓDULOS)

Este documento contiene la descripción visual detallada de cada una de las 25 pantallas de la aplicación NOMI. Úsalo para que una IA genere el diseño visual o el código XML desde cero.

---

## 🏗️ ESTÁNDARES GLOBALES DE DISEÑO
- **Tema:** Dark Mode (Modo Oscuro).
- **Colores:** Fondo Negro (#000000), Tarjetas Gris Oscuro (#12121A), Acentos Azul Cian (#00AEEF).
- **Bordes:** Esquinas redondeadas (CornerRadius) entre 12dp y 16dp.
- **Componentes:** Uso de Material Design 3, CardViews con bordes cian y TextInputLayouts estilo Outlined.

---

## 📂 1. MÓDULOS DE ACCESO Y PERFIL

### 1. activity_splash.xml (Pantalla de Carga)
> "Diseña una pantalla de Splash minimalista con fondo negro. En el centro, el logo de NOMI (un isotipo circular con flechas) de 160dp. Debajo, el nombre 'NOMI' en blanco negrita tamaño 28sp y el eslogan 'SOLUCIONES A TU ALCANCE' en azul cian con espaciado de letras. En la parte inferior, la versión 'v1.0.0' en gris tenue."

### 2. activity_login.xml (Inicio de Sesión)
> "Crea una interfaz de Login elegante. Logo centrado arriba, seguido del nombre de la app y un saludo de bienvenida. Dos campos de texto estilo 'Outlined' (Correo y Contraseña) con bordes cian. Un botón grande azul para 'INICIAR SESIÓN' y un enlace de '¿Olvidó su contraseña?'. Abajo, un botón de contorno cian para 'CREAR USUARIO'."

### 3. activity_register.xml (Creación de Cuenta)
> "Diseña un formulario de registro extenso en modo oscuro. Campos para Nombre, Tipo de Doc (Spinner), Cédula, Teléfono, Correo, Dirección y Contraseña. Incluye una sección legal de 'Habeas Data' con un Checkbox y un enlace a la política. Los botones de acción deben ser 'REGISTRARME' (Sólido) y 'VOLVER' (Contorno)."

### 4. nav_header.xml (Encabezado Menú Lateral)
> "Diseña el cabezal del menú lateral. Fondo gris oscuro (#12121A). Incluye el logo de NOMI a la izquierda, debajo el nombre del usuario en azul cian negrita y su correo en gris. Finaliza con una línea horizontal azul cian de 2dp de grosor."

### 5. activity_perfil.xml (Gestión de Perfil)
> "Interfaz de 'Mi Perfil' con fondo negro. Foto de perfil o logo arriba. Campos de datos personales (Nombre, Doc, Teléfono, Correo) dentro de una caja de texto deshabilitada pero con estilo moderno. Botón principal 'ACTUALIZAR DATOS' en azul cian y botón 'VOLVER AL INICIO' con contorno azul."

---

## 📂 2. MÓDULOS DE HOME Y OPERACIÓN

### 6. activity_home.xml (Panel Principal)
> "Diseña un tablero principal dinámico. Toolbar con menú tipo hamburguesa y logo. Tarjeta superior de 'Rastrear Paquete' con buscador integrado. Debajo, un Carrusel de imágenes promocionales. Al final, una cuadrícula de botones tipo tarjeta para 'Cotizar Envío', 'PQRS' y 'Contacto'. Fondo negro y tarjetas gris oscuro."

### 7. activity_cotizar.xml (Calculadora de Envíos)
> "Interfaz de calculadora logística organizada por tarjetas. Tarjeta 1: Ruta (Origen/Destino con flecha indicadora). Tarjeta 2: Dimensiones (Ancho, Largo, Alto en 3 columnas). Tarjeta 3: Pesos y Valor Declarado. Incluye una tabla de tarifas elegante. El resultado aparece en una burbuja azul destacada que dice 'Valor estimado'."

### 8. activity_pedidos.xml (Menú de Envíos)
> "Pantalla de transición para 'Nuevo Pedido'. Logo arriba y título grande. Dos botones centrales de gran tamaño y estilo profesional: 'REGISTRAR REMITENTE' (Sólido azul) y 'VOLVER AL PANEL' (Contorno azul). Estética limpia y directa."

### 9. activity_remitente.xml (Paso 1: Datos de Origen)
> "Diseña el primer paso de un pedido. Título '1. DATOS DEL REMITENTE' en azul cian. Un CardView con borde cian que contiene 4 campos: Nombre, Teléfono, Correo y Dirección de recogida. Botones inferiores: 'SIGUIENTE' en azul sólido y 'CANCELAR' con solo borde cian sobre fondo negro."

### 10. activity_destinatario.xml (Paso 2: Datos de Destino)
> "Interfaz similar al remitente pero para el destino. Título '2. DATOS DEL DESTINATARIO'. Incluye campos de Nombre, Teléfono, Correo y un Spinner para 'Localidad de Entrega (Bogotá)' con fondo oscuro y borde cian. Botones de navegación 'SIGUIENTE' y 'VOLVER' abajo."

---

## 📂 3. MÓDULOS DE CIERRE DE PEDIDO

### 11. activity_detalles_pedido.xml (Configuración de Envío)
> "Pantalla detallada con tarjetas de resumen de Remitente y Destinatario. Campo para descripción del contenido. Grid de 4 columnas para medidas y peso. Incluye selector de 'Forma de Pago' (Pago Inmediato vs Contraentrega) mediante RadioButtons azul cian. Botón final 'VER RESUMEN'."

### 12. activity_pago_inmediato.xml (Pasarela de Pago)
> "Interfaz de pago por transferencia. Muestra el número de guía y el monto grande en azul cian. Una tarjeta central con el código QR de pago (Nequi/Daviplata), titular y número de cuenta. Botón de 'COPIAR NÚMERO', 'YA TRANSFERÍ' y un botón verde para 'ENVIAR COMPROBANTE POR WHATSAPP'."

### 13. activity_finalizar_pedido.xml (Resumen Final)
> "Diseña una pantalla de confirmación. Tres tarjetas apiladas: 1. Ruta (Remitente/Destinatario). 2. Detalles del Paquete. 3. Método y Total. El precio final aparece en tamaño gigante (28sp) en azul cian. Botón principal 'CONFIRMAR PEDIDO' y secundario 'CORREGIR DATOS'."

### 14. activity_rotulo_pedido.xml (Vista de Etiqueta)
> "Interfaz de 'Rótulo de Envío'. Tarjeta central que imita una guía física: Logo 'NOMI EXPRESS', número de guía en grande, datos del destinatario y contenido. Debajo, una tarjeta de 'Pago Contraentrega' en color amarillo ámbar con el monto a cobrar. Botón 'IMPRIMIR / COMPARTIR'."

### 15. item_pedido.xml (Fila de Lista de Pedidos)
> "Diseña una tarjeta individual para un listado de pedidos. Esquina izquierda: Número de guía en azul negrita. Esquina derecha: Icono de tres puntos para opciones. Centro: Fecha, destinatario y total. Abajo a la izquierda: Etiqueta de estado (Pendiente, Entregado, etc.) con fondo de color suave según el estado."

---

## 📂 4. MÓDULOS DE RASTREO Y PQRS

### 16. activity_rastrear.xml (Línea de Tiempo)
> "Interfaz de seguimiento con línea de tiempo vertical. Cuatro pasos con iconos de Check: 1. Recibido, 2. En camino, 3. En punto de destino, 4. Entregado. Los pasos completados se iluminan en cian. Debajo, dos tarjetas cuadradas: una para ver la imagen de la guía y otra para 'Reportar Novedad'."

### 17. activity_pqrs_menu.xml (Menú de Soporte)
> "Pantalla con dos botones tipo tarjeta horizontales y grandes: 'Generar una PQRS' (con icono de lápiz) y 'Consultar PQRS' (con icono de lupa). Títulos en blanco y descripciones en gris tenue. Diseño orientado a la facilidad de uso para el cliente."

### 18. activity_datos_pqrs.xml (Paso 1: Identificación PQR)
> "Formulario inicial para PQRS. Incluye una tarjeta de aviso legal sobre el 'Derecho de Petición'. RadioButtons para elegir entre persona 'Natural' o 'Jurídica'. Campos para Documento, Nombre, Correo y Dirección de notificación. Botón 'Siguiente' en azul cian."

### 19. activity_generar_pqrs.xml (Paso 2: Redacción PQR)
> "Interfaz de redacción de solicitudes. Spinner para 'Tipo de Solicitud'. Campo de 'Asunto'. Un área de texto grande (TextArea) para la descripción detallada. Incluye un resumen de tiempos de respuesta legales y un Checkbox de autorización de uso de datos."

### 20. activity_consultar_pqrs.xml (Lista de Solicitudes)
> "Pantalla de historial de PQRS para el cliente. Fondo negro variante (#0F0F14). Contenedor de Scroll donde se cargan tarjetas dinámicas con el radicado, asunto y estado de cada solicitud. Botón inferior de 'VOLVER' con fondo gris oscuro."

### 21. activity_contactenos.xml (Centro de Ayuda)
> "Diseña una central de contacto con 3 tarjetas grandes: 1. WhatsApp (Borde verde #25D366). 2. Llamada de Servicio (Borde azul cian). 3. Enviar Correo (Borde gris). Cada tarjeta tiene un icono representativo a la izquierda y el nombre del servicio en negrita."

---

## 📂 5. MÓDULOS ADMINISTRATIVOS

### 22. activity_admin.xml (Panel del Jefe)
> "Dashboard administrativo con 3 botones de acción masiva: 'NUEVO PEDIDO', 'VER TODOS LOS PEDIDOS' y 'GESTIONAR PQRS'. Estilo de tarjetas oscuras con bordes cian. Incluye un menú lateral (Drawer) específico para el admin con opción de 'Hacer Backup Excel'."

### 23. activity_admin_lista_pedidos.xml (Control de Envíos)
> "Interfaz avanzada de gestión de pedidos. Incluye filtros por número de guía y por rango de fechas (Desde/Hasta) con botones estilo calendario. Lista scrollable de pedidos con buscador. Botón de 'Limpiar Filtros' en color rojo alerta (#D32F2F)."

### 24. activity_admin_lista_pqrs.xml (Control de PQRS)
> "Listado de todas las PQRS registradas en la nube para el administrador. Título destacado. Espacio para cargar tarjetas que muestran el número de radicado, el nombre del cliente que reporta y el estado actual de la solicitud."

### 25. activity_admin_pqrs_detalle.xml (Gestión de Respuesta)
> "Pantalla de respuesta administrativa. Tarjeta superior con la 'Ficha del Cliente' (Nombre, Doc, Correo). Segunda tarjeta con la 'Situación Reportada'. Campo de texto grande para que el admin escriba la respuesta oficial. Botón sólido 'ENVIAR RESPUESTA'."
