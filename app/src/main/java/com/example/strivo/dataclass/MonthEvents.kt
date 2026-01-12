package com.example.strivo.dataclass

data class MonthEvents(
    val monthName: String,               // The full name of the month, e.g., "January", "February"
    val events: List<CalendarEvent>      // A list of CalendarEvent objects that fall within this month
)