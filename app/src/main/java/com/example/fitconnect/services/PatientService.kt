package com.example.fitconnect.services

import com.example.fitconnect.models.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class PatientService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "patients"

    suspend fun createPatient(patient: Patient): String {
        val docRef = db.collection(collection).document()
        docRef.set(patient, SetOptions.merge()).await()
        return docRef.id
    }

    suspend fun getPatientById(id: String): Patient? {
        return db.collection(collection).document(id).get().await().toObject(Patient::class.java)
    }

    suspend fun updatePatient(id: String, patient: Patient): Boolean {
        return try {
            db.collection(collection).document(id).set(patient, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletePatient(id: String): Boolean {
        return try {
            db.collection(collection).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginPatient(email: String, password: String): Patient? {
        val document = db.collection(collection)
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .await()
            .documents
            .firstOrNull()

        return document?.let { doc ->
            doc.toObject(Patient::class.java)?.copy(id = doc.id)
        }
    }
}