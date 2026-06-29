package com.example.emmo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emmo.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.targetSleepHours.collect { hours ->
                _uiState.update { it.copy(targetSleepHours = hours) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.darkMode.collect { enabled ->
                _uiState.update { it.copy(darkMode = enabled) }
            }
        }
    }

    fun setTargetSleepHours(hours: String) {
        _uiState.update { it.copy(targetSleepHours = hours) }
        viewModelScope.launch {
            preferencesRepository.setTargetSleepHours(hours)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }
}
