package com.opendialer.app.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    fun formatDuration(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
        }
    }

    fun formatCallTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(24) -> {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                SimpleDateFormat("EEE, hh:mm a", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    fun formatDateOnly(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
