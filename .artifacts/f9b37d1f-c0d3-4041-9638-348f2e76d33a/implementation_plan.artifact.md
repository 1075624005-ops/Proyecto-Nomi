# Plan de Implementación: Actualización de Perfiles Unificados y Módulos de Registro (Mensajero y Asesor)

Este plan detalla los cambios necesarios en la aplicación Android para adaptarla estrictamente a las nuevas tablas de Supabase, asegurando que **todos los perfiles compartan la estructura base del cliente**, que los mensajeros **no tengan vehículos en su perfil personal** (sino licencia y zona), y que el Administrador pueda registrar tanto **Mensajeros** como **Asesores** con todos sus campos completos.

## User Review Required

> [!IMPORTANT]
> - Se actualizarán los modelos de Kotlin (`Models.kt`) para que `PerfilMensajeroPostgres` y `PerfilAsesorPostgres` incluyan todos los campos base (Tipo de documento, número de documento, apellido, dirección, etc.).
> - Se eliminarán los campos de vehículo obsoletos del modelo de mensajero.
> - Se creará una nueva actividad y diseño para que el administrador pueda registrar **Asesores**.

## Proposed Changes

### 1. Modelos de Datos ([Models.kt](file:///C:/Proyecto-Nomi-main/app/src/main/java/com/example/nomi/data/Models.kt))
- Actualizar `PerfilMensajeroPostgres` para incluir campos base y de licencia (removiendo placa/soat).
- Actualizar `PerfilAsesorPostgres` para incluir campos base y de asesor.
- Actualizar `PerfilAdminPostgres` para incluir campos base.
- Añadir el modelo `VehiculoPostgres` para la nueva tabla de vehículos de NOMI.

### 2. Repositorio ([SupabaseRepository.kt](file:///C:/Proyecto-Nomi-main/app/src/main/java/com/example/nomi/data/SupabaseRepository.kt))
- Actualizar la función `registrarUsuario` para mapear correctamente todos los campos base y específicos al insertar en `perfil_mensajero` y `perfil_asesor`.

### 3. Interfaz y Registro del Administrador
- **Mensajero**: Actualizar `AdminRegistrarMensajeroActivity` y su layout para recolectar todos los campos (Nombre, Apellido, Tipo Doc, Cédula, Teléfono, Correo, Dirección, Licencia, Tipo de Licencia, Zona, Contraseña).
- **Asesor [NEW]**: Crear `AdminRegistrarAsesorActivity` y su layout `activity_admin_registrar_asesor.xml` para registrar asesores con todos sus campos.
- **Panel de Admin**: Actualizar el menú o actividad de administración para permitir acceder a ambos registros de forma clara.

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`) para verificar que no existan errores de tipos ni de serialización.

### Manual Verification
- Desplegar la aplicación en el emulador / dispositivo.
- Iniciar sesión como Administrador y verificar los botones para registrar Mensajero y Asesor.
- Registrar un Mensajero y un Asesor verificando que sus datos se guarden correctamente en sus respectivas tablas en Supabase.
