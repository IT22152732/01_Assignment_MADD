package com.example.fitconnect.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitconnect.R
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.CloudinaryService
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.services.LocalPrefsService
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DoctorProfileFragment : Fragment() {

    private val doctorService = DoctorService()
    private val cloudinaryService = CloudinaryService()
    private lateinit var localPrefs: LocalPrefsService
    private var currentDoctor: Doctor? = null

    private lateinit var ivProfile: ImageView
    private lateinit var btnEditImage: androidx.appcompat.widget.AppCompatImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: View
    private lateinit var tvName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var etName: EditText
    private lateinit var etSpecialty: EditText
    private lateinit var etPlace: EditText
    private lateinit var etPatients: EditText
    private lateinit var etExperience: EditText

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onImagePicked(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_doctor_profile, container, false)
        // Initialize views
        ivProfile = view.findViewById(R.id.ivProfile)
        btnEditImage = view.findViewById(R.id.btnEditImage)
        progressBar = view.findViewById(R.id.progressBar)
        contentLayout = view.findViewById(R.id.contentLayout)
        tvName = view.findViewById(R.id.tvName)
        tvSubtitle = view.findViewById(R.id.tvSubtitle)
        etName = view.findViewById(R.id.etName)
        etSpecialty = view.findViewById(R.id.etSpecialty)
        etPlace = view.findViewById(R.id.etPlace)
        etPatients = view.findViewById(R.id.etPatients)
        etExperience = view.findViewById(R.id.etExperience)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        localPrefs = LocalPrefsService(requireContext())
        setupUi()
        loadDoctorProfile()
    }

    private fun setupUi() {
        etName.isFocusable = false
        etSpecialty.isFocusable = false
        etPlace.isFocusable = false
        etPatients.isFocusable = false
        etExperience.isFocusable = false

        ivProfile.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnEditImage.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun loadDoctorProfile() {
        val doctorId = localPrefs.getDoctorId()
        if (doctorId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No doctor logged in", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                val doctor = doctorService.getDoctorById(doctorId)
                if (doctor != null) {
                    doctor.snapshotId = doctorId
                    currentDoctor = doctor
                    populateFields(doctor)
                } else {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateFields(doctor: Doctor) {
        if (doctor.imageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(doctor.imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .into(ivProfile)
        } else {
            ivProfile.setImageResource(R.drawable.ic_profile)
        }

        tvName.text = if (doctor.name.isNotEmpty()) doctor.name else "Doctor"
        tvSubtitle.text = if (doctor.email.isNotEmpty()) doctor.email else ""

        etName.setText(doctor.name)
        etSpecialty.setText(doctor.categoryName)
        etPlace.setText(doctor.place)
        etPatients.setText(doctor.patients)
        etExperience.setText(doctor.experience)
    }

    private fun onImagePicked(uri: Uri) {
        Glide.with(this).load(uri).circleCrop().into(ivProfile)

        lifecycleScope.launch {
            showLoading(true)
            try {
                val file = createFileFromUri(uri)
                if (file == null) {
                    Toast.makeText(requireContext(), "Failed to read selected image", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    return@launch
                }

                val uploadedUrl = cloudinaryService.uploadImage(file)
                if (uploadedUrl.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    return@launch
                }

                val doctor = currentDoctor
                if (doctor == null) {
                    Toast.makeText(requireContext(), "No doctor to update", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    return@launch
                }

                val updatedDoctor = doctor.copy(imageUrl = uploadedUrl)
                val success = doctorService.updateDoctor(doctor.snapshotId, updatedDoctor)

                if (success) {
                    currentDoctor = updatedDoctor
                    populateFields(updatedDoctor)
                    Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        contentLayout.alpha = if (show) 0.6f else 1.0f
        btnEditImage.isEnabled = !show
        ivProfile.isEnabled = !show
    }
}
