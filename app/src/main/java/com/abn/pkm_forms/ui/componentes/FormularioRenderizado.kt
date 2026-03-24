package com.abn.pkm_forms.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abn.pkm_forms.dominio.aplanarElementosConIndiceGlobal
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.EstiloFormulario
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario
import kotlin.math.ceil

private data class CeldaTabla(
    val nivel: Int,
    val elemento: ElementoFormulario
)

private fun descripcionEstilo(estilo: EstiloFormulario): String {
    val partes = mutableListOf<String>()
    estilo.colorTexto?.let { partes.add("color=$it") }
    estilo.colorFondo?.let { partes.add("bg=$it") }
    estilo.familiaFuente?.let { partes.add("font=$it") }
    estilo.tamanioTexto?.let { partes.add("size=$it") }
    estilo.borde?.let { partes.add("border=$it") }
    return if (partes.isEmpty()) "" else " | ${partes.joinToString(", ")}"
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

@Composable
fun FormularioRenderizado(
    elementos: List<ElementoFormulario>,
    modoContestar: Boolean,
    respuestas: MutableMap<Int, String>
) {
    val nodos = aplanarElementosConIndiceGlobal(elementos)

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
