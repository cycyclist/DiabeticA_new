package com.example.diabetica.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val LANGUAGE_KEY = booleanPreferencesKey("is_russian") // true - русский, false - английский
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val isRussianLanguage: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: true // по умолчанию русский
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setLanguage(isRussian: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = isRussian
        }
    }
}