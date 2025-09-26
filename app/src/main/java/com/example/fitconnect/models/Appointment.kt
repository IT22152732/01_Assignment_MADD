package com.example.fitconnect.models

data class Appointment(
    val id: String = "",
    val status: String = "",
    val date: String = "",
    val hour: String = "",
    val doctorId: String = "",
    val patientId: String = "",
    val doctorName: String = "",
    val patientName: String = ""
) {
    constructor() : this("", "Pending", "", "", "", "", "", "")
}