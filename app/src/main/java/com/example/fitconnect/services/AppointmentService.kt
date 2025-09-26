package com.example.fitconnect.services

import com.example.fitconnect.models.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class AppointmentService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "appointments"

    suspend fun createAppointment(appointment: Appointment): String {
        val docRef = db.collection(collection).document()
        docRef.set(appointment, SetOptions.merge()).await()
        return docRef.id
    }

    suspend fun getAppointmentById(id: String): Appointment? {
        return db.collection(collection).document(id).get().await().toObject(Appointment::class.java)
    }

    suspend fun updateAppointment(id: String, appointment: Appointment): Boolean {
        return try {
            db.collection(collection).document(id).set(appointment, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAppointment(id: String): Boolean {
        return try {
            db.collection(collection).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAppointmentsByPatientId(patientId: String): List<Appointment> {
        return db.collection(collection)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun getAppointmentsByDoctorId(doctorId: String): List<Appointment> {
        return db.collection(collection)
            .whereEqualTo("doctorId", doctorId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
    }
}