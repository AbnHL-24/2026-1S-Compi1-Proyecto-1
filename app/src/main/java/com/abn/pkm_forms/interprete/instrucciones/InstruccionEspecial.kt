package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.Simbolo
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionEspecial(
    private val identificador: String,
    private val valor: Instruccion,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        if (tabla.existeEnEntornoActual(identificador)) {
            return ErrorInterpretacion("Semantico", "La variable especial '$identificador' ya existe", fila, columna)
        }

        val creado = tabla.setVariable(Simbolo(identificador, TiposDato.NULO, valor))
        if (!creado) {
            return ErrorInterpretacion("Semantico", "No se pudo guardar variable especial '$identificador'", fila, columna)
        }
        return null
    }
}

class InstruccionDrawEspecial(
    private val identificador: String,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val simbolo = tabla.getVariable(identificador)
            ?: return ErrorInterpretacion("Semantico", "Variable especial '$identificador' no existe", fila, columna)
        val instruccion = simbolo.valor as? Instruccion
            ?: return ErrorInterpretacion("Semantico", "Variable especial '$identificador' invalida", fila, columna)

        return instruccion.interpretar(arbol, tabla)
    }
}
