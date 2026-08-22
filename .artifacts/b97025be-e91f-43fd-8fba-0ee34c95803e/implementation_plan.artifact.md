# Migración a Supabase para el Proyecto Nomi

Este plan detalla los pasos para migrar la persistencia de datos de SQLite/Firebase a Supabase en la aplicación Android "Nomi".

## User Review Required

> [!IMPORTANT]
> Se requiere que el usuario pegue la `SUPABASE_KEY` real en el código, ya que en el prompt se proporcionó un marcador de posición.
> También se asume que la tabla `usuarios` en Supabase se utilizará tanto para la autenticación como para almacenar metadatos adicionales.

## Proposed Changes

### Dependencias y Configuración

#### [MODIFY] [build.gradle.kts (app)](file:///C:/version_1/nomi/Proyecto/app/build.gradle.kts)
Se agregarán las dependencias del SDK de Supabase (Postgrest y Auth) y Ktor para la comunicación en red.

### Modelos de Datos

#### [NEW] [Models.kt](file:///C:/version_1/nomi/Proyecto/app/src/main/java/com/example/nomi/Models.kt)
Creación de data classes compatibles con `kotlinx.serialization` para las tablas `usuarios`, `pedidos` y `pqrs`.

### Cliente Supabase

#### [NEW] [SupabaseClient.kt](file:///C:/version_1/nomi/Proyecto/app/src/main/java/com/example/nomi/SupabaseClient.kt)
Inicialización centralizada del cliente Supabase.

### Lógica de Negocio (Reemplazo de DatabaseHelper)

#### [NEW] [SupabaseRepository.kt](file:///C:/version_1/nomi/Proyecto/app/src/main/java/com/example/nomi/SupabaseRepository.kt)
Implementación de las funciones de Login, Registro, Gestión de PQRS y Pedidos usando el SDK de Supabase con Corrutinas.

#### [MODIFY] [DatabaseHelper.kt](file:///C:/version_1/nomi/Proyecto/app/src/main/java/com/example/nomi/DatabaseHelper.kt)
Se marcará como obsoleta o se eliminará progresivamente, ya que la lógica se trasladará al repositorio remoto.

## Verification Plan

### Manual Verification
1.  Verificar que el registro cree un nuevo usuario en la tabla `usuarios` de Supabase.
2.  Validar que el login recupere los datos correctamente.
3.  Consultar un pedido por `num_guia` y verificar que el estado coincida con Supabase.
4.  Crear una PQRS y confirmar su aparición en el dashboard de Supabase.
