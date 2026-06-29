package com.example.emmo.viewmodel

data class SettingsUiState(
    val targetSleepHours: String = "8",
    val darkMode: Boolean = false,
    val isLoading: Boolean = false
)
