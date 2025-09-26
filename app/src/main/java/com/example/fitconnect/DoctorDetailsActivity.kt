package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.DoctorService
import kotlinx.coroutines.launch
import android.widget.ImageView
import android.widget.TextView

class DoctorDetailsActivity : AppCompatActivity() {
    private lateinit var doctorService: DoctorService
    private var doctorId: String? = null
    private var doctorName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_details)

        doctorService = DoctorService()
        doctorId = intent.getStringExtra("doctorId")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        loadDoctorDetails()
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<AppCompatButton>(R.id.bookAppointmentButton).setOnClickListener {
            val intent = Intent(this, CreateBookingActivity::class.java).apply {
                putExtra("doctorId", doctorId)
                putExtra("doctorName", doctorName)
            }
            startActivity(intent)
        }
    }

    private fun loadDoctorDetails() {
        doctorId?.let { id ->
            lifecycleScope.launch {
                try {
                    val doctor = doctorService.getDoctorById(id)
                    doctor?.let {
                        displayDoctorInfo(it)
                        doctorName = it.name
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayDoctorInfo(doctor: Doctor) {
        Glide.with(this)
            .load(doctor.imageUrl)
            .into(findViewById<ImageView>(R.id.doctorImage))

        findViewById<TextView>(R.id.doctorName).text = doctor.name
        findViewById<TextView>(R.id.doctorSpecialty).text = doctor.categoryName
        findViewById<TextView>(R.id.doctorPlace).text = doctor.place
        findViewById<TextView>(R.id.patientsCount).text = doctor.patients
        findViewById<TextView>(R.id.experienceCount).text = doctor.experience
        findViewById<TextView>(R.id.aboutText).text = "Dr. ${doctor.name}, a dedicated ${doctor.categoryName.lowercase()}, brings a wealth of experience to ${doctor.place}."
        findViewById<TextView>(R.id.workingTimeText).text = "Monday-Friday, ${doctor.workStartTime}-${doctor.workEndTime}"
    }
}