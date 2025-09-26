package com.example.fitconnect.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.adapters.AppointmentAdapter
import com.example.fitconnect.models.Appointment
import com.example.fitconnect.services.AppointmentService
import com.example.fitconnect.services.LocalPrefsService
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class PatientAppointmentsFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var appointmentService: AppointmentService
    private lateinit var localPrefsService: LocalPrefsService
    private lateinit var tabLayout: TabLayout

    private val allAppointments = mutableListOf<Appointment>()
    private val pendingAppointments = mutableListOf<Appointment>()
    private val pastAppointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_patient_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupServices()
        setupRecyclerView()
        setupTabs()
        loadAppointments()
    }

    private fun initViews(view: View) {
        titleTextView = view.findViewById(R.id.tvMyBookings)
        recyclerView = view.findViewById(R.id.rvAppointments)
        tabLayout = view.findViewById(R.id.tabLayout)
    }

    private fun setupServices() {
        appointmentService = AppointmentService()
        localPrefsService = LocalPrefsService(requireContext())
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(mutableListOf(), this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = appointmentAdapter
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showPendingAppointments()
                    1 -> showPastAppointments()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadAppointments() {
        val patientId = localPrefsService.getPatientId()
        if (patientId != null) {
            lifecycleScope.launch {
                try {
                    val patientAppointments = appointmentService.getAppointmentsByPatientId(patientId)
                    allAppointments.clear()
                    allAppointments.addAll(patientAppointments)

                    // Separate pending and past
                    pendingAppointments.clear()
                    pastAppointments.clear()
                    allAppointments.forEach { appointment ->
                        if (appointment.status.equals("Pending", ignoreCase = true)) {
                            pendingAppointments.add(appointment)
                        } else {
                            pastAppointments.add(appointment)
                        }
                    }

                    // Default -> show pending
                    showPendingAppointments()
                } catch (e: Exception) {
                    Log.e("PatientAppointments", "Error loading appointments: ${e.message}")
                }
            }
        }
    }

    private fun showPendingAppointments() {
        appointmentAdapter.updateData(pendingAppointments)
    }

    private fun showPastAppointments() {
        appointmentAdapter.updateData(pastAppointments)
    }
}
