package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.Simbolo
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TiposDato

class InstruccionFor(
    private val inicial: Instruccion,
    private val condicion: Expresion,
    private val actualizacion: Instruccion,
    private val instrucciones: List<Instruccion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val errorInicial = inicial.interpretar(arbol, tabla)
        if (errorInicial != null) return errorInicial

        var iteraciones = 0
        while (true) {
            val (resultadoCondicion, errorCondicion) = condicion.interpretar(arbol, tabla)
            if (errorCondicion != null) return errorCondicion
            if (resultadoCondicion == null || resultadoCondicion.tipo != TiposDato.BOOLEAN) {
                return ErrorInterpretacion("Semantico", "La condicion de FOR debe ser boolean", fila, columna)
            }
            if (!(resultadoCondicion.valor as Boolean)) break

            if (iteraciones >= arbol.limiteIteraciones) {
                return ErrorInterpretacion("Semantico", "FOR excedio el limite de iteraciones", fila, columna)
            }

            val errorBloque = ejecutarBloqueControl(instrucciones, arbol, tabla)
            if (errorBloque != null) return errorBloque

            val errorActualizacion = actualizacion.interpretar(arbol, tabla)
            if (errorActualizacion != null) return errorActualizacion
            iteraciones++
        }
        return null
    }
}

class InstruccionForRango(
    private val identificador: String,
    private val inicio: Expresion,
    private val fin: Expresion,
    private val instrucciones: List<Instruccion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (resultadoInicio, errorInicio) = inicio.interpretar(arbol, tabla)
        if (errorInicio != null) return errorInicio
        val (resultadoFin, errorFin) = fin.interpretar(arbol, tabla)
        if (errorFin != null) return errorFin

        if (resultadoInicio == null || resultadoInicio.tipo != TiposDato.NUMBER) {
            return ErrorInterpretacion("Semantico", "FOR rango requiere inicio number", fila, columna)
        }
        if (resultadoFin == null || resultadoFin.tipo != TiposDato.NUMBER) {
            return ErrorInterpretacion("Semantico", "FOR rango requiere fin number", fila, columna)
        }

        val valorInicio = (resultadoInicio.valor as Double).toInt()
        val valorFin = (resultadoFin.valor as Double).toInt()

        val simboloExistente = tabla.getVariable(identificador)
        if (simboloExistente == null) {
            val creado = tabla.setVariable(Simbolo(identificador, TiposDato.NUMBER, valorInicio.toDouble()))
            if (!creado) {
                return ErrorInterpretacion("Semantico", "No se pudo crear variable '$identificador' en FOR rango", fila, columna)
            }
        } else if (simboloExistente.tipo != TiposDato.NUMBER) {
            return ErrorInterpretacion("Semantico", "Variable '$identificador' en FOR rango debe ser number", fila, columna)
        }

        var iteraciones = 0
        var actual = valorInicio
        val paso = if (valorInicio <= valorFin) 1 else -1

        while (true) {
            if (iteraciones >= arbol.limiteIteraciones) {
                return ErrorInterpretacion("Semantico", "FOR rango excedio el limite de iteraciones", fila, columna)
            }

            val simbolo = tabla.getVariable(identificador)
                ?: return ErrorInterpretacion("Semantico", "Variable '$identificador' no encontrada", fila, columna)
            simbolo.valor = actual.toDouble()

            val errorBloque = ejecutarBloqueControl(instrucciones, arbol, tabla)
            if (errorBloque != null) return errorBloque

            if (actual == valorFin) break
            actual += paso
            iteraciones++
        }

        return null
    }
}
