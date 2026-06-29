package com.example.emmo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.emmo.data.entity.*
import com.example.emmo.ui.components.LoadingState
import com.example.emmo.viewmodel.SleepDetailViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepEditScreen(
    recordId: Long?,
    viewModel: SleepDetailViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isNewRecord = recordId == null || recordId == -1L

    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var sleepTime by remember { mutableStateOf("23:00") }
    var wakeTime by remember { mutableStateOf("07:00") }
    var quality by remember { mutableIntStateOf(3) }
    var moodBefore by remember { mutableStateOf("") }
    var moodAfter by remember { mutableStateOf("") }
    var activityBefore by remember { mutableStateOf("") }
    var dreams by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var showMoodPicker by remember { mutableStateOf<String?>(null) }
    var showActivityPicker by remember { mutableStateOf(false) }

    LaunchedEffect(recordId) {
        if (recordId != null && recordId > 0) viewModel.loadRecord(recordId)
    }

    LaunchedEffect(uiState.record) {
        if (!initialized && uiState.record != null) {
            val r = uiState.record!!
            date = r.date
            sleepTime = r.sleepTime
            wakeTime = r.wakeTime
            quality = r.quality
            moodBefore = r.moodBefore
            moodAfter = r.moodAfter
            activityBefore = r.activityBefore
            dreams = r.dreams
            notes = r.notes
            isFavorite = r.isFavorite
            initialized = true
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 心情选择弹窗
    if (showMoodPicker != null) {
        AlertDialog(
            onDismissRequest = { showMoodPicker = null },
            title = { Text("选择心情") },
            text = {
                Column {
                    MoodOption.moods.forEach { mood ->
                        TextButton(
                            onClick = {
                                if (showMoodPicker == "before") moodBefore = mood
                                else moodAfter = mood
                                showMoodPicker = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(mood)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 活动选择弹窗
    if (showActivityPicker) {
        AlertDialog(
            onDismissRequest = { showActivityPicker = false },
            title = { Text("选择睡前活动") },
            text = {
                Column {
                    ActivityOption.activities.forEach { act ->
                        TextButton(
                            onClick = { activityBefore = act; showActivityPicker = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(act)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isNewRecord) "添加睡眠记录" else "编辑睡眠记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (date.isBlank()) return@TextButton
                            if (isNewRecord) {
                                viewModel.saveNewRecord(
                                    com.example.emmo.data.entity.SleepRecordEntity(
                                        date = date,
                                        sleepTime = sleepTime,
                                        wakeTime = wakeTime,
                                        quality = quality,
                                        moodBefore = moodBefore,
                                        moodAfter = moodAfter,
                                        activityBefore = activityBefore,
                                        dreams = dreams,
                                        notes = notes,
                                        isFavorite = isFavorite
                                    )
                                ) { onSaveSuccess() }
                            } else {
                                uiState.record?.let { record ->
                                    viewModel.updateRecord(
                                        record.copy(
                                            date = date,
                                            sleepTime = sleepTime,
                                            wakeTime = wakeTime,
                                            quality = quality,
                                            moodBefore = moodBefore,
                                            moodAfter = moodAfter,
                                            activityBefore = activityBefore,
                                            dreams = dreams,
                                            notes = notes,
                                            isFavorite = isFavorite
                                        )
                                    )
                                    onSaveSuccess()
                                }
                            }
                        }
                    ) { Text("保存") }
                }
            )
        }
    ) { innerPadding ->
        if (!isNewRecord && uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 日期
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("日期 *") },
                    placeholder = { Text("yyyy-MM-dd") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = date.isBlank()
                )

                // 时间
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = { sleepTime = it },
                        label = { Text("入睡时间") },
                        placeholder = { Text("HH:mm") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text("起床时间") },
                        placeholder = { Text("HH:mm") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 睡眠质量
                Text("睡眠质量", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { q ->
                        FilterChip(
                            selected = quality == q,
                            onClick = { quality = q },
                            label = { Text("★".repeat(q) + "☆".repeat(5 - q)) }
                        )
                    }
                }

                // 睡前心情
                Text("睡前心情", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(
                    onClick = { showMoodPicker = "before" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(moodBefore.ifBlank { "选择睡前心情" })
                }

                // 起床心情
                Text("起床后心情", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(
                    onClick = { showMoodPicker = "after" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(moodAfter.ifBlank { "选择起床后心情" })
                }

                // 睡前活动
                Text("睡前活动", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(
                    onClick = { showActivityPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(activityBefore.ifBlank { "选择睡前活动" })
                }

                // 梦境
                OutlinedTextField(
                    value = dreams,
                    onValueChange = { dreams = it },
                    label = { Text("梦境记录") },
                    placeholder = { Text("记录你的梦境...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                // 备注
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注") },
                    placeholder = { Text("补充说明...") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                // 收藏标记
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("标记为优质睡眠", style = MaterialTheme.typography.labelLarge)
                    Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

