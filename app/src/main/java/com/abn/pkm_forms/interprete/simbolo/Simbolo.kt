package com.abn.pkm_forms.interprete.simbolo

data class Simbolo(
    val identificador: String,
    val tipo: TiposDato,
    var valor: Any?
)
