package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.expresiones.ExpresionWhoIsThatPokemon
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionSelectQuestion(
    private val etiqueta: Expresion,
    private val ancho: Expresion?,
    private val alto: Expresion?,
    private val opciones: List<Expresion>,
    private val opcionPokemon: ExpresionWhoIsThatPokemon?,
    private val correctos: List<Expresion>,
    private val colorTexto: Expresion?,
    private val colorFondo: Expresion?,
    private val fuente: Expresion?,
    private val tamanioTexto: Expresion?,
    private val borde: Expresion?,
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

        val opcionesFinales = mutableListOf<String>()
        if (opcionPokemon != null) {
            val (resultadoPokemon, errorPokemon) = opcionPokemon.interpretar(arbol, tabla)
            if (errorPokemon != null) return errorPokemon
            if (resultadoPokemon == null || resultadoPokemon.tipo != TiposDato.LISTA_STRING) {
                return ErrorInterpretacion("Semantico", "who_is_that_pokemon no genero lista valida", fila, columna)
            }
            @Suppress("UNCHECKED_CAST")
            opcionesFinales.addAll(resultadoPokemon.valor as List<String>)
        } else {
            val (opcionesResueltas, errorOpciones) = resolverOpciones(opciones, arbol, tabla, fila, columna)
            if (errorOpciones != null) return errorOpciones
            opcionesFinales.addAll(opcionesResueltas ?: emptyList())
        }

        if (opcionesFinales.size > 5) {
            arbol.agregarAdvertencia(
                "SELECT_QUESTION '${textoResuelto.orEmpty()}' tiene ${opcionesFinales.size} opciones; se recomienda maximo 5"
            )
        }

        val (indicesCorrectos, errorCorrectos) = resolverIndicesCorrectos(correctos, arbol, tabla, opcionesFinales.size, fila, columna)
        if (errorCorrectos != null) return errorCorrectos
        if (indicesCorrectos != null && indicesCorrectos.size > 1) {
            return ErrorInterpretacion("Semantico", "SELECT_QUESTION solo permite un indice correcto", fila, columna)
        }

        val (estiloResuelto, errorEstilo) = resolverEstiloDesdeExpresiones(
            colorTexto,
            colorFondo,
            fuente,
            tamanioTexto,
            borde,
            arbol,
            tabla,
            fila,
            columna
        )
        if (errorEstilo != null) return errorEstilo

        arbol.agregarElemento(
            ElementoFormulario(
                tipo = TipoElementoFormulario.SELECT_QUESTION,
                texto = textoResuelto.orEmpty(),
                ancho = anchoResuelto,
                alto = altoResuelto,
                opciones = opcionesFinales,
                indicesCorrectos = indicesCorrectos ?: emptyList(),
                estilo = estiloResuelto
            )
        )
        return null
    }
}
