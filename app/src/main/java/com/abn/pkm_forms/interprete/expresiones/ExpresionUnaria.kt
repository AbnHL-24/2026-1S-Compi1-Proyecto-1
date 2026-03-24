package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class ExpresionUnaria(
    private val operador: String,
    private val expresion: Expresion,
    fila: Int,
    columna: Int
) : Expresion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        val (resultado, error) = expresion.interpretar(arbol, tabla)
        if (error != null) return Pair(null, error)
        if (resultado == null) {
            return Pair(
                null,
                ErrorInterpretacion("Semantico", "Expresion invalida", fila, columna)
            )
        }

        return when (operador) {
            "-" -> {
                if (resultado.tipo != TiposDato.NUMBER) {
                    Pair(null, ErrorInterpretacion("Semantico", "El operador '-' requiere number", fila, columna))
                } else {
                    Pair(ResultadoExpresion(TiposDato.NUMBER, -(resultado.valor as Double)), null)
                }
            }

            "~" -> {
                if (resultado.tipo != TiposDato.BOOLEAN) {
                    Pair(null, ErrorInterpretacion("Semantico", "El operador '~' requiere boolean", fila, columna))
                } else {
                    Pair(ResultadoExpresion(TiposDato.BOOLEAN, !(resultado.valor as Boolean)), null)
                }
            }

            else -> Pair(null, ErrorInterpretacion("Semantico", "Operador unario desconocido '$operador'", fila, columna))
        }
    }
}
