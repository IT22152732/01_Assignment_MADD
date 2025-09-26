package com.example.fitconnect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitconnect.adapters.CategoryAdminAdapter
import com.example.fitconnect.adapters.DoctorAdapter
import com.example.fitconnect.databinding.ActivityAdminDashboardBinding
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.utils.CategoryNames
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val doctorService = DoctorService()
    private val categoryNames = CategoryNames()
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var categoryAdapter: CategoryAdminAdapter
    private lateinit var localPrefsService: LocalPrefsService

    private var allDoctors = listOf<Doctor>()
    private var filteredDoctors = listOf<Doctor>()
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localPrefsService = LocalPrefsService(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Add Doctor button
        binding.btnAddDoctorMain.setOnClickListener {
            startActivity(Intent(this, CreateDoctorActivity::class.java))
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            localPrefsService.logoutAdmin()
            startActivity(Intent(this, PatientLoginActivity::class.java))
            finish()
        }

        setupRecyclerViews()
        setupSearch()
        setupSwipeRefresh()
        loadDoctors()
    }

    private fun setupRecyclerViews() {
        doctorAdapter = DoctorAdapter(
            onEditClick = { doctor ->
                startActivity(
                    Intent(this, EditDoctorActivity::class.java)
                        .putExtra("doctorId", doctor.snapshotId)
                )
            },
            onDeleteClick = { doctor ->
                showDeleteDialog(doctor)
            }
        )

        binding.rvDoctors.layoutManager = LinearLayoutManager(this)
        binding.rvDoctors.adapter = doctorAdapter

        val categories = listOf("All") + categoryNames.categoryNames
        categoryAdapter = CategoryAdminAdapter(categories) { category ->
            selectedCategory = category
            applyFilters()
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                applyFilters()
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadDoctors()
        }
    }

    private fun loadDoctors() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                allDoctors = doctorService.getAllDoctors()
                filteredDoctors = allDoctors
                doctorAdapter.updateDoctors(filteredDoctors)
                showLoading(false)
                showEmptyState(filteredDoctors.isEmpty())
            } catch (e: Exception) {
                showLoading(false)
                showError("Error loading doctors: ${e.message}")
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun applyFilters() {
        filteredDoctors = allDoctors.filter { doctor ->
            val matchesCategory =
                selectedCategory == "All" || doctor.categoryName == selectedCategory
            val matchesSearch = searchQuery.isEmpty() ||
                    doctor.name.contains(searchQuery, ignoreCase = true) ||
                    doctor.categoryName.contains(searchQuery, ignoreCase = true) ||
                    doctor.place.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        doctorAdapter.updateDoctors(filteredDoctors)
        showEmptyState(filteredDoctors.isEmpty())
    }

    private fun showDeleteDialog(doctor: Doctor) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete?")
            .setPositiveButton("Yes, Delete") { _, _ ->
                deleteDoctor(doctor)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDoctor(doctor: Doctor) {
        lifecycleScope.launch {
            try {
                val success = doctorService.deleteDoctor(doctor.snapshotId)
                if (success) {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Doctor deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadDoctors()
                } else {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Failed to delete doctor",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.rvDoctors.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun showEmptyState(show: Boolean) {
        binding.tvEmptyState.visibility =
            if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
