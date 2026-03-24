package com.abn.pkm_forms.datos

import com.abn.pkm_forms.interprete.simbolo.ElementoFormulario
import com.abn.pkm_forms.interprete.simbolo.EstiloFormulario
import com.abn.pkm_forms.interprete.simbolo.TipoElementoFormulario

data class ResultadoCargaPkm(
    val elementos: List<ElementoFormulario>,
    val errores: List<String>
)

private data class NodoSerializacionPkm(
    val profundidad: Int,
    val elemento: ElementoFormulario
)

private data class NodoLecturaPkm(
    val profundidad: Int,
    val elemento: ElementoFormulario
)

private fun escaparCampoPkm(texto: String): String {
    return texto
        .replace("\\", "\\\\")
        .replace("|", "\\|")
        .replace("\n", "\\n")
}

private fun desescaparCampoPkm(texto: String): String {
    val salida = StringBuilder()
    var indice = 0
    while (indice < texto.length) {
        val actual = texto[indice]
        if (actual == '\\' && indice + 1 < texto.length) {
            val siguiente = texto[indice + 1]
            when (siguiente) {
                'n' -> salida.append('\n')
                '\\' -> salida.append('\\')
                '|' -> salida.append('|')
                else -> salida.append(siguiente)
            }
            indice += 2
        } else {
            salida.append(actual)
            indice++
        }
    }
    return salida.toString()
}

private fun encontrarSeparadorPkm(linea: String): Int {
    var escapado = false
    linea.forEachIndexed { indice, caracter ->
        if (escapado) {
            escapado = false
            return@forEachIndexed
        }
        if (caracter == '\\') {
            escapado = true
            return@forEachIndexed
        }
        if (caracter == '|') {
            return indice
        }
    }
    return -1
}

private fun separarCamposPkm(linea: String): List<String> {
    val campos = mutableListOf<String>()
    val actual = StringBuilder()
    var escapado = false

    linea.forEach { caracter ->
        if (escapado) {
            actual.append(caracter)
            escapado = false
            return@forEach
        }

        if (caracter == '\\') {
            escapado = true
            actual.append(caracter)
            return@forEach
        }

        if (caracter == '|') {
            campos.add(actual.toString())
            actual.clear()
            return@forEach
        }

        actual.append(caracter)
    }

    campos.add(actual.toString())
    return campos
}

fun serializarElementosPkm(elementos: List<ElementoFormulario>): String {
    fun aPlano(
        lista: List<ElementoFormulario>,
        profundidad: Int,
        salida: MutableList<NodoSerializacionPkm>
    ) {
        lista.forEach { elemento ->
            salida.add(NodoSerializacionPkm(profundidad, elemento))
            if (elemento.elementosAnidados.isNotEmpty()) {
                aPlano(elemento.elementosAnidados, profundidad + 1, salida)
            }
        }
    }

    val nodos = mutableListOf<NodoSerializacionPkm>()
    aPlano(elementos, 0, nodos)

    return buildString {
        appendLine("PKM_FORMS_V1")
        nodos.forEach { nodo ->
            val elemento = nodo.elemento
            val opciones = elemento.opciones.joinToString(";;") { escaparCampoPkm(it) }
            val indices = elemento.indicesCorrectos.joinToString(",")
            append(elemento.tipo.name)
            append('|')
            append(escaparCampoPkm(elemento.texto))
            append('|')
            append(elemento.ancho?.toString() ?: "")
            append('|')
            append(elemento.alto?.toString() ?: "")
            append('|')
            append(elemento.pointX?.toString() ?: "")
            append('|')
            append(elemento.pointY?.toString() ?: "")
            append('|')
            append(opciones)
            append('|')
            append(indices)
            append('|')
            append(nodo.profundidad)
            append('|')
            append(escaparCampoPkm(elemento.estilo.colorTexto ?: ""))
            append('|')
            append(escaparCampoPkm(elemento.estilo.colorFondo ?: ""))
            append('|')
            append(escaparCampoPkm(elemento.estilo.familiaFuente ?: ""))
            append('|')
            append(elemento.estilo.tamanioTexto?.toString() ?: "")
            append('|')
            append(escaparCampoPkm(elemento.estilo.borde ?: ""))
            appendLine()
        }
    }
}

