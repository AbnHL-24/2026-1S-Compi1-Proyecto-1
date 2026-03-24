package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class ExpresionWhoIsThatPokemon(
    private val inicio: Expresion,
    private val fin: Expresion,
    fila: Int,
    columna: Int
) : Expresion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        val (resultadoInicio, errorInicio) = inicio.interpretar(arbol, tabla)
        if (errorInicio != null) return Pair(null, errorInicio)
        val (resultadoFin, errorFin) = fin.interpretar(arbol, tabla)
        if (errorFin != null) return Pair(null, errorFin)

        if (resultadoInicio == null || resultadoInicio.tipo != TiposDato.NUMBER) {
            return Pair(null, ErrorInterpretacion("Semantico", "who_is_that_pokemon requiere inicio number", fila, columna))
        }
        if (resultadoFin == null || resultadoFin.tipo != TiposDato.NUMBER) {
            return Pair(null, ErrorInterpretacion("Semantico", "who_is_that_pokemon requiere fin number", fila, columna))
        }

        val valorInicio = (resultadoInicio.valor as Double).toInt()
        val valorFin = (resultadoFin.valor as Double).toInt()
        if (valorInicio <= 0 || valorFin <= 0) {
            return Pair(null, ErrorInterpretacion("Semantico", "who_is_that_pokemon requiere rango positivo", fila, columna))
        }
        if (valorInicio > valorFin) {
            return Pair(null, ErrorInterpretacion("Semantico", "who_is_that_pokemon requiere inicio <= fin", fila, columna))
        }

        val opciones = (valorInicio..valorFin).map { numero ->
            "pokemon_$numero"
        }

        return Pair(ResultadoExpresion(TiposDato.LISTA_STRING, opciones), null)
    }
}
