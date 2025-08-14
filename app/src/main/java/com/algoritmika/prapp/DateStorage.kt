package com.algoritmika.prapp

import android.content.Context
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Load date ranges from internal storage. If the file does not exist it is copied from assets.
 */
fun loadDateRanges(context: Context): List<ClosedRange<LocalDate>> {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val internalFile = File(context.filesDir, "date_ranges.txt")

    // Copy default file from assets on first run
    if (!internalFile.exists()) {
        context.assets.open("date_ranges.txt").use { input ->
            internalFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    val today = LocalDate.now()
    // Read file and filter out future ranges
    return internalFile.bufferedReader().useLines { lines ->
        lines.mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                val start = LocalDate.parse(parts[0].trim(), formatter)
                val end = LocalDate.parse(parts[1].trim(), formatter)
                start..end
            } else null
        }
            .filter { it.start.isBefore(today) || it.start.isEqual(today) }
            .toList()
    }
}

/**
 * Update stored date ranges with prediction and save the merged result.
 */
fun updateDateRangesWithPrediction(context: Context): List<ClosedRange<LocalDate>> {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val internalFile = File(context.filesDir, "date_ranges.txt")

    // Load existing history
    val history = loadDateRanges(context)

    // Build prediction based on settings
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val windowSize = prefs.getInt("windowSize", 3)
    val duration = prefs.getInt("standardDuration", 4)
    val prediction = predictFutureRanges(history, windowSize = windowSize, periodDuration = duration)

    // Merge history with prediction
    val merged = history + prediction

    // Persist merged list
    internalFile.bufferedWriter().use { writer ->
        merged.forEach { range ->
            writer.write("${range.start.format(formatter)}, ${range.endInclusive.format(formatter)}\n")
        }
    }

    return merged
}

/** Save all ranges back to storage. */
fun saveDateRanges(context: Context, ranges: List<ClosedRange<LocalDate>>) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val file = File(context.filesDir, "date_ranges.txt")
    file.printWriter().use { out ->
        ranges.forEach { range ->
            out.println("${range.start.format(formatter)},${range.endInclusive.format(formatter)}")
        }
    }
}

