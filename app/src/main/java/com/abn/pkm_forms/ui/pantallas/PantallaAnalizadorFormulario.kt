package com.abn.pkm_forms.ui.pantallas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.abn.pkm_forms.analisis.ResultadoAnalisisFormulario
import com.abn.pkm_forms.analisis.ServicioAnalisisFormulario
import com.abn.pkm_forms.datos.deserializarElementosPkm
import com.abn.pkm_forms.datos.escribirTextoEnUri
import com.abn.pkm_forms.datos.leerTextoDeUri
import com.abn.pkm_forms.datos.serializarElementosPkm
import com.abn.pkm_forms.dominio.ResultadoEnvioFormulario
import com.abn.pkm_forms.dominio.corregirRespuestas
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.ui.componentes.FormularioRenderizado
import com.abn.pkm_forms.ui.componentes.PanelAnalisis
import com.abn.pkm_forms.ui.editor.insertarTextoEnCursor
import com.abn.pkm_forms.ui.editor.opcionesColor
import com.abn.pkm_forms.ui.editor.transformacionSintaxis
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

        resultado?.let {
            PanelAnalisis(it)
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

@Preview(showBackground = true)
@Composable
fun PantallaAnalizadorFormularioPreview() {
    PKM_FORMSTheme {
        PantallaAnalizadorFormulario()
    }
}
