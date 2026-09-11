# 🛡️ Respuestas Técnicas de Defensa - Auditoría NOMI

Este documento contiene las respuestas estratégicas para las 10 preguntas críticas planteadas por el Arquitecto de Software.

---

## 🏗️ I. ESCALABILIDAD

### 1. Gestión de Lecturas/Escrituras Masivas
**Respuesta:** Implementaremos una estrategia de **Paginación con Cursores de Firestore** (`limit()` y `startAfter()`) para evitar la carga de colecciones completas. Para optimizar costos y latencia, definiremos **Índices Compuestos** específicos en la consola de Firebase. La concurrencia se manejará mediante **Batched Writes** (escrituras por lotes) para procesos administrativos, asegurando que las operaciones se realicen de forma atómica y eficiente.

### 2. Motor de Notificaciones T-2h
**Respuesta:** No usaremos un trigger por documento. La solución será una **Cloud Function Programada (Cron Job)** que se ejecute cada 15 minutos. Esta función consultará todos los pedidos cuya ventana de entrega estimada sea de 2 horas y disparará las notificaciones FCM en lotes (batches) de hasta 500 mensajes por llamada, optimizando drásticamente el costo de ejecución.

---

## 📶 II. RESILIENCIA OFFLINE

### 3. Consistencia Eventual
**Respuesta:** Utilizaremos **WorkManager** de Android. Cuando un mensajero registra una entrega sin señal, el cambio se guarda en SQLite y se encola una tarea persistente. WorkManager garantiza que, aunque se cierre la app o se reinicie el celular, los datos se sincronizarán con Firestore apenas se recupere la conexión.

### 4. Resolución de Conflictos
**Respuesta:** Implementaremos **Control de Concurrencia Optimista**. Cada pedido tendrá un campo `last_updated_at`. Si el mensajero intenta subir un cambio y la versión de la nube es más reciente (ej. el admin reprogramó), la app detectará el conflicto y le pedirá al mensajero "Sincronizar Datos" antes de permitirle guardar, evitando sobrescribir órdenes administrativas.

---

## 🔐 III. SEGURIDAD Y EVIDENCIAS

### 5. Reglas de Firebase Storage
**Respuesta:** El acceso no será público. Configuraremos **Storage Security Rules** granulares. La estructura será `pedidos/{orderId}/evidencia.jpg`. Las reglas permitirán lectura/escritura **únicamente** si el `request.auth.uid` coincide con el `id_mensajero` asignado o si el usuario tiene el rol de `admin`. El cliente solo podrá ver su propia evidencia mediante un Token temporal de Firebase.

### 6. Privacidad de Geolocalización
**Respuesta:** Aplicaremos **Truncamiento de Coordenadas**. En Firestore solo se guardará la ubicación exacta durante la ruta activa para el rastreo del cliente. Una vez entregado el pedido, las coordenadas precisas se eliminan y se guarda únicamente la zona (código postal o barrio) para análisis estadístico, anonimizando el patrón de movimiento del mensajero.

### 7. Refuerzo de Hashing (Salt)
**Respuesta:** La capa local es para **Login Offline**. Implementaremos un **Salt único por usuario** (una cadena aleatoria guardada en SQLite). El hash final será `SHA-256(password + salt)`. Esto anula ataques de Rainbow Tables. Además, el login local solo se permitirá si el usuario ya se autenticó exitosamente en la nube al menos una vez en los últimos 30 días.

---

## 🔁 IV. REPROGRAMACIÓN AUTOMÁTICA

### 8. Transacciones Atómicas
**Respuesta:** El proceso de reprogramación se ejecutará mediante una **Transacción de Firestore**. Esta operación asegura que el cambio de estado del pedido (`reprogramado`) y su eliminación de la ruta activa del mensajero ocurran al mismo tiempo. Si falla el recálculo de la ruta, el estado del pedido no cambia, evitando datos huérfanos.

### 9. Comportamiento por Defecto (No Respuesta)
**Respuesta:** La política por defecto será **"Proceder con la Visita"**. Si el cliente no responde en la ventana de 2 horas, el sistema asume que el compromiso de entrega sigue en pie. Sin embargo, el mensajero recibirá un aviso de "Cliente no validado" para que priorice el contacto telefónico al llegar.

### 10. Regla de Negocio de Reintentos (3-Strike Rule)
**Respuesta:** Implementaremos un **Límite de 3 Reprogramaciones**. Al tercer intento fallido por parte del cliente, el sistema marcará el pedido como "Excepción Logística" y lo devolverá a bodega. El cuarto intento requerirá contacto directo con el administrador y podrá generar un cobro adicional por reenvío, protegiendo la rentabilidad de la ruta.
