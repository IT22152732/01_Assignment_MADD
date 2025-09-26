package com.example.fitconnect.models

data class Patient(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png",
    val age: String = "",
    val gender: String = ""
) {
    constructor() : this("","", "", "", "", "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_640.png", "", "")
}