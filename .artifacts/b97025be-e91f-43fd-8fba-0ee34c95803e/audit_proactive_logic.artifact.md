# 🧠 Especificación de Lógica Proactiva y Optimización - NOMI

## 1. Flujo de Optimización de Última Milla
El sistema busca reducir el "fallo de entrega" mediante la validación previa de disponibilidad del cliente.

### Estrategia de Notificación Proactiva (Estilo Temu)
- **Evento T-2 Horas:** El sistema calcula la llegada estimada. Se dispara una notificación FCM al cliente.
- **Interacción del Cliente:**
    1. **Confirmación:** "Estoy en casa". El mensajero procede.
    2. **Alternativa:** "Dejar en portería/recepción". El mensajero recibe la instrucción de depósito.
    3. **Negación:** "No puedo recibirlo". El sistema activa el flujo de **Reprogramación Automática**.

## 2. Impacto en la Ruta del Mensajero
- Si el cliente reprograma, el pedido se marca como "Pendiente de Nueva Fecha" y desaparece de la "Carga Activa" del mensajero al instante.
- El sistema recalcula la prioridad de las paradas restantes para optimizar el tiempo y combustible del personal de mensajería.

## 3. Trazabilidad de Reprogramación
Cada intento fallido y cada reprogramación genera un log en Firebase para auditoría del Administrador, permitiendo identificar patrones de entrega fallida por zonas.
