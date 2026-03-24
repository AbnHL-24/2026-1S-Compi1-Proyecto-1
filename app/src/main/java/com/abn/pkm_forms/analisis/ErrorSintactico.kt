package com.abn.pkm_forms.analisis

data class ErrorSintactico(
    val descripcion: String,
    val fila: Int,
    val columna: Int
)
