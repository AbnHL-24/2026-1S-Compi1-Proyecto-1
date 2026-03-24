package com.abn.pkm_forms.interprete.abstracto

import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

data class ResultadoExpresion(
    val tipo: TiposDato,
    val valor: Any?
)

abstract class Expresion(
    val fila: Int,
    val columna: Int
) {
    abstract fun interpretar(arbol: Arbol, tabla: TablaSimbolos): Pair<ResultadoExpresion?, ErrorInterpretacion?>
}
