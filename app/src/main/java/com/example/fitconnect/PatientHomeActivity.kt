package com.example.fitconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitconnect.fragments.PatientAppointmentsFragment
import com.example.fitconnect.fragments.PatientHomeFragment
import com.example.fitconnect.fragments.PatientProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class PatientHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_home)

        loadFragment(PatientHomeFragment())

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(PatientHomeFragment())
                    true
                }
                R.id.nav_appointments -> {
                    loadFragment(PatientAppointmentsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(PatientProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}