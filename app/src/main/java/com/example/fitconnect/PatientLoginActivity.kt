package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitconnect.databinding.ActivityPatientLoginBinding
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.PatientService
import kotlinx.coroutines.launch

class PatientLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientLoginBinding
    private val patientService = PatientService()
    private lateinit var localPrefsService: LocalPrefsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPatientLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localPrefsService = LocalPrefsService(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, PatientSignupActivity::class.java))
        }

        binding.tvAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }

        binding.tvDoctorLogin.setOnClickListener {
            startActivity(Intent(this, DoctorLoginActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            return false
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        showLoading(true)

        lifecycleScope.launch {
            try {
                val patient = patientService.loginPatient(email, password)

                showLoading(false)

                if (patient != null) {
                    localPrefsService.savePatientId(patient.id)
                    localPrefsService.saveToken("patient_token_${patient.id}")

                    Toast.makeText(this@PatientLoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@PatientLoginActivity, PatientHomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@PatientLoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@PatientLoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.btnSignIn.visibility = View.INVISIBLE
            // Add progress bar to your layout if needed
        } else {
            binding.btnSignIn.visibility = View.VISIBLE
        }
        binding.btnSignIn.isEnabled = !show
    }
}