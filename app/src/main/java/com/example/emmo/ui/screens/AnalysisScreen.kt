package com.example.emmo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emmo.viewmodel.SleepListViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun AnalysisScreen(sleepListViewModel: SleepListViewModel = viewModel()) {
    val uiState by sleepListViewModel.uiState.collectAsState()
    val records = uiState.records

    // 计算统计数据
    val stats = remember(records) { calculateSleepStats(records) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            "睡眠分析",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // 本周睡眠质量趋势图
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "近7天睡眠质量",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                val weekData = getLast7DaysData(records)
                if (weekData.any { it.second > 0 }) {
                    QualityBarChart(weekData, modifier = Modifier.fillMaxWidth().height(160.dp))
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无数据", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 睡眠评分
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "睡眠评分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 评分圆环
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        CircularProgressIndicator(
                            progress = { stats.avgQuality / 5f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = qualityColor(stats.avgQuality),
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", stats.avgQuality),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = qualityColor(stats.avgQuality)
                            )
                            Text(
                                "/5",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // 细分指标
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricItem("入睡时间", stats.avgSleepTime, stats.sleepTimeStatus)
                        MetricItem("起床时间", stats.avgWakeTime, stats.wakeTimeStatus)
                        MetricItem("总时长", "${stats.avgDuration}h", stats.durationStatus)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 睡眠趋势卡片
        if (records.size >= 3) {
            TrendCard(stats)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 睡眠质量分布
        if (records.isNotEmpty()) {
            QualityDistributionCard(records)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 动态睡眠建议
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "💡 睡眠建议",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val suggestions = generateSleepSuggestions(stats, records)
                suggestions.forEachIndexed { index, suggestion ->
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• $suggestion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TrendCard(stats: SleepStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "睡眠趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TrendItem(
                    icon = Icons.Default.TrendingUp,
                    label = "质量趋势",
                    value = stats.qualityTrend,
                    color = if (stats.qualityTrend == "上升") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
                TrendItem(
                    icon = Icons.Default.Schedule,
                    label = "时长趋势",
                    value = stats.durationTrend,
                    color = if (stats.durationTrend == "稳定") Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
                TrendItem(
                    icon = Icons.Default.Bedtime,
                    label = "入睡规律",
                    value = stats.sleepRegularity,
                    color = if (stats.sleepRegularity == "规律") Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun TrendItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
fun QualityDistributionCard(records: List<com.example.emmo.data.entity.SleepRecordEntity>) {
    val distribution = remember(records) {
        listOf(
            records.count { it.quality == 5 } to "优秀",
            records.count { it.quality == 4 } to "良好",
            records.count { it.quality == 3 } to "一般",
            records.count { it.quality <= 2 } to "较差"
        )
    }
    val total = records.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "质量分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            distribution.forEach { (count, label) ->
                val percentage = if (total > 0) (count * 100 / total) else 0
                val color = when (label) {
                    "优秀" -> Color(0xFF4CAF50)
                    "良好" -> Color(0xFF8BC34A)
                    "一般" -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, modifier = Modifier.width(48.dp), style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(
                        progress = { percentage / 100f },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, status: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        val statusColor = when (status) {
            "准时", "规律", "达标", "稳定" -> Color(0xFF4CAF50)
            "偏晚", "偏早", "不足", "过长" -> Color(0xFFFF9800)
            else -> Color(0xFF9E9E9E)
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusColor.copy(alpha = 0.12f),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}

@Composable
fun QualityBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 2f)
        val maxHeight = size.height * 0.8f
        val gap = barWidth

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value / 5f) * maxHeight
            val x = index * (barWidth + gap) + gap / 2f
            val y = size.height - barHeight - 20f

            drawRoundRect(
                color = qualityColor(value),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidth / 2f,
                size.height - 4f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 28f
                }
            )
        }
    }
}

fun qualityColor(quality: Float): Color = when {
    quality >= 4.5f -> Color(0xFF4CAF50)
    quality >= 4f -> Color(0xFF8BC34A)
    quality >= 3f -> Color(0xFFFFC107)
    quality >= 2f -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

// 睡眠统计数据类
data class SleepStats(
    val avgQuality: Float = 0f,
    val avgDuration: Float = 0f,
    val avgSleepTime: String = "--:--",
    val avgWakeTime: String = "--:--",
    val sleepTimeStatus: String = "--",
    val wakeTimeStatus: String = "--",
    val durationStatus: String = "--",
    val qualityTrend: String = "--",
    val durationTrend: String = "--",
    val sleepRegularity: String = "--"
)

fun calculateSleepStats(records: List<com.example.emmo.data.entity.SleepRecordEntity>): SleepStats {
    if (records.isEmpty()) return SleepStats()

    val sortedRecords = records.sortedBy { it.date }

    // 平均质量
    val avgQuality = records.map { it.quality }.average().toFloat()

    // 平均时长
    val avgDuration = (records.map { it.durationMinutes }.average() / 60.0).toFloat()

    // 解析时间
    fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        return Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
    }

    // 计算平均入睡时间
    val sleepTimes = records.map { parseTime(it.sleepTime) }
    val avgSleepHour = sleepTimes.map { it.first }.average().roundToInt()
    val avgSleepMin = sleepTimes.map { it.second }.average().roundToInt()
    val avgSleepTime = String.format("%02d:%02d", avgSleepHour, avgSleepMin)

    // 计算平均起床时间
    val wakeTimes = records.map { parseTime(it.wakeTime) }
    val avgWakeHour = wakeTimes.map { it.first }.average().roundToInt()
    val avgWakeMin = wakeTimes.map { it.second }.average().roundToInt()
    val avgWakeTime = String.format("%02d:%02d", avgWakeHour, avgWakeMin)

    // 状态判断
    val sleepTimeStatus = when {
        avgSleepHour in 21..23 -> "准时"
        avgSleepHour < 21 -> "偏早"
        else -> "偏晚"
    }

    val wakeTimeStatus = when {
        avgWakeHour in 6..8 -> "规律"
        avgWakeHour < 6 -> "偏早"
        else -> "偏晚"
    }

    val durationStatus = when {
        avgDuration in 7.0..9.0 -> "达标"
        avgDuration < 7.0 -> "不足"
        else -> "过长"
    }

    // 趋势分析（最近3天 vs 之前）
    val recent = records.takeLast(3)
    val previous = records.dropLast(3)

    val qualityTrend = if (previous.isNotEmpty()) {
        val recentAvg = recent.map { it.quality }.average()
        val prevAvg = previous.map { it.quality }.average()
        when {
            recentAvg > prevAvg + 0.3 -> "上升"
            recentAvg < prevAvg - 0.3 -> "下降"
            else -> "稳定"
        }
    } else "--"

    val durationTrend = if (previous.isNotEmpty()) {
        val recentAvg = recent.map { it.durationMinutes }.average()
        val prevAvg = previous.map { it.durationMinutes }.average()
        when {
            recentAvg > prevAvg + 30 -> "增加"
            recentAvg < prevAvg - 30 -> "减少"
            else -> "稳定"
        }
    } else "--"

    // 入睡规律性
    val sleepHourStd = sleepTimes.map { it.first }.let { hours ->
        val mean = hours.average()
        kotlin.math.sqrt(hours.map { (it - mean) * (it - mean) }.average())
    }
    val sleepRegularity = if (sleepHourStd < 1.5) "规律" else "波动"

    return SleepStats(
        avgQuality = avgQuality,
        avgDuration = avgDuration,
        avgSleepTime = avgSleepTime,
        avgWakeTime = avgWakeTime,
        sleepTimeStatus = sleepTimeStatus,
        wakeTimeStatus = wakeTimeStatus,
        durationStatus = durationStatus,
        qualityTrend = qualityTrend,
        durationTrend = durationTrend,
        sleepRegularity = sleepRegularity
    )
}

fun generateSleepSuggestions(stats: SleepStats, records: List<com.example.emmo.data.entity.SleepRecordEntity>): List<String> {
    val suggestions = mutableListOf<String>()

    if (records.isEmpty()) {
        return listOf("开始记录睡眠，获取个性化建议。")
    }

    // 基于质量的建议
    when {
        stats.avgQuality >= 4.5f -> suggestions.add("你的睡眠质量很棒，继续保持！")
        stats.avgQuality >= 3.5f -> suggestions.add("睡眠质量良好，尝试睡前冥想进一步提升。")
        stats.avgQuality >= 2.5f -> suggestions.add("睡眠质量一般，建议检查睡眠环境。")
        else -> suggestions.add("睡眠质量较差，建议咨询医生或调整作息。")
    }

    // 基于时长的建议
    when (stats.durationStatus) {
        "不足" -> suggestions.add("睡眠时间不足，建议提前30分钟上床。")
        "过长" -> suggestions.add("睡眠时间偏长，过长睡眠可能导致昏沉。")
        "达标" -> suggestions.add("睡眠时长很健康，继续保持！")
    }

    // 基于入睡时间的建议
    when (stats.sleepTimeStatus) {
        "偏晚" -> suggestions.add("入睡时间较晚，尝试每天提前15分钟上床。")
        "偏早" -> suggestions.add("入睡时间较早，如果白天精神好就保持。")
        "准时" -> suggestions.add("入睡时间很规律，有利于生物钟稳定。")
    }

    // 基于规律性的建议
    if (stats.sleepRegularity == "波动") {
        suggestions.add("入睡时间波动较大，建议固定睡前仪式。")
    }

    // 基于趋势的建议
    if (stats.qualityTrend == "下降") {
        suggestions.add("近期睡眠质量下降，检查是否有压力或环境变化。")
    }

    // 通用建议
    val randomTips = listOf(
        "睡前1小时避免使用电子设备。",
        "保持卧室温度在18-22°C之间。",
        "周末也尽量保持固定起床时间。",
        "下午3点后避免摄入咖啡因。",
        "尝试睡前阅读或听轻音乐放松。"
    )
    suggestions.add(randomTips.random())

    return suggestions.take(4)
}

fun getLast7DaysData(records: List<com.example.emmo.data.entity.SleepRecordEntity>): List<Pair<String, Float>> {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val result = mutableListOf<Pair<String, Float>>()

    for (i in 6 downTo 0) {
        cal.time = Date()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val dateStr = sdf.format(cal.time)
        val targetDateStr = dateSdf.format(cal.time)
        val record = records.find { it.date == targetDateStr }
        result.add(dateStr to (record?.quality?.toFloat() ?: 0f))
    }
    return result
}
