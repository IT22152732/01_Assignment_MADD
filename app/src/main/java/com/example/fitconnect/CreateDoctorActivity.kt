package com.example.fitconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitconnect.databinding.ActivityCreateDoctorBinding
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.CloudinaryService
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.utils.CategoryNames
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CreateDoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateDoctorBinding
    private val doctorService = DoctorService()
    private val cloudinaryService = CloudinaryService()
    private val categoryNames = CategoryNames()
    private var selectedImageUri: Uri? = null
    private var selectedCategory = ""

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivDoctorImage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupCategorySpinner()
        setupClickListeners()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames.categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categoryNames.categoryNames[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.ivDoctorImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.btnCreateDoctor.setOnClickListener {
            if (validateInputs()) {
                createDoctor()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val place = binding.etPlace.text.toString().trim()
        val workStartTime = binding.etWorkStartTime.text.toString().trim()
        val workEndTime = binding.etWorkEndTime.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return false
        }

        if (place.isEmpty()) {
            binding.etPlace.error = "Place is required"
            return false
        }

        if (workStartTime.isEmpty()) {
            binding.etWorkStartTime.error = "Work start time is required"
            return false
        }

        if (workEndTime.isEmpty()) {
            binding.etWorkEndTime.error = "Work end time is required"
            return false
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
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

    private fun createDoctor() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                var imageUrl = ""

                selectedImageUri?.let { uri ->
                    val file = createFileFromUri(uri)
                    file?.let {
                        val uploadedUrl = cloudinaryService.uploadImage(it)
                        if (uploadedUrl != null) {
                            imageUrl = uploadedUrl
                        } else {
                            showLoading(false)
                            Toast.makeText(this@CreateDoctorActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }
                }

                val doctor = Doctor(
                    name = binding.etName.text.toString().trim(),
                    snapshotId = "",
                    imageUrl = imageUrl,
                    categoryName = selectedCategory,
                    place = binding.etPlace.text.toString().trim(),
                    workStartTime = binding.etWorkStartTime.text.toString().trim(),
                    workEndTime = binding.etWorkEndTime.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    password = binding.etPassword.text.toString().trim()
                )

                val doctorId = doctorService.createDoctor(doctor)
                val updatedDoctor = doctor.copy(snapshotId = doctorId)
                doctorService.updateDoctor(doctorId, updatedDoctor)

                showLoading(false)
                Toast.makeText(this@CreateDoctorActivity, "Doctor created successfully", Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@CreateDoctorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateDoctor.isEnabled = !show
    }
}