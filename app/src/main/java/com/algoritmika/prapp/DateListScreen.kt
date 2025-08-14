package com.algoritmika.prapp

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateListScreen(
    rangesByMonth: Map<YearMonth, List<Pair<ClosedRange<LocalDate>, Long>>>,
    onUpdateRange: (LocalDate, LocalDate, LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var editingRange by remember { mutableStateOf<ClosedRange<LocalDate>?>(null) }

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        rangesByMonth.forEach { (month, list) ->
            Text(
                text = "${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            list.forEach { (range, gap) ->
                val isFuture = range.start.isAfter(LocalDate.now())
                DateRangeItem(
                    start = range.start.format(formatter),
                    end = range.endInclusive.format(formatter),
                    gap = "$gap",
                    isFuture = isFuture,
                    onEdit = { editingRange = range }
                )
            }
        }
    }

    editingRange?.let { range ->
        val isFutureRange = range.start.isAfter(LocalDate.now())
        DateEditDialog(
            initialStart = range.start,
            initialEnd = range.endInclusive,
            onConfirm = { newStart, newEnd ->
                onUpdateRange(range.start, range.endInclusive, newStart, newEnd)
                editingRange = null
            },
            onDismiss = { editingRange = null },
            editable = !isFutureRange
        )
    }
}

@Composable
fun DateRangeItem(
    start: String,
    end: String,
    gap: String,
    isFuture: Boolean,
    onEdit: () -> Unit
) {
    val statusColor = if (isFuture) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$start – $end",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(statusColor, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = gap,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DateEditDialog(
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    editable: Boolean = true
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf(initialStart) }
    var endDate by remember { mutableStateOf(initialEnd) }
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit_dates),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                startDate = LocalDate.of(year, month + 1, day)
                            },
                            startDate.year,
                            startDate.monthValue - 1,
                            startDate.dayOfMonth
                        ).show()
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.start_label, startDate.format(formatter)))
                }
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                endDate = LocalDate.of(year, month + 1, day)
                            },
                            endDate.year,
                            endDate.monthValue - 1,
                            endDate.dayOfMonth
                        ).show()
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.end_label, endDate.format(formatter)))
                }
                if (!editable) {
                    Text(
                        text = stringResource(R.string.predicted_values_cannot_be_changed),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(startDate, endDate) },
                        enabled = editable,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
