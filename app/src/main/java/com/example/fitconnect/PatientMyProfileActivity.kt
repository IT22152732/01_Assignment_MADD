package com.example.fitconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitconnect.models.Patient
import com.example.fitconnect.services.CloudinaryService
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.PatientService
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PatientMyProfileActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivProfilePicture: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAge: EditText
    private lateinit var etGender: EditText
    private lateinit var btnEditProfile: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var localPrefsService: LocalPrefsService
    private lateinit var patientService: PatientService
    private lateinit var cloudinaryService: CloudinaryService

    private var currentPatient: Patient? = null
    private var currentPatientId: String? = null
    private var isEditing = false

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_my_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initImagePicker()
        initServices()
        initViews()
        setupClickListeners()
        loadPatientData()
    }

    private fun initImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                imageUri?.let { uploadProfileImage(it) }
            }
        }
    }

    private fun initServices() {
        localPrefsService = LocalPrefsService(this)
        patientService = PatientService()
        cloudinaryService = CloudinaryService()
    }

    private fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvName = findViewById(R.id.tvName)
        tvPhone = findViewById(R.id.tvPhone)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etAge = findViewById(R.id.etAge)
        etGender = findViewById(R.id.etGender)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        progressBar = findViewById(R.id.progressBar)

        setEditingEnabled(false)
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        ivProfilePicture.setOnClickListener {
            if (isEditing) {
                showImagePicker()
            }
        }

        btnEditProfile.setOnClickListener {
            if (isEditing) {
                saveProfile()
            } else {
                enableEditing()
            }
        }
    }

    private fun loadPatientData() {
        currentPatientId = localPrefsService.getPatientId()
        if (currentPatientId.isNullOrEmpty()) {
            finish()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                currentPatient = patientService.getPatientById(currentPatientId!!)
                currentPatient?.let { patient ->
                    updateUI(patient)
                } ?: run {
                    Toast.makeText(this@PatientMyProfileActivity, "Patient data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PatientMyProfileActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateUI(patient: Patient) {
        tvName.text = patient.name
        tvPhone.text = patient.phoneNumber
        etName.setText(patient.name)
        etPhone.setText(patient.phoneNumber)
        etAge.setText(patient.age)
        etGender.setText(patient.gender)

        Glide.with(this)
            .load(patient.profileImageUrl)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .into(ivProfilePicture)
    }

    private fun enableEditing() {
        isEditing = true
        setEditingEnabled(true)
        btnEditProfile.text = "Save Changes"
    }

    private fun setEditingEnabled(enabled: Boolean) {
        etName.isEnabled = enabled
        etPhone.isEnabled = enabled
        etAge.isEnabled = enabled
        etGender.isEnabled = enabled

        val alpha = if (enabled) 1.0f else 0.7f
        etName.alpha = alpha
        etPhone.alpha = alpha
        etAge.alpha = alpha
        etGender.alpha = alpha
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val age = etAge.text.toString().trim()
        val gender = etGender.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                val updatedPatient = currentPatient!!.copy(
                    name = name,
                    phoneNumber = phone,
                    age = age,
                    gender = gender
                )

                val success = patientService.updatePatient(currentPatientId!!, updatedPatient)

                if (success) {
                    currentPatient = updatedPatient
                    updateUI(updatedPatient)
                    isEditing = false
                    setEditingEnabled(false)
                    btnEditProfile.text = "Edit Profile"
                    Toast.makeText(this@PatientMyProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PatientMyProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PatientMyProfileActivity, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("profile_image", ".jpg", cacheDir)

                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val imageUrl = cloudinaryService.uploadImage(tempFile)

                if (imageUrl != null && currentPatient != null && currentPatientId != null) {
                    val updatedPatient = currentPatient!!.copy(profileImageUrl = imageUrl)
                    val success = patientService.updatePatient(currentPatientId!!, updatedPatient)

                    if (success) {
                        currentPatient = updatedPatient
                        Glide.with(this@PatientMyProfileActivity)
                            .load(imageUrl)
                            .placeholder(R.drawable.profile_placeholder)
                            .error(R.drawable.profile_placeholder)
                            .into(ivProfilePicture)
                        Toast.makeText(this@PatientMyProfileActivity, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PatientMyProfileActivity, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PatientMyProfileActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }

                tempFile.delete()

            } catch (e: Exception) {
                Toast.makeText(this@PatientMyProfileActivity, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnEditProfile.isEnabled = !show
    }
}