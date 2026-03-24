package com.abn.pkm_forms.analisis

import com.abn.pkm_forms.interprete.abstracto.Instruccion
import com.abn.pkm_forms.interprete.EjecutorFormulario
import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import java.io.StringReader
import java_cup.runtime.Symbol

data class ResultadoAnalisisFormulario(
    val cantidadTokens: Int,
    val cantidadInstrucciones: Int,
    val erroresLexicos: List<ErrorLexico>,
    val erroresSintacticos: List<ErrorSintactico>,
    val erroresSemanticos: List<String>,
    val elementosFormulario: List<ElementoFormulario>
)

class ServicioAnalisisFormulario {
    fun ejecutar(codigo: String): ResultadoAnalisisFormulario {
        val cantidadTokens = contarTokens(codigo)

        val scanner = AnalizadorJflex(StringReader(codigo))
        val parser = parser(scanner)

        val instrucciones = mutableListOf<Instruccion>()
        try {
            val simboloResultado = parser.parse()
            val valor = simboloResultado.value
            if (valor is List<*>) {
                valor.filterIsInstance<Instruccion>().forEach { instrucciones.add(it) }
            }
        } catch (_: Exception) {
        }

        val ejecutor = EjecutorFormulario()
        val resultadoEjecucion = ejecutor.ejecutar(instrucciones)

        return ResultadoAnalisisFormulario(
            cantidadTokens = cantidadTokens,
            cantidadInstrucciones = instrucciones.size,
            erroresLexicos = scanner.erroresLexicos,
            erroresSintacticos = parser.erroresSintacticos,
            erroresSemanticos = resultadoEjecucion.erroresSemanticos,
            elementosFormulario = resultadoEjecucion.elementosFormulario
        )
    }

    private fun contarTokens(codigo: String): Int {
        val scanner = AnalizadorJflex(StringReader(codigo))
        var contador = 0
        while (true) {
            val token: Symbol = scanner.next_token()
            if (token.sym == sym.EOF) {
                break
            }
            contador++
        }
        return contador
    }
}
