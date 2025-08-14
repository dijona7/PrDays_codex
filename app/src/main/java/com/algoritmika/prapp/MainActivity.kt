package com.algoritmika.prapp

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // снова читаем уже обновлённый файл
        val updatedRanges = loadDateRanges(this)
        val insertedRange = updateDateRangesWithPrediction(this)

        setContent {
            val ranges by remember { mutableStateOf(insertedRange) }
            // val grouped = groupRangesByMonth(ranges)
            val grouped = groupRangesByMonthWithGlobalGaps(ranges)

            MainScreen(ranges, grouped) { updated ->
                saveDateRanges(this, updated)
            }
        }
    }
}


@Composable
fun MainScreen(
    ranges: List<ClosedRange<LocalDate>>,
    grouped: Map<YearMonth, List<Pair<ClosedRange<LocalDate>, Long>>>,
    onSave: (List<ClosedRange<LocalDate>>) -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("calendar") }
    var currentRanges by remember { mutableStateOf(ranges) }


    ThreeZonesScreen(
        topContent = { CycleInfo(currentRanges) },
        middleContent = {
            when (currentTab) {
                "calendar" -> DateListScreen(
                    rangesByMonth = grouped,
                    onUpdateRange = { oldStart, oldEnd, newStart, newEnd ->
                        currentRanges = currentRanges.map {
                            if (it.start == oldStart && it.endInclusive == oldEnd) {
                                newStart..newEnd
                            } else it
                        }
                        onSave(currentRanges)
                    }
                )
                "file" -> FileEditorScreen(context) { newRanges ->
                    currentRanges = newRanges
                    onSave(currentRanges)
                }
                "settings" -> SettingsScreen()
            }
        },
        selectedTab = currentTab,
        onTabSelected = { currentTab = it }
    )
}



@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings screen")
    }
}



@Composable
fun ThreeZonesScreen(
    topContent: @Composable () -> Unit,
    middleContent: @Composable () -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
            ) {
                topContent()
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "calendar",
                    onClick = { onTabSelected("calendar") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = selectedTab == "file",
                    onClick = { onTabSelected("file") },
                    icon = { Icon(Icons.Default.Info, contentDescription = "File") },
                    label = { Text("File") }
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { onTabSelected("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            middleContent()
        }
    }
}


// ---------- ЛОГИКА РАБОТЫ С ФАЙЛОМ ----------

fun loadDateRanges(context: Context): List<ClosedRange<LocalDate>> {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val internalFile = File(context.filesDir, "date_ranges.txt")

    // Если внутреннего файла нет — копируем из assets
    if (!internalFile.exists()) {
        context.assets.open("date_ranges.txt").use { input ->
            internalFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    val today = LocalDate.now()
    // Теперь читаем из внутреннего файла
    // Читаем и фильтруем
    return internalFile.bufferedReader().useLines { lines ->
        lines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                val start = LocalDate.parse(parts[0].trim(), formatter)
                val end = LocalDate.parse(parts[1].trim(), formatter)
                start..end
            } else null
        }
            // фильтруем только те, что начинаются не позже сегодняшнего дня
            .filter { it.start.isBefore(today) || it.start.isEqual(today) }
            .toList()
    }
}


fun updateDateRangesWithPrediction(context: Context): List<ClosedRange<LocalDate>> {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val internalFile = File(context.filesDir, "date_ranges.txt")

    // Загружаем историю
    val history = loadDateRanges(context)

    // Строим прогноз
    val prediction =predictFutureRanges(history, windowSize = 3)

    // Объединяем
    val merged = history + prediction

    // Сохраняем обратно
    internalFile.bufferedWriter().use { writer ->
        merged.forEach { range -> writer.write("${range.start.format(formatter)}, ${range.endInclusive.format(formatter)}\n")
        }
    }

    return merged
}


fun saveDateRanges(context: Context, ranges: List<ClosedRange<LocalDate>>) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val file = File(context.filesDir, "date_ranges.txt")
    file.printWriter().use { out ->
        ranges.forEach { range ->
            out.println("${range.start.format(formatter)},${range.endInclusive.format(formatter)}")
        }
    }
}

