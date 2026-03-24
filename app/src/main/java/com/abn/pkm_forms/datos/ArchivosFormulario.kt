package com.abn.pkm_forms.datos

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun leerTextoDeUri(contexto: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val flujo = contexto.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir archivo")
        flujo.bufferedReader().use { it.readText() }
    }
}

suspend fun escribirTextoEnUri(contexto: Context, uri: Uri, contenido: String) {
    withContext(Dispatchers.IO) {
        val flujo = contexto.contentResolver.openOutputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir destino")
        flujo.bufferedWriter().use { it.write(contenido) }
    }
}
