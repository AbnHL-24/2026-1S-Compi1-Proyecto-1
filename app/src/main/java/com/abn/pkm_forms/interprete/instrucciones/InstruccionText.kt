package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

class InstruccionText(
    private val texto: Expresion,
    private val ancho: Expresion?,
    private val alto: Expresion?,
    private val colorTexto: Expresion?,
    private val colorFondo: Expresion?,
    private val fuente: Expresion?,
    private val tamanioTexto: Expresion?,
    private val borde: Expresion?,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (textoResuelto, errorTexto) = resolverTexto(texto, arbol, tabla, "content", fila, columna)
        if (errorTexto != null) return errorTexto

        val (anchoResuelto, errorAncho) = resolverNumeroNoNegativo(ancho, arbol, tabla, "width", fila, columna)
        if (errorAncho != null) return errorAncho
        val (altoResuelto, errorAlto) = resolverNumeroNoNegativo(alto, arbol, tabla, "height", fila, columna)
        if (errorAlto != null) return errorAlto

        val (estiloResuelto, errorEstilo) = resolverEstiloDesdeExpresiones(
            colorTexto,
            colorFondo,
            fuente,
            tamanioTexto,
            borde,
            arbol,
            tabla,
            fila,
            columna
        )
        if (errorEstilo != null) return errorEstilo

        arbol.agregarElemento(
            ElementoFormulario(
                tipo = TipoElementoFormulario.TEXT,
                texto = textoResuelto.orEmpty(),
                ancho = anchoResuelto,
                alto = altoResuelto,
                estilo = estiloResuelto
            )
        )
        return null
    }
}
