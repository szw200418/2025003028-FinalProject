package com.example.emmo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emmo.data.entity.*
import com.example.emmo.ui.components.SleepCard
import com.example.emmo.viewmodel.SleepListViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: SleepListViewModel,
    onRecordClick: (Long) -> Unit,
    onAddRecordClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "睡眠统计",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    getDateRangeText(uiState.records),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            IconButton(
                onClick = onAddRecordClick,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加记录",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }

        // 统计卡片横排
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatMiniCard(
                    icon = Icons.Default.Bedtime,
                    label = "记录天数",
                    value = "${uiState.records.size}",
                    unit = "天",
                    color = Color(0xFF7C4DFF)
                )
            }
            item {
                val avgDuration = if (uiState.records.isNotEmpty())
                    uiState.records.map { it.durationMinutes }.average().toInt()
                else 0
                StatMiniCard(
                    icon = Icons.Default.Schedule,
                    label = "平均时长",
                    value = formatDuration(avgDuration),
                    unit = "",
                    color = Color(0xFF00BFA5)
                )
            }
            item {
                val avgQuality = if (uiState.records.isNotEmpty())
                    uiState.records.map { it.quality }.average().toFloat()
                else 0f
                StatMiniCard(
                    icon = Icons.Default.Star,
                    label = "平均质量",
                    value = String.format("%.1f", avgQuality),
                    unit = "/5",
                    color = Color(0xFFFFAB00)
                )
            }
            item {
                val bestStreak = calcBestStreak(uiState.records)
                StatMiniCard(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "最长连续",
                    value = "$bestStreak",
                    unit = "天",
                    color = Color(0xFFFF5252)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 筛选标签
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "全部" to "ALL",
                "本周" to "WEEK",
                "本月" to "MONTH",
                "优眠" to "GOOD",
                "失眠" to "BAD"
            )
            items(filters) { (label, key) ->
                val selected = uiState.selectedTimeFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setTimeFilter(key) },
                    label = {
                        Text(
                            label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 记录列表
        if (uiState.records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🌙", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("还没有睡眠记录", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("点击右上角 + 开始记录", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.records, key = { it.id }) { record ->
                    SleepCard(
                        record = record,
                        onClick = { onRecordClick(record.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(record) }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun StatMiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (unit.isNotEmpty()) {
                    Text(
                        unit,
                        fontSize = 12.sp,
                        color = color.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}.${m / 6}h"
}

fun calcBestStreak(records: List<SleepRecordEntity>): Int {
    if (records.isEmpty()) return 0
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sorted = records.sortedBy { it.date }
    var streak = 1
    var best = 1
    val cal = Calendar.getInstance()
    for (i in 1 until sorted.size) {
        cal.time = sdf.parse(sorted[i - 1].date)!!
        cal.add(Calendar.DAY_OF_YEAR, 1)
        if (sdf.format(cal.time) == sorted[i].date) {
            streak++
            if (streak > best) best = streak
        } else {
            streak = 1
        }
    }
    return best
}

fun getDateRangeText(records: List<SleepRecordEntity>): String {
    if (records.isEmpty()) return "暂无记录"
    val sorted = records.sortedBy { it.date }
    return "${sorted.first().date} - ${sorted.last().date}"
}
