package com.example.fitconnect.models

data class CalendarDay(
    val day: String,
    val date: String,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isSelectable: Boolean
)
