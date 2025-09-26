package com.example.fitconnect

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitconnect.fragments.DoctorAppointmentsFragment
import com.example.fitconnect.fragments.DoctorProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DoctorDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Default fragment
        loadFragment(DoctorAppointmentsFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_appointments -> {
                    loadFragment(DoctorAppointmentsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(DoctorProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
