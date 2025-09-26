package com.example.fitconnect.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitconnect.PatientLoginActivity
import com.example.fitconnect.PatientMyProfileActivity
import com.example.fitconnect.PatientNotificationsActivity
import com.example.fitconnect.R
import com.example.fitconnect.models.Patient
import com.example.fitconnect.services.CloudinaryService
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.PatientService
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PatientProfileFragment : Fragment() {

    private lateinit var layoutMyProfile: LinearLayout
    private lateinit var layoutNotifications: LinearLayout
    private lateinit var layoutLogout: LinearLayout
    private lateinit var layoutEditProfile: LinearLayout
    private lateinit var ivProfilePicture: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView

    private lateinit var localPrefsService: LocalPrefsService
    private lateinit var patientService: PatientService
    private lateinit var cloudinaryService: CloudinaryService

    private var currentPatient: Patient? = null
    private var currentPatientId: String? = null

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                imageUri?.let { uploadProfileImage(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initServices()
        initViews(view)
        setupClickListeners()
        loadPatientData()
    }

    private fun initServices() {
        localPrefsService = LocalPrefsService(requireContext())
        patientService = PatientService()
        cloudinaryService = CloudinaryService()
    }

    private fun initViews(view: View) {
        layoutMyProfile = view.findViewById(R.id.layoutMyProfile)
        layoutNotifications = view.findViewById(R.id.layoutNotifications)
        layoutLogout = view.findViewById(R.id.layoutLogout)
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvName = view.findViewById(R.id.tvName)
        tvPhone = view.findViewById(R.id.tvPhone)
    }

    private fun setupClickListeners() {
        layoutMyProfile.setOnClickListener {
            val intent = Intent(requireContext(), PatientMyProfileActivity::class.java)
            startActivity(intent)
        }
        layoutNotifications.setOnClickListener {
            val intent = Intent(requireContext(), PatientNotificationsActivity::class.java)
            startActivity(intent)
        }

        layoutEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), PatientMyProfileActivity::class.java)
            startActivity(intent)
        }

        ivProfilePicture.setOnClickListener {
            showImagePickerDialog()
        }

        layoutLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadPatientData() {
        currentPatientId = localPrefsService.getPatientId()
        if (currentPatientId.isNullOrEmpty()) {
            performLogout()
            return
        }

        lifecycleScope.launch {
            try {
                currentPatient = patientService.getPatientById(currentPatientId!!)
                currentPatient?.let { patient ->
                    updateUI(patient)
                } ?: run {
                    Toast.makeText(context, "Patient data not found", Toast.LENGTH_SHORT).show()
                    performLogout()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(patient: Patient) {
        tvName.text = patient.name
        tvPhone.text = patient.phoneNumber

        Glide.with(this)
            .load(patient.profileImageUrl)
            .placeholder(R.drawable.profile_placeholder)
            .error(R.drawable.profile_placeholder)
            .into(ivProfilePicture)
    }

    private fun showImagePickerDialog() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val tempFile = File.createTempFile("profile_image", ".jpg", requireContext().cacheDir)

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
                        updateUI(updatedPatient)
                        Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }

                tempFile.delete()

            } catch (e: Exception) {
                Toast.makeText(context, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnLogout = dialog.findViewById<Button>(R.id.btnLogout)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        localPrefsService.logout()
        val intent = Intent(activity, PatientLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }
}