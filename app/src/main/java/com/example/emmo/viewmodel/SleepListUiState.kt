package com.example.emmo.viewmodel

import com.example.emmo.data.entity.SleepRecordEntity

data class SleepListUiState(
    val records: List<SleepRecordEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedQualityFilter: String = "ALL",
    val selectedTimeFilter: String = "ALL", // ALL, WEEK, MONTH, GOOD, BAD
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    // 统计
    val totalCount: Int = 0,
    val avgDuration: Float = 0f,
    val avgQuality: Float = 0f,
    // 本周统计
    val weekRecords: List<SleepRecordEntity> = emptyList(),
    val weekAvgDuration: Float = 0f,
    val weekAvgQuality: Float = 0f
)
