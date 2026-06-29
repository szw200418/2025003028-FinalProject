package com.example.emmo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emmo.data.database.AppDatabase
import com.example.emmo.data.entity.SleepRecordEntity
import com.example.emmo.data.repository.SleepRepository
import com.example.emmo.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SleepListViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SleepRepository(database.sleepRecordDao())
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow(SleepListUiState())
    val uiState: StateFlow<SleepListUiState> = _uiState.asStateFlow()

    init {
        refreshSeedData()
        loadRecords()
        loadStats()
        loadWeekStats()
        loadPreferences()
    }

    private fun refreshSeedData() {
        viewModelScope.launch {
            try {
                // 删除旧数据
                repository.deleteAllRecords()

                // 动态生成最近7天的种子数据
                val today = LocalDate.now()
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val now = System.currentTimeMillis()

                // 7天前：工作熬夜
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(6).format(fmt),
                    sleepTime = "00:15", wakeTime = "06:30",
                    durationMinutes = 375, quality = 3,
                    moodBefore = "😰 焦虑", moodAfter = "😴 疲惫",
                    activityBefore = "工作/学习", dreams = "",
                    notes = "熬夜赶项目，睡眠不足，第二天状态很差",
                    isFavorite = false, createdAt = now, updatedAt = now
                ))
                // 6天前：冥想入睡
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(5).format(fmt),
                    sleepTime = "22:45", wakeTime = "06:45",
                    durationMinutes = 480, quality = 4,
                    moodBefore = "😐 平静", moodAfter = "😊 愉快",
                    activityBefore = "冥想", dreams = "梦见在沙滩散步，海浪声很清晰",
                    notes = "冥想10分钟后入睡，整体睡眠质量不错",
                    isFavorite = true, createdAt = now, updatedAt = now
                ))
                // 5天前：阅读深度睡眠
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(4).format(fmt),
                    sleepTime = "23:30", wakeTime = "07:00",
                    durationMinutes = 450, quality = 5,
                    moodBefore = "😊 愉快", moodAfter = "😊 愉快",
                    activityBefore = "阅读", dreams = "梦到在飞翔，俯瞰整个城市，感觉非常自由",
                    notes = "睡前看了半小时书，入睡很快，深度睡眠充足",
                    isFavorite = true, createdAt = now, updatedAt = now
                ))
                // 4天前：热水澡放松
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(3).format(fmt),
                    sleepTime = "23:15", wakeTime = "06:30",
                    durationMinutes = 435, quality = 4,
                    moodBefore = "😐 平静", moodAfter = "😊 愉快",
                    activityBefore = "洗澡", dreams = "梦见在考试，但题目都会做",
                    notes = "洗了个热水澡帮助放松，入睡比较顺利",
                    isFavorite = false, createdAt = now, updatedAt = now
                ))
                // 3天前：周末自然醒
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(2).format(fmt),
                    sleepTime = "23:00", wakeTime = "08:00",
                    durationMinutes = 540, quality = 5,
                    moodBefore = "😊 愉快", moodAfter = "😊 愉快",
                    activityBefore = "听音乐", dreams = "",
                    notes = "周末睡到自然醒，完美的休息日睡眠",
                    isFavorite = true, createdAt = now, updatedAt = now
                ))
                // 2天前：刷手机失眠
                repository.insertRecord(SleepRecordEntity(
                    date = today.minusDays(1).format(fmt),
                    sleepTime = "01:30", wakeTime = "09:00",
                    durationMinutes = 450, quality = 2,
                    moodBefore = "😤 烦躁", moodAfter = "😔 低落",
                    activityBefore = "玩手机", dreams = "",
                    notes = "周末睡前刷短视频停不下来，严重影响了睡眠",
                    isFavorite = false, createdAt = now, updatedAt = now
                ))
                // 昨天/今天：新一周早起
                repository.insertRecord(SleepRecordEntity(
                    date = today.format(fmt),
                    sleepTime = "22:30", wakeTime = "06:00",
                    durationMinutes = 450, quality = 4,
                    moodBefore = "😐 平静", moodAfter = "😊 愉快",
                    activityBefore = "阅读", dreams = "",
                    notes = "新的一周早睡早起，状态很好",
                    isFavorite = true, createdAt = now, updatedAt = now
                ))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "初始化数据失败: ${e.message}") }
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.defaultQualityFilter.collect { filter ->
                _uiState.update { it.copy(selectedQualityFilter = filter) }
            }
        }
    }

    fun loadRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val flow = when {
                    _uiState.value.searchQuery.isNotBlank() -> {
                        repository.searchRecords(_uiState.value.searchQuery)
                    }
                    _uiState.value.selectedQualityFilter != "ALL" -> {
                        val quality = _uiState.value.selectedQualityFilter.toIntOrNull() ?: 0
                        repository.getAllRecords()
                    }
                    else -> repository.getAllRecords()
                }

                flow.collect { records ->
                    val filtered = if (_uiState.value.selectedQualityFilter != "ALL") {
                        val quality = _uiState.value.selectedQualityFilter.toIntOrNull() ?: 0
                        records.filter { it.quality == quality }
                    } else {
                        records
                    }
                    _uiState.update { it.copy(records = filtered, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getRecordCount().collect { count ->
                _uiState.update { it.copy(totalCount = count) }
            }
        }
        viewModelScope.launch {
            repository.getAvgDuration().collect { avg ->
                _uiState.update { it.copy(avgDuration = avg) }
            }
        }
        viewModelScope.launch {
            repository.getAvgQuality().collect { avg ->
                _uiState.update { it.copy(avgQuality = avg) }
            }
        }
    }

    private fun loadWeekStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.with(DayOfWeek.MONDAY)
            val weekEnd = today.with(DayOfWeek.SUNDAY)
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            repository.getRecordsByDateRange(weekStart.format(fmt), weekEnd.format(fmt)).collect { records ->
                val avgDur = if (records.isNotEmpty()) records.map { it.durationMinutes }.average().toFloat() else 0f
                val avgQ = if (records.isNotEmpty()) records.map { it.quality }.average().toFloat() else 0f
                _uiState.update {
                    it.copy(
                        weekRecords = records,
                        weekAvgDuration = avgDur,
                        weekAvgQuality = avgQ
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadRecords()
        viewModelScope.launch { preferencesRepository.setLastSearchQuery(query) }
    }

    fun toggleSearch() {
        _uiState.update {
            it.copy(
                isSearchActive = !it.isSearchActive,
                searchQuery = if (it.isSearchActive) "" else it.searchQuery
            )
        }
        if (_uiState.value.isSearchActive) loadRecords()
    }

    fun setQualityFilter(quality: String) {
        _uiState.update { it.copy(selectedQualityFilter = quality) }
        loadRecords()
        viewModelScope.launch { preferencesRepository.setDefaultQualityFilter(quality) }
    }

    fun setTimeFilter(filter: String) {
        _uiState.update { it.copy(selectedTimeFilter = filter, isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val flow = when (filter) {
                    "WEEK" -> {
                        val weekStart = today.with(DayOfWeek.MONDAY)
                        val weekEnd = today.with(DayOfWeek.SUNDAY)
                        repository.getRecordsByDateRange(weekStart.format(fmt), weekEnd.format(fmt))
                    }
                    "MONTH" -> {
                        val monthStart = today.withDayOfMonth(1)
                        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
                        repository.getRecordsByDateRange(monthStart.format(fmt), monthEnd.format(fmt))
                    }
                    "GOOD" -> {
                        // 优眠：质量 >= 4
                        repository.getAllRecords()
                    }
                    "BAD" -> {
                        // 失眠：质量 <= 2
                        repository.getAllRecords()
                    }
                    else -> {
                        repository.getAllRecords()
                    }
                }
                flow.collect { records ->
                    val filtered = when (filter) {
                        "GOOD" -> records.filter { it.quality >= 4 }
                        "BAD" -> records.filter { it.quality <= 2 }
                        else -> records
                    }
                    _uiState.update { it.copy(records = filtered, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "筛选失败") }
            }
        }
    }

    fun clearFilters() {
        _uiState.update { it.copy(selectedQualityFilter = "ALL", selectedTimeFilter = "ALL", searchQuery = "") }
        loadRecords()
    }

    fun deleteRecord(record: SleepRecordEntity) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(record)
                loadStats()
                loadWeekStats()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "删除失败: ${e.message}") }
            }
        }
    }

    fun toggleFavorite(record: SleepRecordEntity) {
        viewModelScope.launch {
            try {
                repository.updateRecord(
                    record.copy(isFavorite = !record.isFavorite, updatedAt = System.currentTimeMillis())
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "操作失败: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
