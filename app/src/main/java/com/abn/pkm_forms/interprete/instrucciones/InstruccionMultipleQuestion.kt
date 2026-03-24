package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

class InstruccionMultipleQuestion(
    private val etiqueta: Expresion,
    private val ancho: Expresion?,
    private val alto: Expresion?,
    private val opciones: List<Expresion>,
    private val correctos: List<Expresion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (textoResuelto, errorTexto) = resolverTexto(etiqueta, arbol, tabla, "label", fila, columna)
        if (errorTexto != null) return errorTexto

        val (anchoResuelto, errorAncho) = resolverNumeroNoNegativo(ancho, arbol, tabla, "width", fila, columna)
        if (errorAncho != null) return errorAncho
        val (altoResuelto, errorAlto) = resolverNumeroNoNegativo(alto, arbol, tabla, "height", fila, columna)
        if (errorAlto != null) return errorAlto

        val (opcionesResueltas, errorOpciones) = resolverOpciones(opciones, arbol, tabla, fila, columna)
        if (errorOpciones != null) return errorOpciones
        val opcionesFinales = opcionesResueltas ?: emptyList()

        val (indicesCorrectos, errorCorrectos) = resolverIndicesCorrectos(correctos, arbol, tabla, opcionesFinales.size, fila, columna)
        if (errorCorrectos != null) return errorCorrectos

        arbol.agregarElemento(
            ElementoFormulario(
                tipo = TipoElementoFormulario.MULTIPLE_QUESTION,
                texto = textoResuelto.orEmpty(),
                ancho = anchoResuelto,
                alto = altoResuelto,
                opciones = opcionesFinales,
                indicesCorrectos = indicesCorrectos ?: emptyList()
            )
        )
        return null
    }
}
