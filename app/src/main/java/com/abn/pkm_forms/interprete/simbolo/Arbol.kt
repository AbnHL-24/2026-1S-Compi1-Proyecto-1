package com.abn.pkm_forms.interprete.simbolo

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion

class Arbol(val instrucciones: List<Instruccion>) {
    val errores: MutableList<ErrorInterpretacion> = mutableListOf()
    val elementosFormulario: MutableList<ElementoFormulario> = mutableListOf()
    var salidaConsola: String = ""

    fun agregarError(error: ErrorInterpretacion) {
        errores.add(error)
    }

    fun agregarSalida(texto: String) {
        salidaConsola += texto + "\n"
    }

    fun agregarElemento(elemento: ElementoFormulario) {
        elementosFormulario.add(elemento)
    }
}
