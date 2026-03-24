package com.abn.pkm_forms

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.abn.pkm_forms.analisis.ResultadoAnalisisFormulario
import com.abn.pkm_forms.analisis.ServicioAnalisisFormulario
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.EstiloFormulario
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario
import com.abn.pkm_forms.ui.theme.PKM_FORMSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

private const val CODIGO_DEMO = """
$ Demo inicial
number edad = 10;
string nombre = "Ash";
number indice = 2;
number total = (edad * 2) + 5;
number ancho_base = 320;
number i = 0;
string titulo = "Entrenador: " + nombre;
SECTION [label: "Inicio", width: ancho_base, height: 220, pointX: 0, pointY: 0];
TEXT titulo;
OPEN_QUESTION [label: "Cual es tu pokemon favorito?", width: ancho_base, height: 48];
DROP_QUESTION [label: "Elige tipo", options: {"Fuego", "Agua", "Planta"}, correct: 1, width: ancho_base, height: 48];
SELECT_QUESTION [label: "Elige una opcion", options: {"Pikachu", "Charmander", "Squirtle"}, correct: 0, width: ancho_base, height: 48];
MULTIPLE_QUESTION [label: "Selecciona multiples", options: {"Rojo", "Azul", "Verde"}, correct: {0, 2}, width: ancho_base, height: 48];
TABLE [label: "Tabla de pruebas", width: ancho_base, height: 120, pointX: 0, pointY: 230];
IF (edad > 5) {
    TEXT [content: "IF activo", width: ancho_base, height: 28];
} ELSE {
    TEXT [content: "IF no activo", width: ancho_base, height: 28];
}
WHILE (i < 2) {
    TEXT [content: "WHILE vuelta", width: ancho_base, height: 24];
    i = i + 1;
}
/* comentario de bloque */
edad = total - 1;
"""

private const val PLANTILLA_BASE = """
$ Plantilla base
number edad = 18;
string nombre = "Misty";
SECTION [label: "Datos", width: 320, height: 220, pointX: 0, pointY: 0];
TEXT [content: "Bienvenido", width: 320, height: 32];
OPEN_QUESTION [label: "Cual es tu pokemon favorito?", width: 320, height: 48];
DROP_QUESTION [label: "Elige tipo", options: {"opcion 1", "opcion 2"}, correct: 0, width: 320, height: 48];
SELECT_QUESTION [label: "Elige una opcion", options: {"opcion 1", "opcion 2", "opcion 3"}, correct: 1, width: 320, height: 48];
MULTIPLE_QUESTION [label: "Selecciona multiples", options: {"opcion 1", "opcion 2", "opcion 3"}, correct: {0, 2}, width: 320, height: 48];
"""

private val opcionesColor = listOf(
    "Rojo" to "#E53935",
    "Azul" to "#1E88E5",
    "Verde" to "#43A047",
    "Naranja" to "#FB8C00",
    "Negro" to "#212121"
)

private val regexComentarios = Regex("\\$[^\\n]*|/\\*([\\s\\S]*?)\\*/")
private val regexCadenas = Regex("\"([^\"\\\\]|\\\\.)*\"")
private val regexNumeros = Regex("\\b\\d+(\\.\\d+)?\\b")
private val regexReservadas = Regex("\\b(SECTION|TABLE|TEXT|OPEN_QUESTION|DROP_QUESTION|SELECT_QUESTION|MULTIPLE_QUESTION|IF|ELSE|WHILE|DO|FOR|in|SPECIAL|DRAW|WHO_IS_THAT_POKEMON|elements|styles|number|string|true|false)\\b")
private val regexOperadores = Regex("(\\|\\||&&|==|!=|>=|<=|[+\\-*/%~<>]=?|=)")

private val transformacionSintaxis = VisualTransformation { textoOriginal ->
    val textoPlano = textoOriginal.text
    val textoColoreado = runCatching {
        buildAnnotatedString {
            append(textoPlano)

            regexComentarios.findAll(textoPlano).forEach {
                addStyle(SpanStyle(color = Color(0xFF2E7D32)), it.range.first, it.range.last + 1)
            }
            regexCadenas.findAll(textoPlano).forEach {
                addStyle(SpanStyle(color = Color(0xFF6A1B9A)), it.range.first, it.range.last + 1)
            }
            regexNumeros.findAll(textoPlano).forEach {
                addStyle(SpanStyle(color = Color(0xFF1565C0)), it.range.first, it.range.last + 1)
            }
            regexReservadas.findAll(textoPlano).forEach {
                addStyle(
                    SpanStyle(color = Color(0xFFEF6C00), fontWeight = FontWeight.SemiBold),
                    it.range.first,
                    it.range.last + 1
                )
            }
            regexOperadores.findAll(textoPlano).forEach {
                addStyle(
                    SpanStyle(color = Color(0xFFC62828), fontWeight = FontWeight.Medium),
                    it.range.first,
                    it.range.last + 1
                )
            }
        }
    }.getOrElse {
        buildAnnotatedString { append(textoPlano) }
    }

    TransformedText(textoColoreado, OffsetMapping.Identity)
}

