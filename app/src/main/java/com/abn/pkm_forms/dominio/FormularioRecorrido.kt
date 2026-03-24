package com.abn.pkm_forms.dominio

import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

data class NodoIndiceElemento(
    val indice: Int,
    val nivel: Int,
    val elemento: ElementoFormulario
)

fun esPregunta(tipo: TipoElementoFormulario): Boolean {
    return tipo == TipoElementoFormulario.OPEN_QUESTION ||
        tipo == TipoElementoFormulario.DROP_QUESTION ||
        tipo == TipoElementoFormulario.SELECT_QUESTION ||
        tipo == TipoElementoFormulario.MULTIPLE_QUESTION
}

fun aplanarElementosConIndiceGlobal(elementos: List<ElementoFormulario>): List<NodoIndiceElemento> {
    val salida = mutableListOf<NodoIndiceElemento>()
    var indice = 0

    fun recorrer(lista: List<ElementoFormulario>, nivel: Int) {
        lista.forEach { elemento ->
            salida.add(NodoIndiceElemento(indice = indice, nivel = nivel, elemento = elemento))
            indice++
            if (elemento.elementosAnidados.isNotEmpty()) {
                recorrer(elemento.elementosAnidados, nivel + 1)
            }
        }
    }

    recorrer(elementos, 0)
    return salida
}

fun preguntasConIndiceGlobal(elementos: List<ElementoFormulario>): List<NodoIndiceElemento> {
    return aplanarElementosConIndiceGlobal(elementos).filter { esPregunta(it.elemento.tipo) }
}
