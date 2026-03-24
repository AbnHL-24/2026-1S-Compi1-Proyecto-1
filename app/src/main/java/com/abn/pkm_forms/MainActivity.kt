package com.abn.pkm_forms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.abn.pkm_forms.analisis.ResultadoAnalisisFormulario
import com.abn.pkm_forms.analisis.ServicioAnalisisFormulario
import com.abn.pkm_forms.ui.theme.PKM_FORMSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CODIGO_DEMO = """
$ Demo inicial
SECTION inicio;
TEXT bienvenida;
/* comentario de bloque */
nombre = "Ash";
edad = 10;
"""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PKM_FORMSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaAnalizadorFormulario(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PantallaAnalizadorFormulario(modifier: Modifier = Modifier) {
    val servicioAnalisis = remember { ServicioAnalisisFormulario() }
    val alcanceCorrutina = rememberCoroutineScope()
    var codigoFuente by remember { mutableStateOf(CODIGO_DEMO) } // DEMO: comentar esta linea para APK limpia
    var ejecutando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<ResultadoAnalisisFormulario?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "PKM_FORMS", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Editor .form", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = codigoFuente,
            onValueChange = { codigoFuente = it },
            label = { Text("Codigo fuente") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    alcanceCorrutina.launch {
                        ejecutando = true
                        resultado = withContext(Dispatchers.Default) {
                            servicioAnalisis.ejecutar(codigoFuente)
                        }
                        ejecutando = false
                    }
                },
                enabled = !ejecutando
            ) {
                Text("Ejecutar")
            }

            if (ejecutando) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }
        }

        resultado?.let { analisis ->
            Text("Tokens: ${analisis.cantidadTokens}")
            Text("Instrucciones: ${analisis.cantidadInstrucciones}")
            Text("Errores lexicos: ${analisis.erroresLexicos.size}")
            Text("Errores sintacticos: ${analisis.erroresSintacticos.size}")
            Text("Errores semanticos: ${analisis.erroresSemanticos.size}")

            if (analisis.erroresLexicos.isNotEmpty()) {
                Text("Detalle lexico:")
                analisis.erroresLexicos.forEach {
                    Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
                }
            }

            if (analisis.erroresSintacticos.isNotEmpty()) {
                Text("Detalle sintactico:")
                analisis.erroresSintacticos.forEach {
                    Text("- ${it.descripcion} (fila ${it.fila}, col ${it.columna})")
                }
            }

            if (analisis.erroresSemanticos.isNotEmpty()) {
                Text("Detalle semantico:")
                analisis.erroresSemanticos.forEach {
                    Text("- $it")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PKM_FORMSTheme {
        PantallaAnalizadorFormulario()
    }
}
