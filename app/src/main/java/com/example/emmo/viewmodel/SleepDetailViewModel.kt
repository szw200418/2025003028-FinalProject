package com.example.emmo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emmo.data.database.AppDatabase
import com.example.emmo.data.entity.SleepRecordEntity
import com.example.emmo.data.repository.SleepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SleepDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SleepRepository(database.sleepRecordDao())

    private val _uiState = MutableStateFlow(SleepDetailUiState())
    val uiState: StateFlow<SleepDetailUiState> = _uiState.asStateFlow()

    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val record = repository.getRecordById(recordId)
                if (record != null) {
                    _uiState.update { it.copy(record = record, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "未找到该睡眠记录") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateRecord(record: SleepRecordEntity) {
        viewModelScope.launch {
            try {
                repository.updateRecord(record.copy(updatedAt = System.currentTimeMillis()))
                _uiState.update { it.copy(record = record, isEditing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "更新失败: ${e.message}") }
            }
        }
    }

    fun deleteRecord(record: SleepRecordEntity) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(record)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun toggleFavorite(record: SleepRecordEntity) {
        viewModelScope.launch {
            try {
                val updated = record.copy(isFavorite = !record.isFavorite, updatedAt = System.currentTimeMillis())
                repository.updateRecord(updated)
                _uiState.update { it.copy(record = updated) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "操作失败: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun createNewRecord(): SleepRecordEntity {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return SleepRecordEntity(
            date = today,
            sleepTime = "23:00",
            wakeTime = "07:00",
            quality = 3
        )
    }

    fun saveNewRecord(record: SleepRecordEntity, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                if (record.date.isBlank()) {
                    _uiState.update { it.copy(error = "日期不能为空") }
                    return@launch
                }
                // 自动计算时长
                val duration = calculateDuration(record.sleepTime, record.wakeTime)
                val finalRecord = record.copy(durationMinutes = duration)
                val id = repository.insertRecord(finalRecord)
                onSuccess(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败: ${e.message}") }
            }
        }
    }

    private fun calculateDuration(sleepTime: String, wakeTime: String): Int {
        return try {
            val sleep = LocalTime.parse(sleepTime)
            val wake = LocalTime.parse(wakeTime)
            var minutes = java.time.Duration.between(sleep, wake).toMinutes()
            if (minutes <= 0) minutes += 24 * 60
            minutes.toInt()
        } catch (e: Exception) {
            0
        }
    }
}
