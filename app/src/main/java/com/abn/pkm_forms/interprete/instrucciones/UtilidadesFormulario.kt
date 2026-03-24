package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.EstiloFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

internal fun resolverResultado(
    expresion: Expresion,
    arbol: Arbol,
    tabla: TablaSimbolos
): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
    return expresion.interpretar(arbol, tabla)
}

internal fun resolverTexto(
    expresion: Expresion,
    arbol: Arbol,
    tabla: TablaSimbolos,
    nombreCampo: String,
    fila: Int,
    columna: Int
): Pair<String?, ErrorInterpretacion?> {
    val (resultado, error) = resolverResultado(expresion, arbol, tabla)
    if (error != null) return Pair(null, error)
    if (resultado == null || resultado.tipo != TiposDato.STRING) {
        return Pair(
            null,
            ErrorInterpretacion("Semantico", "El campo '$nombreCampo' requiere string", fila, columna)
        )
    }
    return Pair(resultado.valor as String, null)
}

internal fun resolverNumeroNoNegativo(
    expresion: Expresion?,
    arbol: Arbol,
    tabla: TablaSimbolos,
    nombreCampo: String,
    fila: Int,
    columna: Int
): Pair<Double?, ErrorInterpretacion?> {
    if (expresion == null) return Pair(null, null)

    val (resultado, error) = resolverResultado(expresion, arbol, tabla)
    if (error != null) return Pair(null, error)
    if (resultado == null || resultado.tipo != TiposDato.NUMBER) {
        return Pair(
            null,
            ErrorInterpretacion("Semantico", "El campo '$nombreCampo' requiere number", fila, columna)
        )
    }

    val numero = resultado.valor as Double
    if (numero < 0) {
        return Pair(
            null,
            ErrorInterpretacion(
                "Semantico",
                "El atributo '$nombreCampo' no acepta valores negativos",
                fila,
                columna
            )
        )
    }
    return Pair(numero, null)
}

internal fun resolverOpciones(
    expresiones: List<Expresion>,
    arbol: Arbol,
    tabla: TablaSimbolos,
    fila: Int,
    columna: Int
): Pair<List<String>?, ErrorInterpretacion?> {
    val opciones = mutableListOf<String>()
    expresiones.forEach { expresion ->
        val (opcion, error) = resolverTexto(expresion, arbol, tabla, "options", fila, columna)
        if (error != null) return Pair(null, error)
        if (opcion != null) {
            opciones.add(opcion)
        }
    }
    return Pair(opciones, null)
}

internal fun resolverIndicesCorrectos(
    expresiones: List<Expresion>,
    arbol: Arbol,
    tabla: TablaSimbolos,
    totalOpciones: Int,
    fila: Int,
    columna: Int
): Pair<List<Int>?, ErrorInterpretacion?> {
    val indices = mutableListOf<Int>()
    expresiones.forEach { expresion ->
        val (resultado, error) = resolverResultado(expresion, arbol, tabla)
        if (error != null) return Pair(null, error)
        if (resultado == null || resultado.tipo != TiposDato.NUMBER) {
            return Pair(null, ErrorInterpretacion("Semantico", "Los indices correctos requieren number", fila, columna))
        }

        val numero = resultado.valor as Double
        val entero = numero.toInt()
        if (numero != entero.toDouble()) {
            return Pair(
                null,
                ErrorInterpretacion("Semantico", "Los indices correctos deben ser enteros", fila, columna)
            )
        }

        if (entero < 0 || entero >= totalOpciones) {
            return Pair(
                null,
                ErrorInterpretacion(
                    "Semantico",
                    "Indice correcto fuera de rango: $entero",
                    fila,
                    columna
                )
            )
        }

        indices.add(entero)
    }

    return Pair(indices, null)
}

internal fun resolverEstilo(
    atributos: Map<String, Expresion>,
    arbol: Arbol,
    tabla: TablaSimbolos,
    fila: Int,
    columna: Int
): Pair<EstiloFormulario, ErrorInterpretacion?> {
    fun texto(nombre: String): Pair<String?, ErrorInterpretacion?> {
        val expresion = atributos[nombre] ?: return Pair(null, null)
        return resolverTexto(expresion, arbol, tabla, nombre, fila, columna)
    }

    fun numero(nombre: String): Pair<Double?, ErrorInterpretacion?> {
        val expresion = atributos[nombre] ?: return Pair(null, null)
        return resolverNumeroNoNegativo(expresion, arbol, tabla, nombre, fila, columna)
    }

    val (colorTexto, errorColorTexto) = texto("style_color")
    if (errorColorTexto != null) return Pair(EstiloFormulario(), errorColorTexto)
    val (colorFondo, errorColorFondo) = texto("style_background")
    if (errorColorFondo != null) return Pair(EstiloFormulario(), errorColorFondo)
    val (fuente, errorFuente) = texto("style_font")
    if (errorFuente != null) return Pair(EstiloFormulario(), errorFuente)
    val (tamanio, errorTamanio) = numero("style_text_size")
    if (errorTamanio != null) return Pair(EstiloFormulario(), errorTamanio)
    val (borde, errorBorde) = texto("style_border")
    if (errorBorde != null) return Pair(EstiloFormulario(), errorBorde)

    return Pair(
        EstiloFormulario(
            colorTexto = colorTexto,
            colorFondo = colorFondo,
            familiaFuente = fuente,
            tamanioTexto = tamanio,
            borde = borde
        ),
        null
    )
}

internal fun resolverEstiloDesdeExpresiones(
    colorTextoExp: Expresion?,
    colorFondoExp: Expresion?,
    fuenteExp: Expresion?,
    tamanioExp: Expresion?,
    bordeExp: Expresion?,
    arbol: Arbol,
    tabla: TablaSimbolos,
    fila: Int,
    columna: Int
): Pair<EstiloFormulario, ErrorInterpretacion?> {
    val atributos = mutableMapOf<String, Expresion>()
    if (colorTextoExp != null) atributos["style_color"] = colorTextoExp
    if (colorFondoExp != null) atributos["style_background"] = colorFondoExp
    if (fuenteExp != null) atributos["style_font"] = fuenteExp
    if (tamanioExp != null) atributos["style_text_size"] = tamanioExp
    if (bordeExp != null) atributos["style_border"] = bordeExp
    return resolverEstilo(atributos, arbol, tabla, fila, columna)
}
