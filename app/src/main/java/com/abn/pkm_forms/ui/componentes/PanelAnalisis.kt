package com.abn.pkm_forms.ui.componentes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.abn.pkm_forms.analisis.ResultadoAnalisisFormulario

@Composable
fun PanelAnalisis(resultado: ResultadoAnalisisFormulario) {
    Text("Tokens: ${resultado.cantidadTokens}")
    Text("Instrucciones: ${resultado.cantidadInstrucciones}")
    Text("Errores lexicos: ${resultado.erroresLexicos.size}")
    Text("Errores sintacticos: ${resultado.erroresSintacticos.size}")
    Text("Errores semanticos: ${resultado.erroresSemanticos.size}")
    Text("Advertencias: ${resultado.advertenciasSemanticas.size}")

    if (resultado.erroresLexicos.isNotEmpty()) {
        Text("Detalle lexico:")
        resultado.erroresLexicos.forEach {
            Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
        }
    }

    if (resultado.erroresSintacticos.isNotEmpty()) {
        Text("Detalle sintactico:")
        resultado.erroresSintacticos.forEach {
            Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
        }
    }

    if (resultado.erroresSemanticos.isNotEmpty()) {
        Text("Detalle semantico:")
        resultado.erroresSemanticos.forEach {
            Text("- $it")
        }
    }

    if (resultado.advertenciasSemanticas.isNotEmpty()) {
        Text("Detalle advertencias:")
        resultado.advertenciasSemanticas.forEach {
            Text("- $it")
        }
    }
}