fun groupRangesByMonth(ranges: List<ClosedRange<LocalDate>>): Map<YearMonth, List<ClosedRange<LocalDate>>> {
    return ranges.groupBy { YearMonth.from(it.start) }
        .toSortedMap(compareByDescending { it }) // свежие месяцы сверху
}

fun groupRangesByMonthWithGlobalGaps(
    ranges: List<ClosedRange<LocalDate>>
): Map<YearMonth, List<Pair<ClosedRange<LocalDate>, Long>>> {
    val sorted = ranges.sortedBy { it.start } // общий список по возрастанию
    val gapsMap = mutableMapOf<ClosedRange<LocalDate>, Long>()

    // сначала считаем gap сквозь все месяцы
    sorted.forEachIndexed { index, range ->
        if (index == 0) {
            gapsMap[range] = 0L // для первого разница = 0
        } else {
            val prevStart = sorted[index - 1].start
            val gap = java.time.temporal.ChronoUnit.DAYS.between(prevStart, range.start)
            gapsMap[range] = gap
        }
    }

    // потом группируем по месяцу начала
    return sorted
        .groupBy { YearMonth.from(it.start) }
        .mapValues { (_, monthRanges) ->
            monthRanges.map { range -> range to (gapsMap[range] ?: 0L) }
        }
        .toSortedMap(compareByDescending { it })
}

// ---------- UI ----------

@Composable
fun DateListScreen(
    rangesByMonth: Map<YearMonth, List<Pair<ClosedRange<LocalDate>, Long>>>,
    onUpdateRange: (LocalDate, LocalDate, LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var editingRange by remember { mutableStateOf<ClosedRange<LocalDate>?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        rangesByMonth.forEach { (month, list) ->
            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            list.forEach { (range, gap) ->
                DateRangeItem(
                    start = range.start.format(formatter),
                    end = range.endInclusive.format(formatter),
                    gap = "$gap",
                    onEdit = { editingRange = range }
                )
            }
        }
    }

    editingRange?.let { range ->
        DateEditDialog(
            initialStart = range.start,
            initialEnd = range.endInclusive,
            onConfirm = { newStart, newEnd ->
                onUpdateRange(range.start, range.endInclusive, newStart, newEnd)
                editingRange = null
            },
            onDismiss = { editingRange = null }
        )
    }
}



@Composable
fun DateRangeItem(
    start: String,
    end: String,
    gap: String,
    onEdit: () -> Unit
) {

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val startDate = LocalDate.parse(start, formatter)
    val isFuture = startDate.isAfter(LocalDate.now())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(if (isFuture) Color(0xFF664FA3) else Color.DarkGray, // фиолетовый или серый
                shape = RoundedCornerShape(8.dp))
            .clickable { onEdit() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(
                text = "$start – $end",
                color = Color.White
            )
            Text(
                text = "$gap",
                color = Color.White,
                fontSize = 18.sp,          // больше размер
                fontWeight = FontWeight.Bold // жирный
            )
        }
    }
}

@Composable
fun DateEditDialog(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf(initialStart) }
    var endDate by remember { mutableStateOf(initialEnd) }
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить даты") },
        text = {
            Column {
                Button(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            startDate = LocalDate.of(year, month + 1, day)
                        },
                        startDate.year,
                        startDate.monthValue - 1,
                        startDate.dayOfMonth
                    ).show()
                }) {
                    Text("Начало: ${startDate.format(formatter)}")
                }

                Button(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            endDate = LocalDate.of(year, month + 1, day)
                        },
                        endDate.year,
                        endDate.monthValue - 1,
                        endDate.dayOfMonth
                    ).show()
                }) {
                    Text("Конец: ${endDate.format(formatter)}")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(startDate, endDate) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun MyScreen(viewModel: MyViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = viewModel.message.value)
        Button(
            onClick = { viewModel.toggleMessage() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Изменить текст")
        }
        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    val today = Calendar.getInstance()
                    setDate(today.timeInMillis, false, true)
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}