private fun insertarTextoEnCursor(estadoActual: TextFieldValue, textoInsertar: String): TextFieldValue {
    val largo = estadoActual.text.length
    val inicioSeleccion = estadoActual.selection.start.coerceIn(0, largo)
    val finSeleccion = estadoActual.selection.end.coerceIn(0, largo)
    val inicio = minOf(inicioSeleccion, finSeleccion)
    val fin = maxOf(inicioSeleccion, finSeleccion)
    val textoNuevo = estadoActual.text.replaceRange(inicio, fin, textoInsertar)
    val cursorNuevo = inicio + textoInsertar.length
    return estadoActual.copy(text = textoNuevo, selection = TextRange(cursorNuevo, cursorNuevo))
}

private data class ResultadoCargaPkm(
    val elementos: List<ElementoFormulario>,
    val errores: List<String>
)

private data class NodoSerializacionPkm(
    val profundidad: Int,
    val elemento: ElementoFormulario
)

private data class NodoRender(
    val indice: Int,
    val nivel: Int,
    val elemento: ElementoFormulario
)

private data class CeldaTabla(
    val nivel: Int,
    val elemento: ElementoFormulario
)

private data class NodoLecturaPkm(
    val profundidad: Int,
    val elemento: ElementoFormulario
)

private fun escaparCampoPkm(texto: String): String {
    return texto
        .replace("\\", "\\\\")
        .replace("|", "\\|")
        .replace("\n", "\\n")
}

private fun desescaparCampoPkm(texto: String): String {
    val salida = StringBuilder()
    var indice = 0
    while (indice < texto.length) {
        val actual = texto[indice]
        if (actual == '\\' && indice + 1 < texto.length) {
            val siguiente = texto[indice + 1]
            when (siguiente) {
                'n' -> salida.append('\n')
                '\\' -> salida.append('\\')
                '|' -> salida.append('|')
                else -> salida.append(siguiente)
            }
            indice += 2
        } else {
            salida.append(actual)
            indice++
        }
    }
    return salida.toString()
}

private fun encontrarSeparadorPkm(linea: String): Int {
    var escapado = false
    linea.forEachIndexed { indice, caracter ->
        if (escapado) {
            escapado = false
            return@forEachIndexed
        }
        if (caracter == '\\') {
            escapado = true
            return@forEachIndexed
        }
        if (caracter == '|') {
            return indice
        }
    }
    return -1
}

private fun separarCamposPkm(linea: String): List<String> {
    val campos = mutableListOf<String>()
    val actual = StringBuilder()
    var escapado = false

    linea.forEach { caracter ->
        if (escapado) {
            actual.append(caracter)
            escapado = false
            return@forEach
        }

        if (caracter == '\\') {
            escapado = true
            actual.append(caracter)
            return@forEach
        }

        if (caracter == '|') {
            campos.add(actual.toString())
            actual.clear()
            return@forEach
        }

        actual.append(caracter)
    }

    campos.add(actual.toString())
    return campos
}

