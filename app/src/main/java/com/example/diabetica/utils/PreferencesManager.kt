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
        val LANGUAGE_KEY = booleanPreferencesKey("is_russian")
        val GLUCOSE_UNIT_KEY = booleanPreferencesKey("use_mmol")}

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val isRussianLanguage: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: true // по умолчанию русский
        }

    val useMmol: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[GLUCOSE_UNIT_KEY] ?: false  // по умолчанию мг/дл
        }

    suspend fun setGlucoseUnit(useMmol: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GLUCOSE_UNIT_KEY] = useMmol
        }}

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