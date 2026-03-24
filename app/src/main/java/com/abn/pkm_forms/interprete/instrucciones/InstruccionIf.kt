package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionIf(
    private val condicion: Expresion,
    private val instruccionesIf: List<Instruccion>,
    private val instruccionesElse: List<Instruccion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (resultado, error) = condicion.interpretar(arbol, tabla)
        if (error != null) return error
        if (resultado == null || resultado.tipo != TiposDato.BOOLEAN) {
            return ErrorInterpretacion("Semantico", "La condicion de IF debe ser boolean", fila, columna)
        }

        val instrucciones = if (resultado.valor as Boolean) instruccionesIf else instruccionesElse
        return ejecutarBloqueControl(instrucciones, arbol, tabla)
    }
}
