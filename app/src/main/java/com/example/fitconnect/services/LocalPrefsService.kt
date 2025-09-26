package com.example.fitconnect.services

import android.content.Context
import android.content.SharedPreferences

class LocalPrefsService(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FitConnectPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    // Patient
    fun savePatientId(id: String) {
        editor.putString("patientId", id).apply()
    }
    fun getPatientId(): String? = prefs.getString("patientId", null)
    fun clearPatientId() { editor.remove("patientId").apply() }

    // Admin
    fun saveIsAdmin() { editor.putBoolean("isAdmin", true).apply() }
    fun logoutAdmin() { editor.putBoolean("isAdmin", false).apply() }
    fun getIsAdmin(): Boolean = prefs.getBoolean("isAdmin", false)

    // Doctor
    fun saveDoctorId(id: String) {
        editor.putString("doctorId", id).apply()
    }
    fun getDoctorId(): String? = prefs.getString("doctorId", null)
    fun clearDoctorId() { editor.remove("doctorId").apply() }

    fun saveIsDoctor() { editor.putBoolean("isDoctor", true).apply() }
    fun getIsDoctor(): Boolean = prefs.getBoolean("isDoctor", false)
    fun logoutDoctor() { editor.putBoolean("isDoctor", false).apply() }

    // Token
    fun saveToken(token: String) { editor.putString("authToken", token).apply() }
    fun getToken(): String? = prefs.getString("authToken", null)
    fun clearToken() { editor.remove("authToken").apply() }

    fun isLoggedIn(): Boolean {
        return (getPatientId() != null || getDoctorId() != null) && getToken() != null
    }

    fun logout() {
        clearPatientId()
        clearDoctorId()
        clearToken()
        logoutAdmin()
        logoutDoctor()
    }
}
