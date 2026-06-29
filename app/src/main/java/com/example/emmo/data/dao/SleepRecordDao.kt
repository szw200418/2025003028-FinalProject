package com.example.emmo.data.dao

import androidx.room.*
import com.example.emmo.data.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {

    @Query("SELECT * FROM sleep_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE id = :id")
    suspend fun getRecordById(id: Long): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE is_favorite = 1 ORDER BY date DESC")
    fun getFavoriteRecords(): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE notes != '' OR dreams != '' ORDER BY date DESC")
    fun getRecordsWithNotes(): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE date LIKE '%' || :query || '%' OR dreams LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchRecords(query: String): Flow<List<SleepRecordEntity>>

    @Query("SELECT COUNT(*) FROM sleep_records")
    fun getRecordCount(): Flow<Int>

    @Query("SELECT AVG(duration_minutes) FROM sleep_records")
    fun getAvgDuration(): Flow<Float>

    @Query("SELECT AVG(quality) FROM sleep_records")
    fun getAvgQuality(): Flow<Float>

    @Query("SELECT COUNT(*) FROM sleep_records WHERE date BETWEEN :startDate AND :endDate")
    fun getWeekRecordCount(startDate: String, endDate: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SleepRecordEntity): Long

    @Update
    suspend fun updateRecord(record: SleepRecordEntity)

    @Delete
    suspend fun deleteRecord(record: SleepRecordEntity)

    @Query("DELETE FROM sleep_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM sleep_records")
    suspend fun deleteAllRecords()
}
