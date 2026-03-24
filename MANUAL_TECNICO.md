# Manual Técnico (Básico)

## 1. Objetivo
Aplicación Android para analizar, interpretar y renderizar formularios del lenguaje `.form`, además de guardar/cargar formularios en `.pkm`.

## 2. Tecnologías
- Kotlin
- Jetpack Compose
- JFlex (análisis léxico)
- CUP (análisis sintáctico)
- Gradle

## 3. Estructura principal
- `app/src/main/java/com/abn/pkm_forms/analisis/`
  - Lexer y parser (`AnalizadorJflex.flex`, `Parser.cup`), manejo de errores léxicos/sintácticos.
- `app/src/main/java/com/abn/pkm_forms/interprete/`
  - AST e interpretación semántica, construcción de elementos de formulario.
- `app/src/main/java/com/abn/pkm_forms/dominio/`
  - Lógica de recorrido de formulario y corrección de respuestas.
- `app/src/main/java/com/abn/pkm_forms/datos/`
  - Lectura/escritura de archivos y serialización/deserialización `.pkm`.
- `app/src/main/java/com/abn/pkm_forms/ui/`
  - Pantalla principal y componentes Compose (editor, paneles, render de formulario).

## 4. Flujo de ejecución
1. Usuario escribe o carga código `.form`.
2. Se ejecuta análisis léxico/sintáctico.
3. Se interpreta el AST y se generan elementos renderizables.
4. Se muestran errores (léxicos, sintácticos, semánticos) y advertencias.
5. El formulario se renderiza en UI.

## 5. Funcionalidad implementada (resumen)
- Soporte de elementos: `SECTION`, `TABLE`, `TEXT`, `OPEN_QUESTION`, `DROP_QUESTION`, `SELECT_QUESTION`, `MULTIPLE_QUESTION`.
- Control de flujo: `IF`, `WHILE`, `DO-WHILE`, `FOR` clásico, `FOR` por rango.
- Soporte de `styles[...]`, `elements:{...}`, `special` y `draw(...)`.
- Soporte de `who_is_that_pokemon(NUMBER,n,m)` para opciones en preguntas cerradas.
- Guardado y carga de `.form` y `.pkm`.

## 6. Compilación
Comando principal:

```bash
./gradlew :app:assembleDebug
```

Generación de lexer/parser (si se modifica `.flex` o `.cup`):

```bash
./gradlew :app:generarParser :app:generarLexer --rerun-tasks
```

## 7. Notas técnicas
- El análisis/interpretación se ejecuta con corrutinas para no bloquear la UI.
- Se maneja límite de iteraciones en ciclos para evitar bucles infinitos.
- Los atributos negativos de tamaño/posición se reportan como error semántico.
