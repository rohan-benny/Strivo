package com.example.strivo.dataclass

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*

@IgnoreExtraProperties
data class CalendarEvent(
    val category: String? = null,    // e.g., "holiday", "general", "exam"
    val description: String? = null, // e.g., "Vacation"
    val displayId: String? = null,   // e.g., "H1"
    val endDate: String? = null,     // Date in "YYYY-MM-DD" format, e.g., "2025-07-23"
    val idNumber: Int? = null,       // e.g., 1
    val idPrefix: String? = null,    // e.g., "H"
    val name: String? = null,        // The title of the event, e.g., "Sem Break"
    val startDate: String? = null    // Date in "YYYY-MM-DD" format, e.g., "2025-05-22"
) {
    // Firebase needs a no-argument constructor to deserialize data snapshots into this class.
    constructor() : this(null, null, null, null, null, null, null, null)

    val type: String?
        get() = category


    val title: String?
        get() = name


    val displayDate: String?
        get() {
            // Extract the day part (last two digits) from "YYYY-MM-DD"
            val startDay = startDate?.split("-")?.getOrNull(2)
            val endDay = endDate?.split("-")?.getOrNull(2)

            // If both start and end days exist and are different, show a date range
            return if (startDay != null && endDay != null && startDay != endDay) {
                "$startDay-$endDay"
            } else {
                startDay // Otherwise, just show the start day
            }
        }


    val month: String?
        get() {
            val dateString = startDate ?: return null // If startDate is null, month cannot be determined
            return try {
                // Parse the "YYYY-MM-DD" string into a Java Date object
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                // Format the Date object into a full month name (e.g., "January", "February")
                date?.let { SimpleDateFormat("MMMM", Locale.getDefault()).format(it) }
            } catch (e: Exception) {
                // Log the exception for debugging if date parsing fails
                // Log.e("CalendarEvent", "Error parsing month from startDate: $dateString", e)
                null
            }
        }


    val year: String?
        get() {
            val dateString = startDate ?: return null // If startDate is null, year cannot be determined
            return try {
                // Parse the "YYYY-MM-DD" string into a Java Date object
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
                // Format the Date object into a four-digit year (e.g., "2025")
                date?.let { SimpleDateFormat("yyyy", Locale.getDefault()).format(it) }
            } catch (e: Exception) {
                // Log the exception for debugging if date parsing fails
                // Log.e("CalendarEvent", "Error parsing year from startDate: $dateString", e)
                null
            }
        }
}