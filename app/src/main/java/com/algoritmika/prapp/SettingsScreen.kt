package com.algoritmika.prapp

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@Composable
fun SettingsScreen(onApply: (List<ClosedRange<LocalDate>>) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var windowSize by remember { mutableStateOf(prefs.getInt("windowSize", 3)) }
    var duration by remember { mutableStateOf(prefs.getInt("standardDuration", 4)) }
    var language by remember { mutableStateOf(prefs.getString("language", "en") ?: "en") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingDropdown(
            label = stringResource(R.string.prediction_window),
            options = (1..5).toList(),
            selected = windowSize,
            onSelected = {
                windowSize = it
                prefs.edit().putInt("windowSize", it).apply()
            }
        )
        SettingDropdown(
            label = stringResource(R.string.standard_duration),
            options = (3..6).toList(),
            selected = duration,
            onSelected = {
                duration = it
                prefs.edit().putInt("standardDuration", it).apply()
            }
        )
        LanguageDropdown(
            label = stringResource(R.string.language),
            options = mapOf(
                stringResource(R.string.lang_english) to "en",
                stringResource(R.string.lang_russian) to "ru",
                stringResource(R.string.lang_ukrainian) to "uk",
                stringResource(R.string.lang_norwegian) to "nb"
            ),
            selected = language,
            onSelected = {
                language = it
                prefs.edit().putString("language", it).apply()
                setLocale(context, it)
                (context as? Activity)?.recreate()
            }
        )
        Button(
            onClick = {
                val updated = updateDateRangesWithPrediction(context)
                onApply(updated)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply_settings))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDropdown(
    label: String,
    options: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(
    label: String,
    options: Map<String, String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.entries.firstOrNull { it.value == selected }?.key ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (labelText, code) ->
                DropdownMenuItem(
                    text = { Text(labelText) },
                    onClick = {
                        onSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