private fun serializarElementosPkm(elementos: List<ElementoFormulario>): String {
    fun aPlano(
        lista: List<ElementoFormulario>,
        profundidad: Int,
        salida: MutableList<NodoSerializacionPkm>
    ) {
        lista.forEach { elemento ->
            salida.add(NodoSerializacionPkm(profundidad, elemento))
            if (elemento.elementosAnidados.isNotEmpty()) {
                aPlano(elemento.elementosAnidados, profundidad + 1, salida)
            }
        }
    }

    val nodos = mutableListOf<NodoSerializacionPkm>()
    aPlano(elementos, 0, nodos)

    return buildString {
        appendLine("PKM_FORMS_V1")
        nodos.forEach { nodo ->
            val elemento = nodo.elemento
            val opciones = elemento.opciones.joinToString(";;") { escaparCampoPkm(it) }
            val indices = elemento.indicesCorrectos.joinToString(",")
            append(elemento.tipo.name)
            append('|')
            append(escaparCampoPkm(elemento.texto))
            append('|')
            append(elemento.ancho?.toString() ?: "")
            append('|')
            append(elemento.alto?.toString() ?: "")
            append('|')
            append(elemento.pointX?.toString() ?: "")
            append('|')
            append(elemento.pointY?.toString() ?: "")
            append('|')
            append(opciones)
            append('|')
            append(indices)
            append('|')
            append(nodo.profundidad)
            append('|')
            append(escaparCampoPkm(elemento.estilo.colorTexto ?: ""))
            append('|')
            append(escaparCampoPkm(elemento.estilo.colorFondo ?: ""))
            append('|')
            append(escaparCampoPkm(elemento.estilo.familiaFuente ?: ""))
            append('|')
            append(elemento.estilo.tamanioTexto?.toString() ?: "")
            append('|')
            append(escaparCampoPkm(elemento.estilo.borde ?: ""))
            appendLine()
        }
    }
}

private fun deserializarElementosPkm(contenido: String): ResultadoCargaPkm {
    val lineas = contenido.lines()
    val elementosRaiz = mutableListOf<ElementoFormulario>()
    val errores = mutableListOf<String>()
    val nodosLectura = mutableListOf<NodoLecturaPkm>()

    lineas.forEachIndexed { indice, lineaOriginal ->
        val linea = lineaOriginal.trimEnd()
        if (linea.isBlank()) {
            return@forEachIndexed
        }
        if (indice == 0 && linea == "PKM_FORMS_V1") {
            return@forEachIndexed
        }

        val posicionSeparador = encontrarSeparadorPkm(linea)
        if (posicionSeparador <= 0) {
            errores.add("Linea ${indice + 1} invalida en .pkm")
            return@forEachIndexed
        }

        val campos = separarCamposPkm(linea)
        val tipoTexto = campos.getOrNull(0).orEmpty()
        val tipo = runCatching { TipoElementoFormulario.valueOf(tipoTexto) }.getOrNull()
        if (tipo == null) {
            errores.add("Linea ${indice + 1} con tipo desconocido: $tipoTexto")
            return@forEachIndexed
        }

        val texto = desescaparCampoPkm(campos.getOrNull(1).orEmpty())
        val ancho = campos.getOrNull(2)?.toDoubleOrNull()
        val alto = campos.getOrNull(3)?.toDoubleOrNull()
        val pointX = campos.getOrNull(4)?.toDoubleOrNull()
        val pointY = campos.getOrNull(5)?.toDoubleOrNull()
        val opciones = campos.getOrNull(6)
            ?.split(";;")
            ?.filter { it.isNotBlank() }
            ?.map { desescaparCampoPkm(it) }
            ?: emptyList()
        val indicesCorrectos = campos.getOrNull(7)
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()

        val profundidad = campos.getOrNull(8)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val estilo = EstiloFormulario(
            colorTexto = campos.getOrNull(9)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            colorFondo = campos.getOrNull(10)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            familiaFuente = campos.getOrNull(11)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            tamanioTexto = campos.getOrNull(12)?.toDoubleOrNull(),
            borde = campos.getOrNull(13)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) }
        )

        nodosLectura.add(
            NodoLecturaPkm(
                profundidad = profundidad,
                elemento = ElementoFormulario(
                    tipo = tipo,
                    texto = texto,
                    ancho = ancho,
                    alto = alto,
                    pointX = pointX,
                    pointY = pointY,
                    opciones = opciones,
                    indicesCorrectos = indicesCorrectos,
                    estilo = estilo
                )
            )
        )
    }

    fun insertarEnRuta(
        lista: MutableList<ElementoFormulario>,
        ruta: List<Int>,
        nuevo: ElementoFormulario
    ): MutableList<ElementoFormulario> {
        if (ruta.isEmpty()) {
            lista.add(nuevo)
            return lista
        }

        val copia = lista.toMutableList()
        val indiceActual = ruta.first()
        if (indiceActual !in copia.indices) {
            copia.add(nuevo)
            return copia
        }

        val elementoActual = copia[indiceActual]
        val hijosActualizados = insertarEnRuta(
            elementoActual.elementosAnidados.toMutableList(),
            ruta.drop(1),
            nuevo
        )
        copia[indiceActual] = elementoActual.copy(elementosAnidados = hijosActualizados)
        return copia
    }

    val pilaRutas = mutableListOf<List<Int>>()
    nodosLectura.forEach { nodo ->
        while (pilaRutas.size > nodo.profundidad) {
            pilaRutas.removeAt(pilaRutas.lastIndex)
        }

        if (nodo.profundidad == 0 || pilaRutas.isEmpty()) {
            elementosRaiz.add(nodo.elemento)
            val rutaNueva = listOf(elementosRaiz.lastIndex)
            pilaRutas.add(rutaNueva)
            return@forEach
        }

        val rutaPadre = pilaRutas.last()
        val copiaRaiz = insertarEnRuta(elementosRaiz.toMutableList(), rutaPadre, nodo.elemento)
        elementosRaiz.clear()
        elementosRaiz.addAll(copiaRaiz)

        fun obtenerHijosEnRuta(lista: List<ElementoFormulario>, ruta: List<Int>): List<ElementoFormulario> {
            var elemento = lista[ruta.first()]
            ruta.drop(1).forEach { indice ->
                elemento = elemento.elementosAnidados[indice]
            }
            return elemento.elementosAnidados
        }

        val hijosPadre = obtenerHijosEnRuta(elementosRaiz, rutaPadre)
        val rutaNueva = rutaPadre + (hijosPadre.lastIndex)
        pilaRutas.add(rutaNueva)
    }

    return ResultadoCargaPkm(elementos = elementosRaiz, errores = errores)
}

