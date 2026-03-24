package com.abn.pkm_forms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario
import com.abn.pkm_forms.ui.theme.PKM_FORMSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CODIGO_DEMO = """
$ Demo inicial
number edad = 10;
string nombre = "Ash";
number indice = 2;
number total = (edad * 2) + 5;
string titulo = "Entrenador: " + nombre;
SECTION "Inicio";
TEXT titulo;
OPEN_QUESTION "Cual es tu pokemon favorito?";
DROP_QUESTION "Elige tipo";
SELECT_QUESTION "Elige una opcion";
MULTIPLE_QUESTION "Selecciona multiples";
/* comentario de bloque */
edad = total - 1;
"""

private const val PLANTILLA_BASE = """
$ Plantilla base
number edad = 18;
string nombre = "Misty";
SECTION "Datos";
TEXT "Bienvenido";
OPEN_QUESTION "Cual es tu pokemon favorito?";
DROP_QUESTION "Elige tipo";
SELECT_QUESTION "Elige una opcion";
MULTIPLE_QUESTION "Selecciona multiples";
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
private val regexReservadas = Regex("\\b(SECTION|TEXT|OPEN_QUESTION|DROP_QUESTION|SELECT_QUESTION|MULTIPLE_QUESTION|number|string|true|false)\\b")
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

private fun validarRespuestaCerrada(tipo: TipoElementoFormulario, valor: String): Boolean {
    if (tipo == TipoElementoFormulario.DROP_QUESTION || tipo == TipoElementoFormulario.SELECT_QUESTION) {
        val indice = valor.toIntOrNull() ?: return false
        return indice >= 0
    }

    if (tipo == TipoElementoFormulario.MULTIPLE_QUESTION) {
        val partes = valor.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (partes.isEmpty()) return false
        return partes.all {
            val indice = it.toIntOrNull() ?: return@all false
            indice >= 0
        }
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

    elementos.forEachIndexed { indice, elemento ->
        if (!esPregunta(elemento.tipo)) {
            return@forEachIndexed
        }

        totalPreguntas++
        val respuesta = respuestas[indice]?.trim().orEmpty()
        if (respuesta.isNotEmpty()) {
            respondidas++
        }

        if (elemento.tipo == TipoElementoFormulario.OPEN_QUESTION || respuesta.isEmpty()) {
            return@forEachIndexed
        }

        cerradasCorregibles++
        val esValida = validarRespuestaCerrada(elemento.tipo, respuesta)
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

@Composable
fun PantallaAnalizadorFormulario(modifier: Modifier = Modifier) {
    val servicioAnalisis = remember { ServicioAnalisisFormulario() }
    val alcanceCorrutina = rememberCoroutineScope()
    var estadoCodigo by remember { mutableStateOf(TextFieldValue(CODIGO_DEMO)) } // DEMO: comentar esta linea para APK limpia
    var ejecutando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoAnalisisFormulario?>(null) }
    var elementosAcumulados by remember { mutableStateOf<List<ElementoFormulario>>(emptyList()) }
    var modoContestar by remember { mutableStateOf(false) }
    var menuColorAbierto by remember { mutableStateOf(false) }
    val respuestasUsuario = remember { mutableStateMapOf<Int, String>() }
    val mensajesEnvio = remember { mutableStateListOf<String>() }
    var resultadoEnvio by remember { mutableStateOf<ResultadoEnvioFormulario?>(null) }

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
                        val inicioNuevos = elementosAcumulados.size
                        resultado = resultadoNuevo
                        elementosAcumulados = elementosAcumulados + resultadoNuevo.elementosFormulario
                        resultadoNuevo.elementosFormulario.forEachIndexed { indiceLocal, _ ->
                            respuestasUsuario[inicioNuevos + indiceLocal] = ""
                        }
                        mensajesEnvio.clear()
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

        resultado?.let { analisis ->
            Text("Tokens: ${analisis.cantidadTokens}")
            Text("Instrucciones: ${analisis.cantidadInstrucciones}")
            Text("Errores lexicos: ${analisis.erroresLexicos.size}")
            Text("Errores sintacticos: ${analisis.erroresSintacticos.size}")
            Text("Errores semanticos: ${analisis.erroresSemanticos.size}")

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
}

@Composable
private fun FormularioRenderizado(
    elementos: List<ElementoFormulario>,
    modoContestar: Boolean,
    respuestas: MutableMap<Int, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        elementos.forEachIndexed { indice, elemento ->
            when (elemento.tipo) {
                TipoElementoFormulario.SECTION -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SECTION: ${elemento.texto}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                TipoElementoFormulario.TEXT -> {
                    Text(
                        text = elemento.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                TipoElementoFormulario.OPEN_QUESTION -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Abierta: ${elemento.texto}")
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
                    Text("$tipo: ${elemento.texto}")
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
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PKM_FORMSTheme {
        PantallaAnalizadorFormulario()
    }
}
