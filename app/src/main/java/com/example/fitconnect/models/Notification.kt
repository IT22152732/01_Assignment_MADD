package com.example.fitconnect.models

data class Notification(
    val id: String = "",
    val patientId: String = "",
    val snapshotId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val createdAt: String = ""
) {
    constructor() : this("", "", "", "", "", "")
}