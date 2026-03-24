package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.Simbolo
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionForInicial(
    private val identificador: String,
    private val expresion: Expresion,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (resultado, error) = expresion.interpretar(arbol, tabla)
        if (error != null) return error
        if (resultado == null || resultado.tipo != TiposDato.NUMBER) {
            return ErrorInterpretacion("Semantico", "Inicializacion de FOR requiere number", fila, columna)
        }

        val simbolo = tabla.getVariable(identificador)
        if (simbolo == null) {
            val creado = tabla.setVariable(Simbolo(identificador, TiposDato.NUMBER, resultado.valor))
            if (!creado) {
                return ErrorInterpretacion("Semantico", "No se pudo crear variable '$identificador' para FOR", fila, columna)
            }
            return null
        }

        if (simbolo.tipo != TiposDato.NUMBER) {
            return ErrorInterpretacion("Semantico", "Variable '$identificador' en FOR debe ser number", fila, columna)
        }

        simbolo.valor = resultado.valor
        return null
    }
}
