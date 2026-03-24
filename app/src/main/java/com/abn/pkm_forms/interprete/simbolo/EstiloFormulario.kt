package com.abn.pkm_forms.interprete.simbolo

data class EstiloFormulario(
    val colorTexto: String? = null,
    val colorFondo: String? = null,
    val familiaFuente: String? = null,
    val tamanioTexto: Double? = null,
    val borde: String? = null
) {
    fun mezclar(sobrescrito: EstiloFormulario): EstiloFormulario {
        return EstiloFormulario(
            colorTexto = sobrescrito.colorTexto ?: colorTexto,
            colorFondo = sobrescrito.colorFondo ?: colorFondo,
            familiaFuente = sobrescrito.familiaFuente ?: familiaFuente,
            tamanioTexto = sobrescrito.tamanioTexto ?: tamanioTexto,
            borde = sobrescrito.borde ?: borde
        )
    }

    fun estaVacio(): Boolean {
        return colorTexto == null &&
            colorFondo == null &&
            familiaFuente == null &&
            tamanioTexto == null &&
            borde == null
    }
}
