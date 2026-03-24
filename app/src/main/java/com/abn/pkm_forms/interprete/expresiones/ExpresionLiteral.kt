package com.abn.pkm_forms.interprete.expresiones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.ResultadoExpresion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class ExpresionLiteral(
    private val tipoLiteral: TiposDato,
    private val valorLiteral: Any?,
    fila: Int,
    columna: Int
) : Expresion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?> {
        return Pair(ResultadoExpresion(tipoLiteral, valorLiteral), null)
    }
}
