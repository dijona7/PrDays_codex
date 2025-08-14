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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun FileEditorScreen(context: Context,
    onSave: (List<ClosedRange<LocalDate>>) -> Unit) {
    val file = File(context.filesDir, "date_ranges.txt")
    var text by remember { mutableStateOf(file.readText()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Редактирование date_ranges.txt", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()) // ← прокрутка
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxSize(),
                singleLine = false
            )
        }

        error?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val isValid = text.lines().all { line ->
                    val regex = Regex("""\d{2}\.\d{2}\.\d{4},\d{2}\.\d{2}\.\d{4}""")
                    line.isBlank() || regex.matches(line.trim())
                }

                if (isValid) {
                    file.writeText(text.trim())
                    error = null

                    // Парсим строки в список диапазонов

                    val newRanges = text.lines()
                        .filter { it.isNotBlank() }
                        .map { line ->
                            val (startStr, endStr) = line.split(",")
                            val start = LocalDate.parse(startStr.trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            val end = LocalDate.parse(endStr.trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            start..end
                        }

                    onSave(newRanges)

                } else {
                    error = "Файл содержит строки в неправильном формате!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}