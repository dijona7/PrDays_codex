package com.algoritmika.prapp

import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun predictNextPeriodsMovingAverage(startDates: List<LocalDate>, windowSize: Int = 3): List<LocalDate> {
    require(startDates.size >= windowSize + 1) {
        "Нужно минимум ${windowSize + 1} дат для прогноза"
    }

    val intervals = startDates.zipWithNext { a, b ->
        ChronoUnit.DAYS.between(a, b).toDouble()
    }.toMutableList()

    fun movingAverage(lastIndex: Int): Double {
        val slice = intervals.subList(
            maxOf(0, lastIndex - windowSize + 1),
            lastIndex + 1
        )
        return slice.average()
    }

    val predictions = mutableListOf<LocalDate>()
    var lastDate = startDates.last()

    repeat(windowSize) {
        val avgInterval = movingAverage(intervals.lastIndex)
        lastDate = lastDate.plusDays(avgInterval.toLong())
        predictions.add(lastDate)
        intervals += avgInterval
    }

    return predictions
}

fun predictFutureRanges(
    pastRanges: List<ClosedRange<LocalDate>>,
    windowSize: Int = 3,
    periodDuration: Int = 4
): List<ClosedRange<LocalDate>> {
    val startDates = pastRanges.map { it.start }
    val predictedStarts = predictNextPeriodsMovingAverage(startDates, windowSize)
    return predictedStarts.map { startDate ->
        startDate..startDate.plusDays(periodDuration.toLong())
    }
}

