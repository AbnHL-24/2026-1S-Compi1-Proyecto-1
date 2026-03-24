package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionWhile(
    private val condicion: Expresion,
    private val instrucciones: List<Instruccion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        var iteraciones = 0
        while (true) {
            val (resultado, error) = condicion.interpretar(arbol, tabla)
            if (error != null) return error
            if (resultado == null || resultado.tipo != TiposDato.BOOLEAN) {
                return ErrorInterpretacion("Semantico", "La condicion de WHILE debe ser boolean", fila, columna)
            }
            if (!(resultado.valor as Boolean)) break

            if (iteraciones >= arbol.limiteIteraciones) {
                return ErrorInterpretacion("Semantico", "WHILE excedio el limite de iteraciones", fila, columna)
            }

            val errorBloque = ejecutarBloqueControl(instrucciones, arbol, tabla)
            if (errorBloque != null) return errorBloque
            iteraciones++
        }
        return null
    }
}
