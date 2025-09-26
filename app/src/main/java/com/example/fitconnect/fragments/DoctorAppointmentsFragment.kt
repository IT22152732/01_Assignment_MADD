package com.example.fitconnect.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.PatientLoginActivity
import com.example.fitconnect.models.Appointment
import com.example.fitconnect.services.AppointmentService
import com.example.fitconnect.services.LocalPrefsService
import com.example.fitconnect.services.PatientService
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class DoctorAppointmentsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appointmentsAdapter: AppointmentsAdapter
    private lateinit var appointmentService: AppointmentService
    private lateinit var patientService: PatientService
    private lateinit var localPrefsService: LocalPrefsService
    private lateinit var logoutButton: ImageView
    private val appointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doctor_appointments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeServices()
        setupViews(view)
        loadAppointments()
    }

    private fun initializeServices() {
        appointmentService = AppointmentService()
        patientService = PatientService()
        localPrefsService = LocalPrefsService(requireContext())
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewAppointments)
        logoutButton = view.findViewById(R.id.ivLogout)

        appointmentsAdapter = AppointmentsAdapter(appointments) { appointment, action ->
            handleAppointmentAction(appointment, action)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentsAdapter
        }

        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            try {
                val doctorId = localPrefsService.getDoctorId()
                if (doctorId != null) {
                    val doctorAppointments = appointmentService.getAppointmentsByDoctorId(doctorId)
                    appointments.clear()
                    appointments.addAll(doctorAppointments)
                    appointmentsAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading appointments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAppointmentAction(appointment: Appointment, action: String) {
        lifecycleScope.launch {
            try {
                val updatedAppointment = appointment.copy(status = action)
                val success = appointmentService.updateAppointment(appointment.id, updatedAppointment)

                if (success) {
                    val index = appointments.indexOfFirst { it.id == appointment.id }
                    if (index != -1) {
                        appointments[index] = updatedAppointment
                        appointmentsAdapter.notifyItemChanged(index)
                    }
                    Toast.makeText(requireContext(), "Appointment ${action.lowercase()}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update appointment", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating appointment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        localPrefsService.logout()
        val intent = Intent(requireContext(), PatientLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    inner class AppointmentsAdapter(
        private val appointments: List<Appointment>,
        private val onActionClick: (Appointment, String) -> Unit
    ) : RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {

        inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
            val ivPatientImage: ImageView = itemView.findViewById(R.id.ivPatientImage)
            val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
            val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
            val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
            val btnReject: Button = itemView.findViewById(R.id.btnReject)
            val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doctor_appointment, parent, false)
            return AppointmentViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
            val appointment = appointments[position]

            // Set date and time
            holder.tvDateTime.text = "${appointment.date} - ${appointment.hour}"

            // Set patient name
            holder.tvPatientName.text = if (appointment.patientName.isNotEmpty()) {
                appointment.patientName
            } else {
                "Patient"
            }

            // Load patient data to get additional info
            lifecycleScope.launch {
                try {
                    val patient = patientService.getPatientById(appointment.patientId)
                    patient?.let {
                        holder.tvPatientName.text = it.name.ifEmpty { "Patient" }

                        // Load profile image
                        if (it.profileImageUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(it.profileImageUrl)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(holder.ivPatientImage)
                        }
                    }
                } catch (e: Exception) {
                    // Handle error silently or show default values
                }
            }

            // Set specialty and location (you can customize these)
            holder.tvSpecialty.text = "Orthopedic Surgery"
            holder.tvLocation.text = "Elite Ortho Clinic, USA"

            // Handle button clicks
            holder.btnReject.setOnClickListener {
                if (appointment.status != "Rejected") {
                    onActionClick(appointment, "Rejected")
                }
            }

            holder.btnApprove.setOnClickListener {
                if (appointment.status != "Approved") {
                    onActionClick(appointment, "Approved")
                }
            }

            // Update button states based on current status
            when (appointment.status) {
                "Approved" -> {
                    holder.btnApprove.isEnabled = false
                    holder.btnApprove.alpha = 0.6f
                    holder.btnReject.isEnabled = true
                    holder.btnReject.alpha = 1.0f
                }
                "Rejected" -> {
                    holder.btnReject.isEnabled = false
                    holder.btnReject.alpha = 0.6f
                    holder.btnApprove.isEnabled = true
                    holder.btnApprove.alpha = 1.0f
                }
                else -> {
                    holder.btnApprove.isEnabled = true
                    holder.btnApprove.alpha = 1.0f
                    holder.btnReject.isEnabled = true
                    holder.btnReject.alpha = 1.0f
                }
            }
        }

        override fun getItemCount(): Int = appointments.size
    }
}