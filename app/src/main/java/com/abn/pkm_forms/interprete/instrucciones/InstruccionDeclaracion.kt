package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.Simbolo
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionDeclaracion(
    private val tipoDeclarado: TiposDato,
    private val identificador: String,
    private val expresionInicial: Expresion?,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        if (tabla.existeEnEntornoActual(identificador)) {
            return ErrorInterpretacion("Semantico", "La variable '$identificador' ya existe", fila, columna)
        }

        val valorInicial = when (tipoDeclarado) {
            TiposDato.NUMBER -> 0.0
            TiposDato.STRING -> ""
            TiposDato.BOOLEAN -> false
            TiposDato.NULO -> null
        }

        var valorFinal: Any? = valorInicial
        if (expresionInicial != null) {
            val (resultado, error) = expresionInicial.interpretar(arbol, tabla)
            if (error != null) return error
            if (resultado == null) return ErrorInterpretacion("Semantico", "No se pudo evaluar la expresion", fila, columna)
            if (resultado.tipo != tipoDeclarado) {
                return ErrorInterpretacion(
                    "Semantico",
                    "Tipo incompatible para '$identificador'. Se esperaba $tipoDeclarado y se obtuvo ${resultado.tipo}",
                    fila,
                    columna
                )
            }
            valorFinal = resultado.valor
        }

        val creado = tabla.setVariable(Simbolo(identificador, tipoDeclarado, valorFinal))
        if (!creado) {
            return ErrorInterpretacion("Semantico", "No se pudo crear la variable '$identificador'", fila, columna)
        }
        return null
    }
}
