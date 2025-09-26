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
import com.example.fitconnect.databinding.ActivityDoctorLoginBinding
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.services.LocalPrefsService
import kotlinx.coroutines.launch

class DoctorLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorLoginBinding
    private val doctorService = DoctorService()
    private lateinit var localPrefs: LocalPrefsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDoctorLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localPrefs = LocalPrefsService(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLogin()
    }

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                val doctor = doctorService.loginDoctor(email, password)
                binding.progressBar.visibility = View.GONE

                if (doctor != null) {
                    // Save doctor session
                    localPrefs.saveDoctorId(doctor.snapshotId)
                    localPrefs.saveIsDoctor()

                    Toast.makeText(this@DoctorLoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                    // Navigate to DoctorDashboardActivity
                    startActivity(Intent(this@DoctorLoginActivity, DoctorDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@DoctorLoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
