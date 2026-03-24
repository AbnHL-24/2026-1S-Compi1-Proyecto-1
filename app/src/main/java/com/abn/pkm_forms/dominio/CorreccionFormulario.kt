package com.abn.pkm_forms.dominio

import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

data class ResultadoEnvioFormulario(
    val totalPreguntas: Int,
    val respondidas: Int,
    val sinResponder: Int,
    val cerradasCorregibles: Int,
    val cerradasValidas: Int,
    val cerradasInvalidas: Int,
    val detalleInvalidas: List<String>
)

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

fun corregirRespuestas(
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
