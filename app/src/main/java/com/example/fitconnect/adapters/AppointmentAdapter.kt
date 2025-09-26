package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitconnect.R
import com.example.fitconnect.models.Appointment
import com.example.fitconnect.services.DoctorService
import kotlinx.coroutines.launch

class AppointmentAdapter(
    private val appointments: MutableList<Appointment>,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    private val doctorService: DoctorService = DoctorService()

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorImage: ImageView = itemView.findViewById(R.id.ivDoctorImage)
        val doctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val specialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        val location: TextView = itemView.findViewById(R.id.tvLocation)
        val date: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.doctorName.text = appointment.doctorName
        holder.date.text = "${appointment.date} - ${appointment.hour}"

        // Default values while fetching doctor
        holder.specialty.text = "Loading..."
        holder.location.text = "Loading..."

        // Load doctor details asynchronously
        lifecycleOwner.lifecycleScope.launch {
            try {
                val doctor = doctorService.getDoctorById(appointment.doctorId)
                doctor?.let {
                    holder.specialty.text = it.categoryName
                    holder.location.text = it.place

                    Glide.with(holder.itemView.context)
                        .load(it.imageUrl.ifEmpty { "https://via.placeholder.com/60x60" })
                        .placeholder(R.drawable.circle_background)
                        .error(R.drawable.circle_background)
                        .circleCrop()
                        .into(holder.doctorImage)
                } ?: run {
                    holder.specialty.text = "Unknown Specialty"
                    holder.location.text = "Unknown Location"

                    Glide.with(holder.itemView.context)
                        .load("https://via.placeholder.com/60x60")
                        .placeholder(R.drawable.circle_background)
                        .circleCrop()
                        .into(holder.doctorImage)
                }
            } catch (e: Exception) {
                holder.specialty.text = "Error loading data"
                holder.location.text = "Error loading data"

                Glide.with(holder.itemView.context)
                    .load("https://via.placeholder.com/60x60")
                    .placeholder(R.drawable.circle_background)
                    .circleCrop()
                    .into(holder.doctorImage)
            }
        }
    }

    fun updateData(newAppointments: List<Appointment>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged()
    }

    override fun getItemCount() = appointments.size
}
