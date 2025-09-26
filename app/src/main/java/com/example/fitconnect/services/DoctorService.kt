package com.example.fitconnect.services

import com.example.fitconnect.models.Doctor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class DoctorService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "doctors"

    suspend fun createDoctor(doctor: Doctor): String {
        val docRef = db.collection(collection).document()
        docRef.set(doctor, SetOptions.merge()).await()
        return docRef.id
    }

    suspend fun getDoctorById(id: String): Doctor? {
        return db.collection(collection).document(id).get().await().toObject(Doctor::class.java)
    }

    suspend fun updateDoctor(id: String, doctor: Doctor): Boolean {
        return try {
            db.collection(collection).document(id).set(doctor, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteDoctor(id: String): Boolean {
        return try {
            db.collection(collection).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllDoctors(): List<Doctor> {
        return db.collection(collection).get().await().toObjects(Doctor::class.java)
    }

    suspend fun loginDoctor(email: String, password: String): Doctor? {
        return try {
            val query = db.collection(collection)
                .whereEqualTo("email", email)
                .whereEqualTo("password", password) // ⚠️ plain text check
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                val doc = query.documents.first()
                val doctor = doc.toObject(Doctor::class.java)
                doctor?.apply { snapshotId = doc.id }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}