private suspend fun leerTextoDeUri(contexto: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val flujo = contexto.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir archivo")
        flujo.bufferedReader().use { it.readText() }
    }
}

private suspend fun escribirTextoEnUri(contexto: Context, uri: Uri, contenido: String) {
    withContext(Dispatchers.IO) {
        val flujo = contexto.contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir destino")
        flujo.bufferedWriter().use { it.write(contenido) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PKM_FORMSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaAnalizadorFormulario(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private data class ResultadoEnvioFormulario(
    val totalPreguntas: Int,
    val respondidas: Int,
    val sinResponder: Int,
    val cerradasCorregibles: Int,
    val cerradasValidas: Int,
    val cerradasInvalidas: Int,
    val detalleInvalidas: List<String>
)

private fun esPregunta(tipo: TipoElementoFormulario): Boolean {
    return tipo == TipoElementoFormulario.OPEN_QUESTION ||
        tipo == TipoElementoFormulario.DROP_QUESTION ||
        tipo == TipoElementoFormulario.SELECT_QUESTION ||
        tipo == TipoElementoFormulario.MULTIPLE_QUESTION
}

private fun aplanarConIndiceGlobal(elementos: List<ElementoFormulario>): List<NodoRender> {
    val salida = mutableListOf<NodoRender>()
    var indice = 0

    fun recorrer(lista: List<ElementoFormulario>, nivel: Int) {
        lista.forEach { elemento ->
            salida.add(NodoRender(indice = indice, nivel = nivel, elemento = elemento))
            indice++
            if (elemento.elementosAnidados.isNotEmpty()) {
                recorrer(elemento.elementosAnidados, nivel + 1)
            }
        }
    }

    recorrer(elementos, 0)
    return salida
}

private fun preguntasConIndiceGlobal(elementos: List<ElementoFormulario>): List<NodoRender> {
    return aplanarConIndiceGlobal(elementos).filter { esPregunta(it.elemento.tipo) }
}

private fun extraerCeldasTabla(elementoTabla: ElementoFormulario, nivelContenedor: Int): List<CeldaTabla> {
    val celdas = mutableListOf<CeldaTabla>()

    fun recorrer(lista: List<ElementoFormulario>, nivel: Int) {
        lista.forEach { elemento ->
            celdas.add(CeldaTabla(nivel, elemento))
            if (elemento.tipo == TipoElementoFormulario.SECTION || elemento.tipo == TipoElementoFormulario.TABLE) {
                recorrer(elemento.elementosAnidados, nivel + 1)
            }
        }
    }

    recorrer(elementoTabla.elementosAnidados, nivelContenedor + 1)
    return celdas
}

private fun validarRespuestaCerrada(elemento: ElementoFormulario, valor: String): Boolean {
    if (elemento.tipo == TipoElementoFormulario.DROP_QUESTION || elemento.tipo == TipoElementoFormulario.SELECT_QUESTION) {
        val indice = valor.toIntOrNull() ?: return false
        if (indice < 0) return false
        if (elemento.opciones.isNotEmpty() && indice >= elemento.opciones.size) return false
        if (elemento.indicesCorrectos.isEmpty()) return true
        return elemento.indicesCorrectos.contains(indice)
    }

    if (elemento.tipo == TipoElementoFormulario.MULTIPLE_QUESTION) {
        val partes = valor.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (partes.isEmpty()) return false
        val indices = partes.map {
            it.toIntOrNull() ?: return false
        }
        if (indices.any { it < 0 }) return false
        if (elemento.opciones.isNotEmpty() && indices.any { it >= elemento.opciones.size }) return false
        if (elemento.indicesCorrectos.isEmpty()) return true
        return indices.sorted() == elemento.indicesCorrectos.sorted()
    }

    return false
}

private fun corregirRespuestas(
    elementos: List<ElementoFormulario>,
    respuestas: Map<Int, String>
): ResultadoEnvioFormulario {
    var totalPreguntas = 0
    var respondidas = 0
    var cerradasCorregibles = 0
    var cerradasValidas = 0
    val detalleInvalidas = mutableListOf<String>()

    val preguntas = preguntasConIndiceGlobal(elementos)
    preguntas.forEach { nodo ->
        val indice = nodo.indice
        val elemento = nodo.elemento

        totalPreguntas++
        val respuesta = respuestas[indice]?.trim().orEmpty()
        if (respuesta.isNotEmpty()) {
            respondidas++
        }

        if (elemento.tipo == TipoElementoFormulario.OPEN_QUESTION || respuesta.isEmpty()) {
            return@forEach
        }

        cerradasCorregibles++
        val esValida = validarRespuestaCerrada(elemento, respuesta)
        if (esValida) {
            cerradasValidas++
        } else {
            val etiqueta = when (elemento.tipo) {
                TipoElementoFormulario.DROP_QUESTION -> "DROP"
                TipoElementoFormulario.SELECT_QUESTION -> "SELECT"
                TipoElementoFormulario.MULTIPLE_QUESTION -> "MULTIPLE"
                else -> ""
            }
            detalleInvalidas.add("$etiqueta '${elemento.texto}' -> '$respuesta'")
        }
    }

    val sinResponder = totalPreguntas - respondidas
    val cerradasInvalidas = cerradasCorregibles - cerradasValidas

    return ResultadoEnvioFormulario(
        totalPreguntas = totalPreguntas,
        respondidas = respondidas,
        sinResponder = sinResponder,
        cerradasCorregibles = cerradasCorregibles,
        cerradasValidas = cerradasValidas,
        cerradasInvalidas = cerradasInvalidas,
        detalleInvalidas = detalleInvalidas
    )
}

private fun descripcionEstilo(estilo: EstiloFormulario): String {
    val partes = mutableListOf<String>()
    estilo.colorTexto?.let { partes.add("color=$it") }
    estilo.colorFondo?.let { partes.add("bg=$it") }
    estilo.familiaFuente?.let { partes.add("font=$it") }
    estilo.tamanioTexto?.let { partes.add("size=$it") }
    estilo.borde?.let { partes.add("border=$it") }
    return if (partes.isEmpty()) "" else " | ${partes.joinToString(", ")}"
}

@Composable
fun PantallaAnalizadorFormulario(modifier: Modifier = Modifier) {
    val servicioAnalisis = remember { ServicioAnalisisFormulario() }
    val alcanceCorrutina = rememberCoroutineScope()
    val contexto = LocalContext.current
    var estadoCodigo by remember { mutableStateOf(TextFieldValue(CODIGO_DEMO)) } // DEMO: comentar esta linea para APK limpia
    var ejecutando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoAnalisisFormulario?>(null) }
    var elementosAcumulados by remember { mutableStateOf<List<ElementoFormulario>>(emptyList()) }
    var modoContestar by remember { mutableStateOf(false) }
    var menuColorAbierto by remember { mutableStateOf(false) }
    val respuestasUsuario = remember { mutableStateMapOf<Int, String>() }
    val mensajesEnvio = remember { mutableStateListOf<String>() }
    val erroresCargaPkm = remember { mutableStateListOf<String>() }
    var resultadoEnvio by remember { mutableStateOf<ResultadoEnvioFormulario?>(null) }
    var mensajeArchivo by remember { mutableStateOf("") }

    val lanzadorGuardarForm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        alcanceCorrutina.launch {
            runCatching {
                escribirTextoEnUri(contexto, uri, estadoCodigo.text)
            }.onSuccess {
                mensajeArchivo = "Archivo .form guardado."
            }.onFailure {
                mensajeArchivo = "Error al guardar .form: ${it.message ?: "desconocido"}"
            }
        }
    }

    val lanzadorAbrirForm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        alcanceCorrutina.launch {
            runCatching {
                leerTextoDeUri(contexto, uri)
            }.onSuccess { contenido ->
                estadoCodigo = TextFieldValue(contenido, TextRange(contenido.length))
                mensajeArchivo = "Archivo .form cargado."
            }.onFailure {
                mensajeArchivo = "Error al abrir .form: ${it.message ?: "desconocido"}"
            }
        }
    }

    val lanzadorGuardarPkm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        alcanceCorrutina.launch {
            runCatching {
                val contenidoPkm = serializarElementosPkm(elementosAcumulados)
                escribirTextoEnUri(contexto, uri, contenidoPkm)
            }.onSuccess {
                mensajeArchivo = "Archivo .pkm guardado."
            }.onFailure {
                mensajeArchivo = "Error al guardar .pkm: ${it.message ?: "desconocido"}"
            }
        }
    }

    val lanzadorAbrirPkm = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        alcanceCorrutina.launch {
            runCatching {
                val contenido = leerTextoDeUri(contexto, uri)
                deserializarElementosPkm(contenido)
            }.onSuccess { carga ->
                resultado = null
                elementosAcumulados = carga.elementos
                modoContestar = false
                respuestasUsuario.clear()
                mensajesEnvio.clear()
                resultadoEnvio = null
                erroresCargaPkm.clear()
                erroresCargaPkm.addAll(carga.errores)
                mensajeArchivo = if (carga.errores.isEmpty()) {
                    "Archivo .pkm cargado: ${carga.elementos.size} elementos."
                } else {
                    "Archivo .pkm cargado con ${carga.errores.size} lineas omitidas."
                }
            }.onFailure {
                mensajeArchivo = "Error al abrir .pkm: ${it.message ?: "desconocido"}"
            }
        }
    }

    LaunchedEffect(elementosAcumulados) {
        if (elementosAcumulados.isEmpty()) {
            modoContestar = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "PKM_FORMS", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Editor .form", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = estadoCodigo,
            onValueChange = { estadoCodigo = it },
            label = { Text("Codigo fuente") },
            visualTransformation = transformacionSintaxis,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    estadoCodigo = TextFieldValue(
                        text = PLANTILLA_BASE,
                        selection = TextRange(PLANTILLA_BASE.length)
                    )
                }
            ) {
                Text("Insertar plantilla")
            }

            Button(onClick = { menuColorAbierto = true }) {
                Text("Selector de colores")
            }

            Button(onClick = { lanzadorGuardarForm.launch("formulario.form") }) {
                Text("Guardar .form")
            }

            Button(onClick = { lanzadorAbrirForm.launch(arrayOf("text/*", "application/octet-stream")) }) {
                Text("Abrir .form")
            }

            Button(
                onClick = {
                    if (elementosAcumulados.isNotEmpty()) {
                        lanzadorGuardarPkm.launch("formulario.pkm")
                    } else {
                        mensajeArchivo = "Ejecuta o carga un formulario antes de guardar .pkm."
                    }
                }
            ) {
                Text("Guardar .pkm")
            }

            Button(onClick = { lanzadorAbrirPkm.launch(arrayOf("text/*", "application/octet-stream")) }) {
                Text("Abrir .pkm")
            }

            DropdownMenu(
                expanded = menuColorAbierto,
                onDismissRequest = { menuColorAbierto = false }
            ) {
                opcionesColor.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text("${opcion.first} (${opcion.second})") },
                        onClick = {
                            val bloqueColor =
                                "string color_seleccionado = \"${opcion.second}\";\n" +
                                    "TEXT \"Color elegido ${opcion.first}: ${opcion.second}\";\n"
                            estadoCodigo = insertarTextoEnCursor(estadoCodigo, bloqueColor)
                            menuColorAbierto = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    alcanceCorrutina.launch {
                        ejecutando = true
                        val resultadoNuevo = withContext(Dispatchers.Default) {
                            servicioAnalisis.ejecutar(estadoCodigo.text)
                        }
                        resultado = resultadoNuevo
                        elementosAcumulados = resultadoNuevo.elementosFormulario
                        respuestasUsuario.clear()
                        mensajesEnvio.clear()
                        erroresCargaPkm.clear()
                        resultadoEnvio = null
                        ejecutando = false
                    }
                },
                enabled = !ejecutando
            ) {
                Text("Ejecutar (reemplazar)")
            }

            Button(
                onClick = {
                    alcanceCorrutina.launch {
                        ejecutando = true
                        val resultadoNuevo = withContext(Dispatchers.Default) {
                            servicioAnalisis.ejecutar(estadoCodigo.text)
                        }
                        resultado = resultadoNuevo
                        elementosAcumulados = elementosAcumulados + resultadoNuevo.elementosFormulario
                        mensajesEnvio.clear()
                        erroresCargaPkm.clear()
                        resultadoEnvio = null
                        ejecutando = false
                    }
                },
                enabled = !ejecutando
            ) {
                Text("Agregar")
            }

            if (elementosAcumulados.isNotEmpty()) {
                Button(onClick = { modoContestar = !modoContestar }) {
                    Text(if (modoContestar) "Ver formulario" else "Contestar")
                }
            }

            if (ejecutando) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (mensajeArchivo.isNotEmpty()) {
            Text(mensajeArchivo)
        }

        resultado?.let { analisis ->
            Text("Tokens: ${analisis.cantidadTokens}")
            Text("Instrucciones: ${analisis.cantidadInstrucciones}")
            Text("Errores lexicos: ${analisis.erroresLexicos.size}")
            Text("Errores sintacticos: ${analisis.erroresSintacticos.size}")
            Text("Errores semanticos: ${analisis.erroresSemanticos.size}")
            Text("Advertencias: ${analisis.advertenciasSemanticas.size}")

            if (analisis.erroresLexicos.isNotEmpty()) {
                Text("Detalle lexico:")
                analisis.erroresLexicos.forEach {
                    Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
                }
            }

            if (analisis.erroresSintacticos.isNotEmpty()) {
                Text("Detalle sintactico:")
                analisis.erroresSintacticos.forEach {
                    Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
                }
            }

            if (analisis.erroresSemanticos.isNotEmpty()) {
                Text("Detalle semantico:")
                analisis.erroresSemanticos.forEach {
                    Text("- $it")
                }
            }

            if (analisis.advertenciasSemanticas.isNotEmpty()) {
                Text("Detalle advertencias:")
                analisis.advertenciasSemanticas.forEach {
                    Text("- $it")
                }
            }
        }

        if (erroresCargaPkm.isNotEmpty()) {
            Text("Detalle .pkm omitido:")
            erroresCargaPkm.forEach {
                Text("- $it")
            }
        }

        if (elementosAcumulados.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (modoContestar) "Modo contestar" else "Formulario renderizado",
                style = MaterialTheme.typography.titleMedium
            )
            FormularioRenderizado(
                elementos = elementosAcumulados,
                modoContestar = modoContestar,
                respuestas = respuestasUsuario
            )
            if (modoContestar) {
                TextButton(
                    onClick = {
                        val correccion = corregirRespuestas(elementosAcumulados, respuestasUsuario)
                        resultadoEnvio = correccion
                        mensajesEnvio.clear()
                        if (correccion.totalPreguntas == 0) {
                            mensajesEnvio.add("No hay preguntas para enviar.")
                        } else {
                            mensajesEnvio.add("Formulario enviado.")
                            if (correccion.sinResponder > 0) {
                                mensajesEnvio.add("Faltan ${correccion.sinResponder} respuestas por completar.")
                            }
                            if (correccion.cerradasCorregibles > 0) {
                                mensajesEnvio.add(
                                    "Correccion cerradas: ${correccion.cerradasValidas}/${correccion.cerradasCorregibles} validas."
                                )
                                if (correccion.cerradasInvalidas > 0) {
                                    mensajesEnvio.add("Usa indices desde 0 en preguntas cerradas.")
                                }
                            }
                        }
                    }
                ) {
                    Text("Enviar")
                }

                if (mensajesEnvio.isNotEmpty()) {
                    mensajesEnvio.forEach { mensaje ->
                        Text(mensaje)
                    }
                }

                resultadoEnvio?.let { envio ->
                    Text("Resumen envio: ${envio.respondidas}/${envio.totalPreguntas} respondidas")
                    if (envio.detalleInvalidas.isNotEmpty()) {
                        Text("Detalle respuestas cerradas invalidas:")
                        envio.detalleInvalidas.forEach {
                            Text("- $it")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioRenderizado(
    elementos: List<ElementoFormulario>,
    modoContestar: Boolean,
    respuestas: MutableMap<Int, String>
) {
    val nodos = aplanarConIndiceGlobal(elementos)
    fun indiceGlobal(elemento: ElementoFormulario): Int {
        val porReferencia = nodos.indexOfFirst { it.elemento === elemento }
        if (porReferencia >= 0) return nodos[porReferencia].indice
        val porIgualdad = nodos.indexOfFirst { it.elemento == elemento }
        return if (porIgualdad >= 0) nodos[porIgualdad].indice else 0
    }

    @Composable
    fun renderElemento(elemento: ElementoFormulario, indice: Int, nivel: Int) {
        val prefijo = "  ".repeat(nivel)
        when (elemento.tipo) {
            TipoElementoFormulario.SECTION,
            TipoElementoFormulario.TABLE -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val tipo = if (elemento.tipo == TipoElementoFormulario.SECTION) "SECTION" else "TABLE"
                        Text(
                            text = "$prefijo$tipo: ${elemento.texto} (w=${elemento.ancho ?: "-"}, h=${elemento.alto ?: "-"}, x=${elemento.pointX ?: "-"}, y=${elemento.pointY ?: "-"})${descripcionEstilo(elemento.estilo)}",
                            style = MaterialTheme.typography.titleSmall
                        )

                        if (elemento.tipo == TipoElementoFormulario.TABLE) {
                            val celdas = extraerCeldasTabla(elemento, nivel)
                            if (celdas.isNotEmpty()) {
                                val columnas = if (celdas.size <= 2) celdas.size else 3
                                val filas = ceil(celdas.size.toDouble() / columnas.toDouble()).toInt()
                                Text("${prefijo}Grid: $filas x $columnas")

                                val celdasEnFilas = celdas.chunked(columnas)
                                celdasEnFilas.forEach { filaCeldas ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(columnas) { indiceColumna ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                val celda = filaCeldas.getOrNull(indiceColumna)
                                                if (celda != null) {
                                                    Card(modifier = Modifier.fillMaxWidth()) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            renderElemento(celda.elemento, indiceGlobal(celda.elemento), celda.nivel)
                                                        }
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            elemento.elementosAnidados.forEach { hijo ->
                                renderElemento(hijo, indiceGlobal(hijo), nivel + 1)
                            }
                        }
                    }
                }
            }

            TipoElementoFormulario.TEXT -> {
                Text(
                    text = "$prefijo${elemento.texto}${descripcionEstilo(elemento.estilo)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            TipoElementoFormulario.OPEN_QUESTION -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${prefijo}Abierta: ${elemento.texto}${descripcionEstilo(elemento.estilo)}")
                    if (modoContestar) {
                        OutlinedTextField(
                            value = respuestas[indice] ?: "",
                            onValueChange = { respuestas[indice] = it },
                            label = { Text("Tu respuesta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            TipoElementoFormulario.DROP_QUESTION,
            TipoElementoFormulario.SELECT_QUESTION,
            TipoElementoFormulario.MULTIPLE_QUESTION -> {
                val tipo = when (elemento.tipo) {
                    TipoElementoFormulario.DROP_QUESTION -> "Drop"
                    TipoElementoFormulario.SELECT_QUESTION -> "Select"
                    else -> "Multiple"
                }
                Text("$prefijo$tipo: ${elemento.texto}${descripcionEstilo(elemento.estilo)}")
                if (elemento.opciones.isNotEmpty()) {
                    Text("$prefijo Opciones: ${elemento.opciones.joinToString(" | ")}")
                }
                if (elemento.indicesCorrectos.isNotEmpty()) {
                    Text("$prefijo Correctas: ${elemento.indicesCorrectos.joinToString(",")}")
                }
                if (modoContestar) {
                    OutlinedTextField(
                        value = respuestas[indice] ?: "",
                        onValueChange = { respuestas[indice] = it },
                        label = {
                            val etiqueta = if (elemento.tipo == TipoElementoFormulario.MULTIPLE_QUESTION) {
                                "Indices separados por coma"
                            } else {
                                "Indice de respuesta (desde 0)"
                            }
                            Text(etiqueta)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        nodos.forEach { nodo ->
            if (nodo.nivel == 0) {
                renderElemento(nodo.elemento, nodo.indice, nodo.nivel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PKM_FORMSTheme {
        PantallaAnalizadorFormulario()
    }
}
