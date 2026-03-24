package com.abn.pkm_forms.interprete

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

data class ResultadoEjecucionFormulario(
    val erroresSemanticos: List<String>,
    val advertenciasSemanticas: List<String>,
    val elementosFormulario: List<ElementoFormulario>
)

class EjecutorFormulario {
    fun ejecutar(instrucciones: List<Instruccion>): ResultadoEjecucionFormulario {
        val arbol = Arbol(instrucciones)
        val tablaGlobal = TablaSimbolos()

        instrucciones.forEach { instruccion ->
            val error = instruccion.interpretar(arbol, tablaGlobal)
            if (error != null) {
                arbol.agregarError(error)
            }
        }

        return ResultadoEjecucionFormulario(
            erroresSemanticos = arbol.errores.map {
                "${it.tipo}: ${it.descripcion} (fila ${it.fila}, col ${it.columna})"
            },
            advertenciasSemanticas = arbol.advertencias,
            elementosFormulario = arbol.elementosFormulario
        )
    }
}
