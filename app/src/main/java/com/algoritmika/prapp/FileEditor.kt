package com.algoritmika.prapp

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FileEditorScreen(
    context: Context,
    onSave: (List<ClosedRange<LocalDate>>) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val history = loadDateRanges(context)
    var text by remember {
        mutableStateOf(
            history.joinToString("\n") {
                "${it.start.format(formatter)},${it.endInclusive.format(formatter)}"
            }
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Edit date_ranges.txt",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxSize(),
                singleLine = false
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val isValid = text.lines().all { line ->
                    val regex = Regex("""\d{2}\.\d{2}\.\d{4},\d{2}\.\d{2}\.\d{4}""")
                    line.isBlank() || regex.matches(line.trim())
                }

                if (isValid) {
                    val newRanges = text.lines()
                        .filter { it.isNotBlank() }
                        .map { line ->
                            val (startStr, endStr) = line.split(",")
                            val start = LocalDate.parse(startStr.trim(), formatter)
                            val end = LocalDate.parse(endStr.trim(), formatter)
                            start..end
                        }
                    saveDateRanges(context, newRanges)
                    val updated = updateDateRangesWithPrediction(context)
                    onSave(updated)
                    text = loadDateRanges(context).joinToString("\n") {
                        "${it.start.format(formatter)},${it.endInclusive.format(formatter)}"
                    }
                    error = null
                } else {
                    error = "File contains lines in wrong format!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

