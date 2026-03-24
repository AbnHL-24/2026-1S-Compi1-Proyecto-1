package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class ExpresionBinaria(
    private val izquierda: Expresion,
    private val operador: String,
    private val derecha: Expresion,
    fila: Int,
    columna: Int
) : Expresion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        val (resultadoIzquierda, errorIzquierda) = izquierda.interpretar(arbol, tabla)
        if (errorIzquierda != null) return Pair(null, errorIzquierda)
        if (resultadoIzquierda == null) return Pair(null, ErrorInterpretacion("Semantico", "Expresion izquierda invalida", fila, columna))

        if (operador == "&&") {
            if (resultadoIzquierda.tipo != TiposDato.BOOLEAN) {
                return Pair(null, ErrorInterpretacion("Semantico", "El operador '&&' requiere boolean", fila, columna))
            }
            if (!(resultadoIzquierda.valor as Boolean)) {
                return Pair(ResultadoExpresion(TiposDato.BOOLEAN, false), null)
            }
        }

        if (operador == "||") {
            if (resultadoIzquierda.tipo != TiposDato.BOOLEAN) {
                return Pair(null, ErrorInterpretacion("Semantico", "El operador '||' requiere boolean", fila, columna))
            }
            if (resultadoIzquierda.valor as Boolean) {
                return Pair(ResultadoExpresion(TiposDato.BOOLEAN, true), null)
            }
        }

        val (resultadoDerecha, errorDerecha) = derecha.interpretar(arbol, tabla)
        if (errorDerecha != null) return Pair(null, errorDerecha)
        if (resultadoDerecha == null) return Pair(null, ErrorInterpretacion("Semantico", "Expresion derecha invalida", fila, columna))

        return when (operador) {
            "+" -> resolverSuma(resultadoIzquierda, resultadoDerecha)
            "-" -> resolverNumerica(resultadoIzquierda, resultadoDerecha) { a, b -> a - b }
            "*" -> resolverNumerica(resultadoIzquierda, resultadoDerecha) { a, b -> a * b }
            "/" -> {
                if (resultadoDerecha.tipo == TiposDato.NUMBER && (resultadoDerecha.valor as Double) == 0.0) {
                    Pair(null, ErrorInterpretacion("Semantico", "Division entre cero", fila, columna))
                } else {
                    resolverNumerica(resultadoIzquierda, resultadoDerecha) { a, b -> a / b }
                }
            }

            "%" -> {
                if (resultadoDerecha.tipo == TiposDato.NUMBER && (resultadoDerecha.valor as Double) == 0.0) {
                    Pair(null, ErrorInterpretacion("Semantico", "Modulo entre cero", fila, columna))
                } else {
                    resolverNumerica(resultadoIzquierda, resultadoDerecha) { a, b -> a % b }
                }
            }

            "==" -> Pair(ResultadoExpresion(TiposDato.BOOLEAN, resultadoIzquierda.valor == resultadoDerecha.valor), null)
            "!=" -> Pair(ResultadoExpresion(TiposDato.BOOLEAN, resultadoIzquierda.valor != resultadoDerecha.valor), null)
            ">" -> resolverRelacional(resultadoIzquierda, resultadoDerecha) { a, b -> a > b }
            "<" -> resolverRelacional(resultadoIzquierda, resultadoDerecha) { a, b -> a < b }
            ">=" -> resolverRelacional(resultadoIzquierda, resultadoDerecha) { a, b -> a >= b }
            "<=" -> resolverRelacional(resultadoIzquierda, resultadoDerecha) { a, b -> a <= b }
            "&&" -> resolverLogica(resultadoIzquierda, resultadoDerecha) { a, b -> a && b }
            "||" -> resolverLogica(resultadoIzquierda, resultadoDerecha) { a, b -> a || b }
            else -> Pair(null, ErrorInterpretacion("Semantico", "Operador desconocido '$operador'", fila, columna))
        }
    }

    private fun resolverSuma(
        izquierda: ResultadoExpresion,
        derecha: ResultadoExpresion
    ): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        return if (izquierda.tipo == TiposDato.NUMBER && derecha.tipo == TiposDato.NUMBER) {
            Pair(ResultadoExpresion(TiposDato.NUMBER, (izquierda.valor as Double) + (derecha.valor as Double)), null)
        } else if (izquierda.tipo == TiposDato.STRING || derecha.tipo == TiposDato.STRING) {
            Pair(ResultadoExpresion(TiposDato.STRING, "${izquierda.valor ?: ""}${derecha.valor ?: ""}"), null)
        } else {
            Pair(null, ErrorInterpretacion("Semantico", "Tipos no compatibles para '+'", fila, columna))
        }
    }

    private fun resolverNumerica(
        izquierda: ResultadoExpresion,
        derecha: ResultadoExpresion,
        operacion: (Double, Double) -> Double
    ): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        if (izquierda.tipo != TiposDato.NUMBER || derecha.tipo != TiposDato.NUMBER) {
            return Pair(null, ErrorInterpretacion("Semantico", "La operacion '$operador' requiere number", fila, columna))
        }
        return Pair(
            ResultadoExpresion(TiposDato.NUMBER, operacion(izquierda.valor as Double, derecha.valor as Double)),
            null
        )
    }

    private fun resolverRelacional(
        izquierda: ResultadoExpresion,
        derecha: ResultadoExpresion,
        comparacion: (Double, Double) -> Boolean
    ): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        if (izquierda.tipo != TiposDato.NUMBER || derecha.tipo != TiposDato.NUMBER) {
            return Pair(null, ErrorInterpretacion("Semantico", "La operacion '$operador' requiere number", fila, columna))
        }
        return Pair(
            ResultadoExpresion(TiposDato.BOOLEAN, comparacion(izquierda.valor as Double, derecha.valor as Double)),
            null
        )
    }

    private fun resolverLogica(
        izquierda: ResultadoExpresion,
        derecha: ResultadoExpresion,
        operacion: (Boolean, Boolean) -> Boolean
    ): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        if (izquierda.tipo != TiposDato.BOOLEAN || derecha.tipo != TiposDato.BOOLEAN) {
            return Pair(null, ErrorInterpretacion("Semantico", "La operacion '$operador' requiere boolean", fila, columna))
        }
        return Pair(
            ResultadoExpresion(TiposDato.BOOLEAN, operacion(izquierda.valor as Boolean, derecha.valor as Boolean)),
            null
        )
    }
}
