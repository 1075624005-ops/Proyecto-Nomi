# ☁️ Especificación de Backend - Proyecto NOMI

## 1. Infraestructura Cloud
- **Proveedor:** Firebase (Google Cloud Platform).
- **Servicios Core:**
    - **Authentication:** Gestión de identidad (Email/Password).
    - **Firestore:** Base de datos orientada a documentos (NoSQL).
    - **Storage:** Almacenamiento de evidencias fotográficas de entrega.

## 2. Esquema de Datos (Colecciones Principales)
- **usuarios:** Documentos con esquema flexible que incluyen campos de metadatos (nombre, id_doc, telefono, rol, placa, area).
- **pedidos:** Registro de trazabilidad con llaves foráneas lógicas hacia usuarios (clientes) y mensajeros.
- **pqrs:** Sistema de tickets con trazabilidad de estados (Pendiente/Respondido).

## 3. Lógica de Servidor
- **Roles (RBAC):** La autoridad de las pantallas se valida contra el campo `rol` del documento de usuario tras la autenticación exitosa.
- **Motor de Notificaciones:** Triggers automáticos para avisos de "Pedido en Camino" y consultas de disponibilidad del cliente (2 horas previas a la entrega).
- **Gestión de Reprogramación:** Lógica para desvincular pedidos de la ruta activa del mensajero y re-encolarlos para fechas posteriores sin afectar el flujo del personal.
