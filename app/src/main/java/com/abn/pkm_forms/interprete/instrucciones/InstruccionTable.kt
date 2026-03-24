package com.abn.pkm_forms.interprete.instrucciones

import com.abn.pkm_forms.interprete.abstracto.Expresion
import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.excepciones.ErrorInterpretacion
import com.abn.pkm_forms.interprete.simbolo.Arbol
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.TablaSimbolos
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

class InstruccionTable(
    private val texto: Expresion,
    private val ancho: Expresion?,
    private val alto: Expresion?,
    private val pointX: Expresion?,
    private val pointY: Expresion?,
    private val colorTexto: Expresion?,
    private val colorFondo: Expresion?,
    private val fuente: Expresion?,
    private val tamanioTexto: Expresion?,
    private val borde: Expresion?,
    private val elementosAnidados: List<Instruccion>,
    fila: Int,
    columna: Int
) : Instruccion(fila, columna) {
    override fun interpretar(arbol: Arbol, tabla: TablaSimbolos): ErrorInterpretacion? {
        val (textoResuelto, errorTexto) = resolverTexto(texto, arbol, tabla, "label", fila, columna)
        if (errorTexto != null) return errorTexto

        val (anchoResuelto, errorAncho) = resolverNumeroNoNegativo(ancho, arbol, tabla, "width", fila, columna)
        if (errorAncho != null) return errorAncho
        val (altoResuelto, errorAlto) = resolverNumeroNoNegativo(alto, arbol, tabla, "height", fila, columna)
        if (errorAlto != null) return errorAlto
        val (pointXResuelto, errorPointX) = resolverNumeroNoNegativo(pointX, arbol, tabla, "pointX", fila, columna)
        if (errorPointX != null) return errorPointX
        val (pointYResuelto, errorPointY) = resolverNumeroNoNegativo(pointY, arbol, tabla, "pointY", fila, columna)
        if (errorPointY != null) return errorPointY

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

        val inicioAnidados = arbol.elementosFormulario.size
        arbol.entrarEstilo(estiloResuelto)
        val errorAnidados = ejecutarBloqueControl(elementosAnidados, arbol, tabla)
        arbol.salirEstilo()
        if (errorAnidados != null) return errorAnidados
        val capturados = arbol.elementosFormulario.drop(inicioAnidados)
        if (capturados.isNotEmpty()) {
            repeat(capturados.size) {
                arbol.elementosFormulario.removeAt(arbol.elementosFormulario.lastIndex)
            }
        }

        arbol.agregarElemento(
            ElementoFormulario(
                tipo = TipoElementoFormulario.TABLE,
                texto = textoResuelto.orEmpty(),
                ancho = anchoResuelto,
                alto = altoResuelto,
                pointX = pointXResuelto,
                pointY = pointYResuelto,
                estilo = estiloResuelto,
                elementosAnidados = capturados
            )
        )
        return null
    }
}
