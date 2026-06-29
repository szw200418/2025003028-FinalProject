package com.example.emmo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.emmo.data.dao.SleepRecordDao
import com.example.emmo.data.entity.SleepRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [SleepRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sleepRecordDao(): SleepRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sleep_tracker_db"
                )
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.sleepRecordDao()?.let { dao ->
                        sampleRecords.forEach { dao.insertRecord(it) }
                    }
                }
            }

            private val sampleRecords = listOf(
                SleepRecordEntity(
                    date = "2026-06-23",
                    sleepTime = "00:15",
                    wakeTime = "06:30",
                    durationMinutes = 375,
                    quality = 3,
                    moodBefore = "😰 焦虑",
                    moodAfter = "😴 疲惫",
                    activityBefore = "工作/学习",
                    dreams = "",
                    notes = "周一熬夜赶项目，睡眠不足，第二天状态很差",
                    isFavorite = false
                ),
                SleepRecordEntity(
                    date = "2026-06-24",
                    sleepTime = "22:45",
                    wakeTime = "06:45",
                    durationMinutes = 480,
                    quality = 4,
                    moodBefore = "😐 平静",
                    moodAfter = "😊 愉快",
                    activityBefore = "冥想",
                    dreams = "梦见在沙滩散步，海浪声很清晰",
                    notes = "冥想10分钟后入睡，整体睡眠质量不错",
                    isFavorite = true
                ),
                SleepRecordEntity(
                    date = "2026-06-25",
                    sleepTime = "23:30",
                    wakeTime = "07:00",
                    durationMinutes = 450,
                    quality = 5,
                    moodBefore = "😊 愉快",
                    moodAfter = "😊 愉快",
                    activityBefore = "阅读",
                    dreams = "梦到在飞翔，俯瞰整个城市，感觉非常自由",
                    notes = "睡前看了半小时书，入睡很快，深度睡眠充足",
                    isFavorite = true
                ),
                SleepRecordEntity(
                    date = "2026-06-26",
                    sleepTime = "23:15",
                    wakeTime = "06:30",
                    durationMinutes = 435,
                    quality = 4,
                    moodBefore = "😐 平静",
                    moodAfter = "😊 愉快",
                    activityBefore = "洗澡",
                    dreams = "梦见在考试，但题目都会做",
                    notes = "洗了个热水澡帮助放松，入睡比较顺利",
                    isFavorite = false
                ),
                SleepRecordEntity(
                    date = "2026-06-27",
                    sleepTime = "23:00",
                    wakeTime = "08:00",
                    durationMinutes = 540,
                    quality = 5,
                    moodBefore = "😊 愉快",
                    moodAfter = "😊 愉快",
                    activityBefore = "听音乐",
                    dreams = "",
                    notes = "周末睡到自然醒，完美的休息日睡眠",
                    isFavorite = true
                ),
                SleepRecordEntity(
                    date = "2026-06-28",
                    sleepTime = "01:30",
                    wakeTime = "09:00",
                    durationMinutes = 450,
                    quality = 2,
                    moodBefore = "😤 烦躁",
                    moodAfter = "😔 低落",
                    activityBefore = "玩手机",
                    dreams = "",
                    notes = "周末睡前刷短视频停不下来，严重影响了睡眠",
                    isFavorite = false
                ),
                SleepRecordEntity(
                    date = "2026-06-29",
                    sleepTime = "22:30",
                    wakeTime = "06:00",
                    durationMinutes = 450,
                    quality = 4,
                    moodBefore = "😐 平静",
                    moodAfter = "😊 愉快",
                    activityBefore = "阅读",
                    dreams = "",
                    notes = "新的一周早睡早起，状态很好",
                    isFavorite = true
                )
            )
        }
    }
}
