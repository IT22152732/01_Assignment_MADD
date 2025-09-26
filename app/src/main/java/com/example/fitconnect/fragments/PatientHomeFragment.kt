package com.example.fitconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.DoctorListActivity
import com.example.fitconnect.R
import com.example.fitconnect.adapters.CategoryAdapter
import com.example.fitconnect.models.Category

class PatientHomeFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCategories()
        setupSearch()
    }

    private fun initViews(view: View) {
        rvCategories = view.findViewById(R.id.rvCategories)
        etSearch = view.findViewById(R.id.etSearch)
    }

    private fun setupCategories() {
        val categories = listOf(
            Category(1, "General", R.drawable.sethescope, "#A78BFA"),
            Category(2, "Dentistry", R.drawable.dentist, "#FB7185"),
            Category(3, "Physiotherapist", R.drawable.physio, "#67E8F9"),
            Category(4, "Personal Trainer", R.drawable.personal, "#4C1D95"),
            Category(5, "Yoga Instructor", R.drawable.yoga, "#86EFAC"),
            Category(6, "Strength Coach", R.drawable.streangth, "#FDBA74"),
            Category(7, "Dentistry", R.drawable.dentist, "#F9A8D4"),
            Category(8, "Nutrition coach", R.drawable.nutrition, "#4C1D95"),
            Category(9, "Strength Coach", R.drawable.streangth, "#5EEAD4"),
            Category(10, "Pilates Trainer", R.drawable.guidance_pilates, "#5EEAD4")
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            val intent = Intent(requireContext(), DoctorListActivity::class.java).apply {
                putExtra("category", category.name)
            }
            startActivity(intent)
        }

        rvCategories.layoutManager = GridLayoutManager(context, 2)
        rvCategories.adapter = categoryAdapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                categoryAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}