package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion

object ErroresExpresiones {
    fun tiposNoCompatibles(
        operacion: String,
        fila: Int,
        columna: Int
    ): ErrorInterpretacion {
        return ErrorInterpretacion(
            tipo = "Semantico",
            descripcion = "Tipos no compatibles para la operacion '$operacion'",
            fila = fila,
            columna = columna
        )
    }

    fun variableNoExiste(
        identificador: String,
        fila: Int,
        columna: Int
    ): ErrorInterpretacion {
        return ErrorInterpretacion(
            tipo = "Semantico",
            descripcion = "La variable '$identificador' no existe",
            fila = fila,
            columna = columna
        )
    }
}
