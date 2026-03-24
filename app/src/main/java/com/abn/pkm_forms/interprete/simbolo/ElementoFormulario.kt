package com.abn.pkm_forms.interprete.simbolo

data class ElementoFormulario(
    val tipo: TipoElementoFormulario,
    val texto: String,
    val ancho: Double? = null,
    val alto: Double? = null,
    val pointX: Double? = null,
    val pointY: Double? = null,
    val opciones: List<String> = emptyList(),
    val indicesCorrectos: List<Int> = emptyList()
)

enum class TipoElementoFormulario {
    SECTION,
    TEXT,
    OPEN_QUESTION,
    DROP_QUESTION,
    SELECT_QUESTION,
    MULTIPLE_QUESTION
}
