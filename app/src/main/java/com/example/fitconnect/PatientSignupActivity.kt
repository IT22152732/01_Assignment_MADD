package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitconnect.models.Patient
import com.example.fitconnect.services.PatientService
import com.example.fitconnect.services.LocalPrefsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PatientSignupActivity : AppCompatActivity() {
    private lateinit var patientService: PatientService
    private lateinit var localPrefsService: LocalPrefsService

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        patientService = PatientService()
        localPrefsService = LocalPrefsService(this)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etPhone = findViewById(R.id.etPhone)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvSignIn = findViewById(R.id.tvSignIn)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            validateAndSignUp()
        }

        tvSignIn.setOnClickListener {
            startActivity(Intent(this, PatientLoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndSignUp() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Phone number is required"
            return
        }

        btnSignUp.isEnabled = false
        btnSignUp.text = "Signing up..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val patient = Patient(name, email, password, phone)
                val id = patientService.createPatient(patient)

                if (id.isNotEmpty()) {
                    localPrefsService.savePatientId(id)
                    runOnUiThread {
                        Toast.makeText(this@PatientSignupActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@PatientSignupActivity, PatientHomeActivity::class.java))
                        finish()
                    }
                } else {
                    throw Exception("Failed to create patient")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "Sign Up"
                    Toast.makeText(this@PatientSignupActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}