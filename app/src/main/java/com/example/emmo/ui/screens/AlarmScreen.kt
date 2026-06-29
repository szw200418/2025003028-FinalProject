package com.example.emmo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val days: List<String>,
    val isEnabled: Boolean
) {
    val time: String
        get() = String.format("%02d:%02d", hour, minute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen() {
    var alarms by remember {
        mutableStateOf(
            listOf(
                Alarm(1, 7, 30, "工作日起床", listOf("一", "二", "三", "四", "五"), true),
                Alarm(2, 8, 30, "周末起床", listOf("六", "日"), true),
                Alarm(3, 22, 30, "睡觉提醒", listOf("一", "二", "三", "四", "五", "六", "日"), true),
                Alarm(4, 23, 0, "放下手机", listOf("一", "二", "三", "四", "五", "六", "日"), false)
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }

    // 计算下次闹钟
    val nextAlarm = remember(alarms) {
        alarms.filter { it.isEnabled }.minByOrNull { alarm ->
            val cal = Calendar.getInstance()
            val currentDay = cal.get(Calendar.DAY_OF_WEEK)
            val dayMap = mapOf("日" to 1, "一" to 2, "二" to 3, "三" to 4, "四" to 5, "五" to 6, "六" to 7)
            val targetDays = alarm.days.mapNotNull { dayMap[it] }

            var daysUntil = 0
            var found = false
            for (i in 0..7) {
                val checkDay = ((currentDay - 1 + i) % 7) + 1
                if (checkDay in targetDays) {
                    if (i == 0) {
                        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                        val currentMin = cal.get(Calendar.MINUTE)
                        if (alarm.hour > currentHour || (alarm.hour == currentHour && alarm.minute > currentMin)) {
                            found = true
                            break
                        }
                    } else {
                        daysUntil = i
                        found = true
                        break
                    }
                }
            }
            if (!found) 999 else daysUntil * 24 * 60 + alarm.hour * 60 + alarm.minute
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 顶部时钟区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "下次闹钟",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (nextAlarm != null) {
                    val timeUntil = calculateTimeUntil(nextAlarm)
                    Text(
                        text = timeUntil,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextAlarm.time,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "无开启的闹钟",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "我的闹钟",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 闹钟列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = {
                        alarms = alarms.map {
                            if (it.id == alarm.id) it.copy(isEnabled = !it.isEnabled) else it
                        }
                    },
                    onClick = { editingAlarm = alarm },
                    onDelete = {
                        alarms = alarms.filter { it.id != alarm.id }
                    }
                )
            }
        }
    }

    // 添加闹钟对话框
    if (showAddDialog) {
        AlarmEditDialog(
            alarm = null,
            onDismiss = { showAddDialog = false },
            onSave = { newAlarm ->
                val newId = (alarms.maxOfOrNull { it.id } ?: 0) + 1
                alarms = alarms + newAlarm.copy(id = newId)
                showAddDialog = false
            }
        )
    }

    // 编辑闹钟对话框
    if (editingAlarm != null) {
        AlarmEditDialog(
            alarm = editingAlarm,
            onDismiss = { editingAlarm = null },
            onSave = { updatedAlarm ->
                alarms = alarms.map {
                    if (it.id == updatedAlarm.id) updatedAlarm else it
                }
                editingAlarm = null
            },
            onDelete = {
                alarms = alarms.filter { it.id != editingAlarm!!.id }
                editingAlarm = null
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.time,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 重复日期
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    alarm.days.forEach { day ->
                        Surface(
                            shape = CircleShape,
                            color = if (alarm.isEnabled)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (alarm.isEnabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // 开关
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var hour by remember { mutableIntStateOf(alarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 30) }
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    val allDays = listOf("一", "二", "三", "四", "五", "六", "日")
    var selectedDays by remember { mutableStateOf(alarm?.days ?: listOf("一", "二", "三", "四", "五")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (alarm == null) "添加闹钟" else "编辑闹钟",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 时间选择器（简化版）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 小时
                    NumberPicker(
                        value = hour,
                        onValueChange = { hour = it },
                        range = 0..23
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                    // 分钟
                    NumberPicker(
                        value = minute,
                        onValueChange = { minute = it },
                        range = 0..59
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 标签输入
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("标签") },
                    placeholder = { Text("例如：起床、睡觉提醒") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 重复日期选择
                Text("重复", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    allDays.forEach { day ->
                        val selected = day in selectedDays
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    selectedDays = if (selected) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                            shape = CircleShape,
                            color = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    day,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                        ) {
                            Text("删除")
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                Alarm(
                                    id = alarm?.id ?: 0,
                                    hour = hour,
                                    minute = minute,
                                    label = label.ifBlank { "闹钟" },
                                    days = selectedDays,
                                    isEnabled = alarm?.isEnabled ?: true
                                )
                            )
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = { if (value < range.last) onValueChange(value + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "增加")
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                String.format("%02d", value),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        IconButton(
            onClick = { if (value > range.first) onValueChange(value - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "减少")
        }
    }
}

fun calculateTimeUntil(alarm: Alarm): String {
    val cal = Calendar.getInstance()
    val currentHour = cal.get(Calendar.HOUR_OF_DAY)
    val currentMin = cal.get(Calendar.MINUTE)
    val currentDay = cal.get(Calendar.DAY_OF_WEEK)

    val dayMap = mapOf("日" to 1, "一" to 2, "二" to 3, "三" to 4, "四" to 5, "五" to 6, "六" to 7)
    val targetDays = alarm.days.mapNotNull { dayMap[it] }

    var daysUntil = 0
    var found = false
    for (i in 0..7) {
        val checkDay = ((currentDay - 1 + i) % 7) + 1
        if (checkDay in targetDays) {
            if (i == 0) {
                if (alarm.hour > currentHour || (alarm.hour == currentHour && alarm.minute > currentMin)) {
                    found = true
                    break
                }
            } else {
                daysUntil = i
                found = true
                break
            }
        }
    }

    if (!found) return "无"

    val totalMinutes = daysUntil * 24 * 60 + (alarm.hour - currentHour) * 60 + (alarm.minute - currentMin)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    return when {
        daysUntil == 0 && hours == 0 -> "${mins}分钟后"
        daysUntil == 0 -> "${hours}小时 ${mins}分钟后"
        daysUntil == 1 -> "明天 ${alarm.time}"
        else -> "${daysUntil}天后"
    }
}
