package com.cerdenia.android.fullcup.util

import android.content.res.Resources
import com.cerdenia.android.fullcup.R
import com.cerdenia.android.fullcup.WEEKDAY
import com.cerdenia.android.fullcup.WEEKEND

object DateTimeUtils {
    // Accepts time in HH:mm format.
    fun to12HourFormat(timeString: String): String {
        if (!timeString.contains(":") || timeString.length != 5) {
            throw IllegalArgumentException("Invalid string time $timeString, must be HH:mm.")
        }

        val hour = timeString.substringBefore(":").toInt()
        val minutes = timeString.substringAfter(":")

        return when (hour) {
            0 -> "12:$minutes AM"
            in 1..11 -> "$hour:$minutes AM"
            12 -> "12:$minutes PM"
            in 13..23 -> "${hour - 12}:$minutes PM"
            else -> throw IllegalStateException("Hour must not be more than 23.")
        }
    }

    // Accepts time in HH:mm format.
    fun getGreeting(resources: Resources, timeString: String): String {
        return when (timeString.substringBefore(":").toInt()) {
            in 0..11 -> R.string.good_morning
            12 -> R.string.good_afternoon
            in 13..17 -> R.string.good_afternoon
            in 18..23 -> R.string.good_evening
            else -> R.string.good_day
        }.run { resources.getString (this) }
    }

    // Accepts day name in EEE format. Returns whether day is a weekday or weekend.
    fun toTypeOfDay(dayString: String): String {
        val weekdays = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri")
        val weekends = arrayOf("Sat", "Sun")
        return when {
            weekdays.contains(dayString) -> WEEKDAY
            weekends.contains(dayString) -> WEEKEND
            else -> "ERROR"
        }
    }
}