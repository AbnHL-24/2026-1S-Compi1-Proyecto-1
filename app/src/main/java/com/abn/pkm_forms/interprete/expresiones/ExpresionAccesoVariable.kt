package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

class ExpresionAccesoVariable(
    private val identificador: String,
    fila: Int,
    columna: Int
) : Expresion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        val simbolo = tabla.getVariable(identificador)
            ?: return Pair(
                null,
                ErrorInterpretacion(
                    tipo = "Semantico",
                    descripcion = "La variable '$identificador' no existe",
                    fila = fila,
                    columna = columna
                )
            )

        return Pair(ResultadoExpresion(simbolo.tipo, simbolo.valor), null)
    }
}
