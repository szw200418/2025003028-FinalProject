package com.example.emmo.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sleep_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val DEFAULT_QUALITY_FILTER = stringPreferencesKey("default_quality_filter")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TARGET_SLEEP_HOURS = stringPreferencesKey("target_sleep_hours")
        val LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
    }

    val defaultQualityFilter: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_QUALITY_FILTER] ?: "ALL"
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    val targetSleepHours: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TARGET_SLEEP_HOURS] ?: "8"
    }

    val lastSearchQuery: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_SEARCH_QUERY] ?: ""
    }

    suspend fun setDefaultQualityFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_QUALITY_FILTER] = filter
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setTargetSleepHours(hours: String) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_SLEEP_HOURS] = hours
        }
    }

    suspend fun setLastSearchQuery(query: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SEARCH_QUERY] = query
        }
    }
}
