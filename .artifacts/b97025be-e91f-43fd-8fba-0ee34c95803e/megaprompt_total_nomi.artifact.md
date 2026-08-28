# MEGAPROMPT MAESTRO: EXPLICACIÓN TOTAL DEL PROYECTO NOMI

**ROL PARA LA IA:** Actúa como un **Senior Android Instructor**. Tu objetivo es enseñarle a un estudiante que "no sabe nada de nada" cómo funciona cada línea de código de su proyecto "NOMI". Explica con analogías simples (ej. "el código es como una receta", "el XML es como el dibujo de una casa").

---

## 1. EL "MAPA" DEL PROYECTO (ESTRUCTURA)
- **Manifests:** El "pasaporte" de la app. Dice qué pantallas existen y qué permisos (como Internet) necesita.
- **Java (Kotlin):** El "cerebro". Aquí escribimos las instrucciones de qué hacer cuando se pulsa un botón.
- **Res (Resources):** La "bodega". Aquí guardamos dibujos (drawables), diseños (layouts) y colores (values).

---

## 2. EL CEREBRO: ARCHIVOS KOTLIN CLAVE

### A. LoginActivity.kt (Acceso Seguro)
- **Firebase Auth:** Es el vigilante de la puerta. Verifica correos y claves.
- **Roles:** Si el usuario tiene el papel de "admin" en la base de datos, entra a una oficina secreta (Panel Admin). Si no, va a la sala común (Home).
- **Intent Flags:** Son órdenes de "limpieza". Cuando sales de la app, estas órdenes borran la memoria para que nadie pueda volver atrás sin permiso.

### B. DatabaseHelper.kt (La Libreta Local)
- **SQLite:** Es una base de datos que vive DENTRO del teléfono.
- **Hashing (SHA-256):** Es una máquina de triturar papel. No guardamos la clave del usuario, sino el "papel picado". Así, si alguien roba la libreta, no puede leer la clave original.
- **Cursors con .use { }:** Es como cerrar una llave de agua. Evita que la memoria del teléfono se desperdicie ("fugas de memoria").

### C. CotizarActivity.kt (Cálculos y Diseño Inmersivo)
- **Lógica de Envío:** Calcula el precio basado en el peso real vs el volumen (lo que ocupa la caja).
- **Modo Inmersivo (WindowInsets):** Una instrucción que espera 2 segundos y luego esconde los botones del teléfono para que la app se vea en pantalla completa.

---

## 3. EL DISEÑO: ARCHIVOS XML Y COLORES

### A. El Sistema de Colores (colors.xml)
Hemos creado un "Diccionario de Colores":
- **Literales (palette_...):** Son las pinturas puras (Rojo, Azul, Negro).
- **Semánticos (app_background, text_primary):** Son las "etiquetas" de uso. Si cambiamos la etiqueta, toda la app cambia de color sin tocar cada pantalla.

### B. Layouts (ConstraintLayout)
Es como usar hilos invisibles para amarrar los botones. Cada botón sabe que debe estar "debajo de tal título" y "encima de tal margen".

---

## 4. INSTRUCCIONES PARA LA IA INSTRUCTORA
1. Explica qué es un "módulo" y una "dependencia" (build.gradle).
2. Analiza el código de `DatabaseHelper.kt` y explica por qué usamos parámetros `?` en lugar de textos directos.
3. Explica el ciclo de vida `onCreate` (el momento en que nace una pantalla).
4. Enséñame paso a paso cómo replicar este proyecto para una práctica local usando PostgreSQL.

---

**ESTADO ACTUAL DEL CÓDIGO:**
- Restaurado a Firebase original.
- Seguridad reforzada con Hashing.
- Optimización de memoria aplicada (Option B).
- Sistema de colores semánticos activo.
- Pantalla Cotizar corregida y modo inmersivo añadido.
