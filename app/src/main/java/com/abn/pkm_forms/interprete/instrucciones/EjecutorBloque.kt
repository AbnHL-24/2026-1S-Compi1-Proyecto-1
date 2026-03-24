package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

internal fun ejecutarBloqueControl(
    instrucciones: List<Instruccion>,
    arbol: Arbol,
    tabla: TablaSimbolos
): ErrorInterpretacion? {
    arbol.entrarBloqueControl()
    try {
        instrucciones.forEach { instruccion ->
            val error = instruccion.interpretar(arbol, tabla)
            if (error != null) {
                return error
            }
        }
        return null
    } finally {
        arbol.salirBloqueControl()
    }
}
