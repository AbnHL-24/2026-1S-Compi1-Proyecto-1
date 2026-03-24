package com.abn.pkm_forms.interprete.simbolo

class TablaSimbolos(private val anterior: TablaSimbolos? = null) {
    private val simbolos: MutableMap<String, Simbolo> = mutableMapOf()

    fun setVariable(simbolo: Simbolo): Boolean {
        if (simbolos.containsKey(simbolo.identificador)) {
            return false
        }
        simbolos[simbolo.identificador] = simbolo
        return true
    }

    fun getVariable(identificador: String): Simbolo? {
        var entorno: TablaSimbolos? = this
        while (entorno != null) {
            val simbolo = entorno.simbolos[identificador]
            if (simbolo != null) {
                return simbolo
            }
            entorno = entorno.anterior
        }
        return null
    }

    fun existeEnEntornoActual(identificador: String): Boolean {
        return simbolos.containsKey(identificador)
    }
}