fun deserializarElementosPkm(contenido: String): ResultadoCargaPkm {
    val lineas = contenido.lines()
    val elementosRaiz = mutableListOf<ElementoFormulario>()
    val errores = mutableListOf<String>()
    val nodosLectura = mutableListOf<NodoLecturaPkm>()

    lineas.forEachIndexed { indice, lineaOriginal ->
        val linea = lineaOriginal.trimEnd()
        if (linea.isBlank()) return@forEachIndexed
        if (indice == 0 && linea == "PKM_FORMS_V1") return@forEachIndexed

        val posicionSeparador = encontrarSeparadorPkm(linea)
        if (posicionSeparador <= 0) {
            errores.add("Linea ${indice + 1} invalida en .pkm")
            return@forEachIndexed
        }

        val campos = separarCamposPkm(linea)
        val tipoTexto = campos.getOrNull(0).orEmpty()
        val tipo = runCatching { TipoElementoFormulario.valueOf(tipoTexto) }.getOrNull()
        if (tipo == null) {
            errores.add("Linea ${indice + 1} con tipo desconocido: $tipoTexto")
            return@forEachIndexed
        }

        val texto = desescaparCampoPkm(campos.getOrNull(1).orEmpty())
        val ancho = campos.getOrNull(2)?.toDoubleOrNull()
        val alto = campos.getOrNull(3)?.toDoubleOrNull()
        val pointX = campos.getOrNull(4)?.toDoubleOrNull()
        val pointY = campos.getOrNull(5)?.toDoubleOrNull()
        val opciones = campos.getOrNull(6)
            ?.split(";;")
            ?.filter { it.isNotBlank() }
            ?.map { desescaparCampoPkm(it) }
            ?: emptyList()
        val indicesCorrectos = campos.getOrNull(7)
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()

        val profundidad = campos.getOrNull(8)?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val estilo = EstiloFormulario(
            colorTexto = campos.getOrNull(9)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            colorFondo = campos.getOrNull(10)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            familiaFuente = campos.getOrNull(11)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) },
            tamanioTexto = campos.getOrNull(12)?.toDoubleOrNull(),
            borde = campos.getOrNull(13)?.takeIf { it.isNotBlank() }?.let { desescaparCampoPkm(it) }
        )

        nodosLectura.add(
            NodoLecturaPkm(
                profundidad = profundidad,
                elemento = ElementoFormulario(
                    tipo = tipo,
                    texto = texto,
                    ancho = ancho,
                    alto = alto,
                    pointX = pointX,
                    pointY = pointY,
                    opciones = opciones,
                    indicesCorrectos = indicesCorrectos,
                    estilo = estilo
                )
            )
        )
    }

    fun insertarEnRuta(
        lista: MutableList<ElementoFormulario>,
        ruta: List<Int>,
        nuevo: ElementoFormulario
    ): MutableList<ElementoFormulario> {
        if (ruta.isEmpty()) {
            lista.add(nuevo)
            return lista
        }

        val copia = lista.toMutableList()
        val indiceActual = ruta.first()
        if (indiceActual !in copia.indices) {
            copia.add(nuevo)
            return copia
        }

        val elementoActual = copia[indiceActual]
        val hijosActualizados = insertarEnRuta(
            elementoActual.elementosAnidados.toMutableList(),
            ruta.drop(1),
            nuevo
        )
        copia[indiceActual] = elementoActual.copy(elementosAnidados = hijosActualizados)
        return copia
    }

    val pilaRutas = mutableListOf<List<Int>>()
    nodosLectura.forEach { nodo ->
        while (pilaRutas.size > nodo.profundidad) {
            pilaRutas.removeAt(pilaRutas.lastIndex)
        }

        if (nodo.profundidad == 0 || pilaRutas.isEmpty()) {
            elementosRaiz.add(nodo.elemento)
            val rutaNueva = listOf(elementosRaiz.lastIndex)
            pilaRutas.add(rutaNueva)
            return@forEach
        }

        val rutaPadre = pilaRutas.last()
        val copiaRaiz = insertarEnRuta(elementosRaiz.toMutableList(), rutaPadre, nodo.elemento)
        elementosRaiz.clear()
        elementosRaiz.addAll(copiaRaiz)

        fun obtenerHijosEnRuta(lista: List<ElementoFormulario>, ruta: List<Int>): List<ElementoFormulario> {
            var elemento = lista[ruta.first()]
            ruta.drop(1).forEach { indice ->
                elemento = elemento.elementosAnidados[indice]
            }
            return elemento.elementosAnidados
        }

        val hijosPadre = obtenerHijosEnRuta(elementosRaiz, rutaPadre)
        val rutaNueva = rutaPadre + (hijosPadre.lastIndex)
        pilaRutas.add(rutaNueva)
    }

    return ResultadoCargaPkm(elementos = elementosRaiz, errores = errores)
}
