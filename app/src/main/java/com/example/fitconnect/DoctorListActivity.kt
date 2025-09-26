package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.adapters.DoctorAdapter
import com.example.fitconnect.adapters.DoctorListAdapter
import com.example.fitconnect.models.Doctor
import com.example.fitconnect.services.DoctorService
import com.example.fitconnect.utils.CategoryNames
import kotlinx.coroutines.launch

class DoctorListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var doctorAdapter: DoctorListAdapter
    private lateinit var etSearch: EditText
    private lateinit var categoryLayout: LinearLayout
    private lateinit var btnBack: ImageView

    private val doctorService = DoctorService()
    private val categoryNames = CategoryNames()
    private var allDoctors = listOf<Doctor>()
    private var filteredDoctors = listOf<Doctor>()
    private var selectedCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectedCategory = intent.getStringExtra("category") ?: "All"

        initViews()
        setupRecyclerView()
        setupCategoryFilter()
        setupSearch()
        loadDoctors()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewDoctors)
        etSearch = findViewById(R.id.etSearch)
        categoryLayout = findViewById(R.id.categoryLayout)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        doctorAdapter = DoctorListAdapter(filteredDoctors) { doctor ->
            showDoctorDetails(doctor)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = doctorAdapter
    }

    private fun setupCategoryFilter() {
        val categories = listOf("All") + categoryNames.categoryNames

        categories.forEach { category ->
            val categoryView = createCategoryView(category)
            categoryLayout.addView(categoryView)
        }

        updateCategorySelection(selectedCategory)
    }

    private fun createCategoryView(category: String): TextView {
        val textView = TextView(this)
        textView.text = category
        textView.setPadding(24, 12, 24, 12)
        textView.textSize = 14f
        textView.setOnClickListener {
            selectedCategory = category
            updateCategorySelection(category)
            filterDoctors()
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 16, 0)
        textView.layoutParams = params

        return textView
    }

    private fun updateCategorySelection(selectedCategory: String) {
        for (i in 0 until categoryLayout.childCount) {
            val child = categoryLayout.getChildAt(i) as TextView
            if (child.text == selectedCategory) {
                child.background = ContextCompat.getDrawable(this, R.drawable.category_selected_shape)
                child.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                child.background = ContextCompat.getDrawable(this, R.drawable.category_unselected_shape)
                child.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterDoctors()
            }
        })
    }

    private fun loadDoctors() {
        lifecycleScope.launch {
            try {
                allDoctors = doctorService.getAllDoctors()
                filterDoctors()
            } catch (e: Exception) {
                Toast.makeText(this@DoctorListActivity, "Failed to load doctors", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterDoctors() {
        val searchText = etSearch.text.toString().lowercase()

        filteredDoctors = allDoctors.filter { doctor ->
            val matchesCategory = selectedCategory == "All" || doctor.categoryName == selectedCategory
            val matchesSearch = doctor.name.lowercase().contains(searchText) ||
                    doctor.categoryName.lowercase().contains(searchText) ||
                    doctor.place.lowercase().contains(searchText)

            matchesCategory && matchesSearch
        }

        doctorAdapter.updateDoctors(filteredDoctors)
    }

    private fun showDoctorDetails(doctor: Doctor) {
        startActivity(
            Intent(this, DoctorDetailsActivity::class.java).apply {
                putExtra("doctorId", doctor.snapshotId)
            }
        )
    }
}