package com.algoritmika.prapp

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CalendarView
import java.util.Calendar

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // снова читаем уже обновлённый файл
        val updatedRanges = loadDateRanges(this)
        val insertedRange = updateDateRangesWithPrediction(this)

        setContent {
            var ranges by remember { mutableStateOf(insertedRange) }
            val grouped = groupRangesByMonth(ranges)

            ThreeZonesScreen(
                topContent = { MyScreen(MyViewModel()) },
                middleContent = {
                    DateListScreen(
                        rangesByMonth = grouped,
                        onUpdateRange = { oldStart, oldEnd, newStart, newEnd ->
                            ranges = ranges.map {
                                if (it.start == oldStart && it.endInclusive == oldEnd) {
                                    newStart..newEnd
                                } else it
                            }
                            saveDateRanges(this@MainActivity, ranges)
                        }
                    )
                },
                bottomContent = { Text("Нижняя панель") }
            )
        }
    }
}

@Composable
fun ThreeZonesScreen(
    topContent: @Composable () -> Unit,
    middleContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Верхняя зона (фиксированная высота)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray)
        ) {
            topContent()
        }

        // Средняя зона (занимает всё оставшееся место и скроллится)
        Box(
            modifier = Modifier
                .weight(1f) // ← основная магия
                .fillMaxWidth()
        ) {
            LazyColumn {
                item {
                    middleContent()
                }
            }
        }

        // Нижняя зона (фиксированная высота)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray)
        ) {
            bottomContent()
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
    println("История: ${history.size}")

    // Строим прогноз
    val prediction = predictFutureRanges(history, windowSize = 3)
    println("Прогноз: ${prediction.size}")

    // Объединяем
    val merged = history + prediction
    println("Объединено: ${merged.size}")


    // Сохраняем обратно
    internalFile.bufferedWriter().use { writer ->
        merged.forEach { range ->
            writer.write("${range.start.format(formatter)}, ${range.endInclusive.format(formatter)}\n")
        }
    }

    Log.d("PREDICT", "История: ${history.size}")
    Log.d("PREDICT", "Прогноз: ${prediction.size}")
    Log.d("PREDICT", "Объединено: ${merged.size}")

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

// ---------- UI ----------

@Composable
fun DateListScreen(
    rangesByMonth: Map<YearMonth, List<ClosedRange<LocalDate>>>,
    onUpdateRange: (LocalDate, LocalDate, LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var editingRange by remember { mutableStateOf<ClosedRange<LocalDate>?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        rangesByMonth.forEach { (month, ranges) ->
            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            ranges.forEach { range ->
                DateRangeItem(
                    start = range.start.format(formatter),
                    end = range.endInclusive.format(formatter),
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
    onEdit: () -> Unit
) {

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val startDate = LocalDate.parse(start, formatter)
    val isFuture = startDate.isAfter(LocalDate.now())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(if (isFuture) Color(0xFF9C27B0) else Color.DarkGray, // фиолетовый или серый
                shape = RoundedCornerShape(8.dp))
            .clickable { onEdit() }
            .padding(12.dp)
    ) {
        Text(
            text = "$start – $end",
            color = Color.White
        )
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
