package com.abn.pkm_forms.interprete.simbolo

data class ElementoFormulario(
    val tipo: TipoElementoFormulario,
    val texto: String
)

enum class TipoElementoFormulario {
    SECTION,
    TEXT,
    OPEN_QUESTION,
    DROP_QUESTION,
    SELECT_QUESTION,
    MULTIPLE_QUESTION
}
