# Guía de Estudio y Réplica - Proyecto NOMI

Manual para replicar la aplicación y practicar la migración de datos.

## 1. Pasos para Replicar la UI
1. **Colores:** Copiar el archivo `res/values/colors.xml` reorganizado. Es el "corazón" visual.
2. **Layouts:** Comenzar por `activity_home.xml` y `activity_login.xml`. Usar siempre `ConstraintLayout`.
3. **Menu Lateral:** Configurar el `NavigationView` en los archivos `activity_home` y `activity_admin`.

## 2. Pasos para Replicar la Lógica
1. **Conexión Firebase:** Configurar `google-services.json` y los permisos en el `AndroidManifest.xml`.
2. **DatabaseHelper:** Crear la clase para manejar SQLite, asegurando que las tablas coincidan con los modelos de datos.
3. **Validación de Datos:** Implementar las validaciones de campos vacíos y formatos de correo en los botones de registro.

## 3. Plan de Práctica: Migración a PostgreSQL
Para pasar de Firebase a una base de datos relacional (Postgres), sigue este esquema:
1. **Esquema de Tablas:**
   - `usuarios`: id (UUID), nombre, correo (UNIQUE), password_hash, rol.
   - `pqrs`: id, usuario_id (FK), tipo, descripcion, estado, fecha_creacion.
   - `pedidos`: num_guia (PK), estado, remitente_id, destinatario_id.
2. **Herramienta:** Usar Supabase o una instancia de Postgres en la nube.
3. **Controlador:** Crear una interfaz de repositorio que oculte si los datos vienen de Firebase o Postgres.
