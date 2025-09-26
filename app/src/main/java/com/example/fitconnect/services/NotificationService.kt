package com.example.fitconnect.services

import com.example.fitconnect.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class NotificationService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "notifications"

    suspend fun createNotification(notification: Notification): String {
        val docRef = db.collection(collection).document()
        docRef.set(notification, SetOptions.merge()).await()
        return docRef.id
    }

    suspend fun getNotificationById(id: String): Notification? {
        return db.collection(collection).document(id).get().await().toObject(Notification::class.java)
    }

    suspend fun updateNotification(id: String, notification: Notification): Boolean {
        return try {
            db.collection(collection).document(id).set(notification, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteNotification(id: String): Boolean {
        return try {
            db.collection(collection).document(id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getNotificationsByPatientId(patientId: String): List<Notification> {
        return db.collection(collection)
            .whereEqualTo("patientId", patientId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }
    }
}