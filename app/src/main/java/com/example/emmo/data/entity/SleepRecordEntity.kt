package com.example.emmo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String,  // yyyy-MM-dd

    @ColumnInfo(name = "sleep_time")
    val sleepTime: String,  // HH:mm 入睡时间

    @ColumnInfo(name = "wake_time")
    val wakeTime: String,  // HH:mm 起床时间

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 0,  // 睡眠时长（分钟）

    @ColumnInfo(name = "quality")
    val quality: Int = 3,  // 1-5 睡眠质量评分

    @ColumnInfo(name = "mood_before")
    val moodBefore: String = "",  // 睡前心情

    @ColumnInfo(name = "mood_after")
    val moodAfter: String = "",  // 起床后心情

    @ColumnInfo(name = "activity_before")
    val activityBefore: String = "",  // 睡前活动

    @ColumnInfo(name = "dreams")
    val dreams: String = "",  // 梦境记录

    @ColumnInfo(name = "notes")
    val notes: String = "",  // 备注

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,  // 标记为优质睡眠

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

object MoodOption {
    val moods = listOf("😊 愉快", "😐 平静", "😔 低落", "😰 焦虑", "😤 烦躁", "😴 疲惫")

    fun displayName(mood: String): String = mood
}

object ActivityOption {
    val activities = listOf("阅读", "听音乐", "冥想", "运动", "玩手机", "工作/学习", "看电视", "洗澡", "无")
}
