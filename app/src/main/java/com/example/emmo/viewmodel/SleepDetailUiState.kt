package com.example.emmo.viewmodel

import com.example.emmo.data.entity.SleepRecordEntity

data class SleepDetailUiState(
    val record: SleepRecordEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val isDeleted: Boolean = false
)
