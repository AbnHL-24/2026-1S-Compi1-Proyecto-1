package com.abn.pkm_forms.interprete

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos

class EjecutorFormulario {
    fun ejecutar(instrucciones: List<Instruccion>): List<String> {
        val arbol = Arbol(instrucciones)
        val tablaGlobal = TablaSimbolos()

        instrucciones.forEach { instruccion ->
            val error = instruccion.interpretar(arbol, tablaGlobal)
            if (error != null) {
                arbol.agregarError(error)
            }
        }

        return arbol.errores.map {
            "${it.tipo}: ${it.descripcion} (fila ${it.fila}, col ${it.columna})"
        }
    }
}
