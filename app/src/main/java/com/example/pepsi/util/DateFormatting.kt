package com.example.pepsi.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Matches "2026-09-12T14:18:16.574000" (no offset, microsecond fraction) and
// "2026-09-11T10:00:00Z" (millisecond fraction, trailing Z) alike.
private val isoDateTimePattern = Regex("""^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2})(?:\.(\d+))?Z?$""")

private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val readableFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())

/**
 * The API returns raw UTC timestamps with an inconsistent fractional-second length and
 * an optional trailing "Z". This renders them in the device's local time zone as something
 * like "Sep 12, 2026, 2:18 PM" instead of the database format. Falls back to the raw
 * string if it doesn't match the expected shape.
 */
fun formatApiDateTime(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val match = isoDateTimePattern.matchEntire(raw) ?: return raw
    val (datePart, timePart, fraction) = match.destructured
    val millis = fraction.take(3).padEnd(3, '0')
    val date = runCatching { isoParser.parse("${datePart}T$timePart.$millis") }.getOrNull()
        ?: return raw
    return readableFormat.format(date)
}
