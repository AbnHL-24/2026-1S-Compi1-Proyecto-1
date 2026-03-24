package com.abn.pkm_forms.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

val opcionesColor = listOf(
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

val transformacionSintaxis = VisualTransformation { textoOriginal ->
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

fun insertarTextoEnCursor(estadoActual: TextFieldValue, textoInsertar: String): TextFieldValue {
    val largo = estadoActual.text.length
    val inicioSeleccion = estadoActual.selection.start.coerceIn(0, largo)
    val finSeleccion = estadoActual.selection.end.coerceIn(0, largo)
    val inicio = minOf(inicioSeleccion, finSeleccion)
    val fin = maxOf(inicioSeleccion, finSeleccion)
    val textoNuevo = estadoActual.text.replaceRange(inicio, fin, textoInsertar)
    val cursorNuevo = inicio + textoInsertar.length
    return estadoActual.copy(text = textoNuevo, selection = TextRange(cursorNuevo, cursorNuevo))
}
