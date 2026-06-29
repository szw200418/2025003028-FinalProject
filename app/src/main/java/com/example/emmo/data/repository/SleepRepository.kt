package com.example.emmo.data.repository

import com.example.emmo.data.dao.SleepRecordDao
import com.example.emmo.data.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

class SleepRepository(
    private val sleepRecordDao: SleepRecordDao
) {
    fun getAllRecords(): Flow<List<SleepRecordEntity>> = sleepRecordDao.getAllRecords()

    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<SleepRecordEntity>> =
        sleepRecordDao.getRecordsByDateRange(startDate, endDate)

    fun getFavoriteRecords(): Flow<List<SleepRecordEntity>> = sleepRecordDao.getFavoriteRecords()

    fun getRecordsWithNotes(): Flow<List<SleepRecordEntity>> = sleepRecordDao.getRecordsWithNotes()

    fun searchRecords(query: String): Flow<List<SleepRecordEntity>> = sleepRecordDao.searchRecords(query)

    fun getRecordCount(): Flow<Int> = sleepRecordDao.getRecordCount()

    fun getAvgDuration(): Flow<Float> = sleepRecordDao.getAvgDuration()

    fun getAvgQuality(): Flow<Float> = sleepRecordDao.getAvgQuality()

    fun getWeekRecordCount(startDate: String, endDate: String): Flow<Int> =
        sleepRecordDao.getWeekRecordCount(startDate, endDate)

    suspend fun getRecordById(id: Long): SleepRecordEntity? = sleepRecordDao.getRecordById(id)

    suspend fun getRecordByDate(date: String): SleepRecordEntity? = sleepRecordDao.getRecordByDate(date)

    suspend fun insertRecord(record: SleepRecordEntity): Long = sleepRecordDao.insertRecord(record)

    suspend fun updateRecord(record: SleepRecordEntity) = sleepRecordDao.updateRecord(record)

    suspend fun deleteRecord(record: SleepRecordEntity) = sleepRecordDao.deleteRecord(record)

    suspend fun deleteRecordById(id: Long) = sleepRecordDao.deleteRecordById(id)

    suspend fun deleteAllRecords() = sleepRecordDao.deleteAllRecords()
}
