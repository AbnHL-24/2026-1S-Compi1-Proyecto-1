package com.abn.pkm_forms.interprete.abstracto

import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

abstract class Instruccion(
    val fila: Int,
    val columna: Int
) {
    abstract fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion?
}
