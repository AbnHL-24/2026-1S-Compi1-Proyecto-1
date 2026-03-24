package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

class InstruccionAsignacion(
    private val identificador: String,
    private val expresion: Expresion,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val simbolo = tabla.getVariable(identificador)
            ?: return ErrorInterpretacion("Semantico", "La variable '$identificador' no existe", fila, columna)

        val (resultado, error) = expresion.interpretar(arbol, tabla)
        if (error != null) return error
        if (resultado == null) return ErrorInterpretacion("Semantico", "No se pudo evaluar la expresion", fila, columna)

        if (simbolo.tipo != resultado.tipo) {
            return ErrorInterpretacion(
                "Semantico",
                "Asignacion incompatible para '$identificador'. Se esperaba ${simbolo.tipo} y se obtuvo ${resultado.tipo}",
                fila,
                columna
            )
        }

        simbolo.valor = resultado.valor
        return null
    }
}
