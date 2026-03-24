# Manual de Usuario (Básico)

## 1. ¿Qué hace la app?
Permite escribir código `.form`, ejecutarlo para ver el formulario generado, contestar preguntas y guardar/cargar archivos `.form` y `.pkm`.

## 2. Pantalla principal
La pantalla incluye:
- Editor de código `.form`.
- Botones de archivo (`Guardar/Abrir .form`, `Guardar/Abrir .pkm`).
- Botón de ejecución (`Ejecutar (reemplazar)` y `Agregar`).
- Modo `Contestar` y botón `Enviar`.
- Panel de resultados (errores y advertencias).

## 3. Uso rápido
1. Escribe o carga un `.form`.
2. Pulsa `Ejecutar (reemplazar)` para renderizar el formulario.
3. Revisa errores/advertencias en el panel.
4. Activa `Contestar` para responder preguntas.
5. Pulsa `Enviar` para validar respuestas cerradas.

## 4. Botones principales
- `Insertar plantilla`: coloca una base de ejemplo.
- `Selector de colores`: inserta snippets de color en el editor.
- `Ejecutar (reemplazar)`: limpia render previo y muestra el nuevo.
- `Agregar`: añade nuevos elementos al render actual.
- `Contestar`: habilita entradas para responder.

## 5. Archivos
- `.form`: código fuente del formulario.
- `.pkm`: representación guardada del formulario para volver a cargarlo en la app.

## 6. Errores y validaciones
La app muestra:
- Errores léxicos
- Errores sintácticos
- Errores semánticos
- Advertencias semánticas

Si hay errores semánticos, algunos elementos no se renderizan.

## 7. Recomendaciones
- Usa índices desde `0` en respuestas cerradas.
- Verifica que atributos de tamaño/posición no sean negativos.
- Guarda `.pkm` después de ejecutar correctamente para conservar el render.
