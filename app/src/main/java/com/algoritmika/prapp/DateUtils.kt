package com.algoritmika.prapp

import java.time.LocalDate
import java.time.YearMonth

/**
 * Group ranges by month and calculate the gap between the start of each range
 * and the previous one in the global list.
 */
fun groupRangesByMonthWithGlobalGaps(
    ranges: List<ClosedRange<LocalDate>>
): Map<YearMonth, List<Pair<ClosedRange<LocalDate>, Long>>> {
    val sorted = ranges.sortedBy { it.start } // overall list ascending
    val gapsMap = mutableMapOf<ClosedRange<LocalDate>, Long>()

    // calculate gaps across all months
    sorted.forEachIndexed { index, range ->
        if (index == 0) {
            gapsMap[range] = 0L // first item has no gap
        } else {
            val prevStart = sorted[index - 1].start
            val gap = java.time.temporal.ChronoUnit.DAYS.between(prevStart, range.start)
            gapsMap[range] = gap
        }
    }

    // group by start month and attach gaps
    return sorted
        .groupBy { YearMonth.from(it.start) }
        .mapValues { (_, monthRanges) ->
            monthRanges.map { range -> range to (gapsMap[range] ?: 0L) }
        }
        .toSortedMap(compareByDescending { it })
}
