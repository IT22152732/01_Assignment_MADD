package com.example.fitconnect

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.adapters.NotificationAdapter
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.NotificationService
import kotlinx.coroutines.launch

class PatientNotificationsActivity : AppCompatActivity() {
    private lateinit var notificationService: NotificationService
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var localPrefsService: LocalPrefsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_notifications)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadNotifications()
    }

    private fun initViews() {
        localPrefsService = LocalPrefsService(this)
        notificationService = NotificationService()
        sharedPreferences = getSharedPreferences("FitConnect", MODE_PRIVATE)
        recyclerView = findViewById(R.id.notificationsRecyclerView)

        findViewById<android.widget.ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notificationAdapter
    }

    private fun loadNotifications() {
        val patientId = localPrefsService.getPatientId()

        if (patientId.isNullOrEmpty()) {
            Toast.makeText(this, "Patient ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val notifications = notificationService.getNotificationsByPatientId(patientId)
                notificationAdapter.updateNotifications(notifications.sortedByDescending { it.createdAt })
            } catch (e: Exception) {
                Toast.makeText(this@PatientNotificationsActivity, "Error loading notifications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}