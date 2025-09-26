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
import com.example.fitconnect.databinding.ActivityEditDoctorBinding
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.CloudinaryService
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.utils.CategoryNames
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditDoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditDoctorBinding
    private val doctorService = DoctorService()
    private val cloudinaryService = CloudinaryService()
    private val categoryNames = CategoryNames()
    private var selectedImageUri: Uri? = null
    private var selectedCategory = ""
    private var doctorId = ""
    private var currentDoctor: Doctor? = null

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
        binding = ActivityEditDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        doctorId = intent.getStringExtra("doctorId") ?: ""

        setupCategorySpinner()
        setupClickListeners()
        loadDoctorData()
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

        binding.btnUpdateDoctor.setOnClickListener {
            if (validateInputs()) {
                updateDoctor()
            }
        }
    }

    private fun loadDoctorData() {
        if (doctorId.isEmpty()) {
            Toast.makeText(this, "Invalid doctor ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                currentDoctor = doctorService.getDoctorById(doctorId)
                currentDoctor?.let { doctor ->
                    populateFields(doctor)
                    showLoading(false)
                } ?: run {
                    showLoading(false)
                    Toast.makeText(this@EditDoctorActivity, "Doctor not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@EditDoctorActivity, "Error loading doctor: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun populateFields(doctor: Doctor) {
        binding.etName.setText(doctor.name)
        binding.etPlace.setText(doctor.place)
        binding.etWorkStartTime.setText(doctor.workStartTime)
        binding.etWorkEndTime.setText(doctor.workEndTime)
        binding.etExperience.setText(doctor.experience)
        binding.etPatients.setText(doctor.patients)
        binding.doctorNameText.text = doctor.name

        Glide.with(this)
            .load(doctor.imageUrl)
            .into(binding.ivDoctorImage)

        val categoryIndex = categoryNames.categoryNames.indexOf(doctor.categoryName)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
            selectedCategory = doctor.categoryName
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val place = binding.etPlace.text.toString().trim()
        val workStartTime = binding.etWorkStartTime.text.toString().trim()
        val workEndTime = binding.etWorkEndTime.text.toString().trim()
        val experience = binding.etExperience.text.toString().trim()
        val patients = binding.etPatients.text.toString().trim()

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

        if (experience.isEmpty()) {
            binding.etExperience.error = "Experience is required"
            return false
        }

        if (patients.isEmpty()) {
            binding.etPatients.error = "Patients is required"
            return false
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateDoctor() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                var imageUrl = currentDoctor?.imageUrl ?: ""

                selectedImageUri?.let { uri ->
                    val file = createFileFromUri(uri)
                    file?.let {
                        val uploadedUrl = cloudinaryService.uploadImage(it)
                        if (uploadedUrl != null) {
                            imageUrl = uploadedUrl
                        } else {
                            showLoading(false)
                            Toast.makeText(this@EditDoctorActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }
                }

                val updatedDoctor = Doctor(
                    name = binding.etName.text.toString().trim(),
                    snapshotId = doctorId,
                    imageUrl = imageUrl,
                    categoryName = selectedCategory,
                    place = binding.etPlace.text.toString().trim(),
                    workStartTime = binding.etWorkStartTime.text.toString().trim(),
                    workEndTime = binding.etWorkEndTime.text.toString().trim(),
                    experience = binding.etExperience.text.toString().trim(),
                    patients = binding.etPatients.text.toString().trim()
                )

                val success = doctorService.updateDoctor(doctorId, updatedDoctor)
                showLoading(false)

                if (success) {
                    Toast.makeText(this@EditDoctorActivity, "Doctor updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditDoctorActivity, "Failed to update doctor", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@EditDoctorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        binding.btnUpdateDoctor.isEnabled = !show
    }
}