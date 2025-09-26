package com.example.fitconnect.models

data class Doctor(
    val name: String = "",
    var snapshotId: String = "",
    val imageUrl: String = "",
    val categoryName: String = "",
    val place: String = "",
    val workStartTime: String = "",
    val workEndTime: String = "",
    val experience: String = "",
    val patients: String = "",
    val email:String = "",
    val password:String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "","","")
}
