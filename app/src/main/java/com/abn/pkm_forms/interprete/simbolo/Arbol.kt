package com.abn.pkm_forms.interprete.simbolo

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion

class Arbol(val instrucciones: List<Instruccion>) {
    val errores: MutableList<ErrorInterpretacion> = mutableListOf()
    val advertencias: MutableList<String> = mutableListOf()
    val elementosFormulario: MutableList<ElementoFormulario> = mutableListOf()
    var salidaConsola: String = ""
    var limiteIteraciones: Int = 1000
    private var profundidadBloqueControl: Int = 0
    private val pilaEstilos: MutableList<EstiloFormulario> = mutableListOf(EstiloFormulario())

    fun agregarError(error: ErrorInterpretacion) {
        errores.add(error)
    }

    fun agregarSalida(texto: String) {
        salidaConsola += texto + "\n"
    }

    fun agregarAdvertencia(advertencia: String) {
        advertencias.add(advertencia)
    }

    fun agregarElemento(elemento: ElementoFormulario) {
        val estiloActual = obtenerEstiloActual()
        val elementoConEstilo = if (elemento.estilo.estaVacio()) {
            elemento.copy(estilo = estiloActual)
        } else {
            elemento.copy(estilo = estiloActual.mezclar(elemento.estilo))
        }
        elementosFormulario.add(elementoConEstilo)
    }

    fun obtenerEstiloActual(): EstiloFormulario {
        return pilaEstilos.lastOrNull() ?: EstiloFormulario()
    }

    fun entrarEstilo(estiloLocal: EstiloFormulario) {
        val heredado = obtenerEstiloActual()
        pilaEstilos.add(heredado.mezclar(estiloLocal))
    }

    fun salirEstilo() {
        if (pilaEstilos.size > 1) {
            pilaEstilos.removeAt(pilaEstilos.lastIndex)
        }
    }

    fun entrarBloqueControl() {
        profundidadBloqueControl++
    }

    fun salirBloqueControl() {
        if (profundidadBloqueControl > 0) {
            profundidadBloqueControl--
        }
    }

    fun estaEnBloqueControl(): Boolean {
        return profundidadBloqueControl > 0
    }
}
