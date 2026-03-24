package com.abn.pkm_forms.interprete.excepciones

data class ErrorInterpretacion(
    val tipo: String,
    val descripcion: String,
    val fila: Int,
    val columna: Int
)
