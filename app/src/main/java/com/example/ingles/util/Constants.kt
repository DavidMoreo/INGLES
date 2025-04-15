package com.example.ingles.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.ingles.InglesApp

object Constants {
    private val _tiposPalabras = mutableStateListOf("Todos")
    val TIPOS_PALABRAS: SnapshotStateList<String> = _tiposPalabras

    private const val PREFS_NAME = "tipos_prefs"
    private const val KEY_TIPOS = "tipos_list"

    init {
        cargarTiposGuardados()
    }

    private fun cargarTiposGuardados() {
        val prefs = InglesApp.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tiposGuardados = prefs.getStringSet(KEY_TIPOS, setOf("Todos")) ?: setOf("Todos")
        _tiposPalabras.clear()
        _tiposPalabras.addAll(tiposGuardados)
    }

    private fun guardarTipos() {
        val prefs = InglesApp.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_TIPOS, _tiposPalabras.toSet()).apply()
    }

    fun agregarTipo(tipo: String) {
        if (!_tiposPalabras.contains(tipo)) {
            _tiposPalabras.add(tipo)
            guardarTipos()
        }
    }

    fun eliminarTipo(tipo: String) {
        if (tipo != "Todos") {
            _tiposPalabras.remove(tipo)
            guardarTipos()
        }
    }

    fun obtenerTipos(): List<String> {
        return _tiposPalabras.toList()
    }

    fun inicializarTipos(tipos: List<String>) {
        _tiposPalabras.clear()
        _tiposPalabras.add("General")
        _tiposPalabras.addAll(tipos.filter { it != "General" })
        guardarTipos()
    }
} 