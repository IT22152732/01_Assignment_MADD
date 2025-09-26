package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitconnect.R
import com.example.fitconnect.models.Doctor

class DoctorListAdapter(
    private var doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorListAdapter.DoctorViewHolder>() {

    fun updateDoctors(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount(): Int = doctors.size

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivDoctorImage: ImageView = itemView.findViewById(R.id.ivDoctorImage)
        private val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        private val tvSpecialty: TextView = itemView.findViewById(R.id.tvSpecialty)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvReviews: TextView = itemView.findViewById(R.id.tvReviews)

        fun bind(doctor: Doctor) {
            tvDoctorName.text = doctor.name
            tvSpecialty.text = doctor.categoryName
            tvLocation.text = doctor.place
            tvRating.text = "4.5"
            tvReviews.text = "${doctor.patients} Reviews"

            Glide.with(itemView.context)
                .load(doctor.imageUrl)
                .placeholder(R.drawable.profile_placeholder)
                .error(R.drawable.profile_placeholder)
                .into(ivDoctorImage)

            itemView.setOnClickListener {
                onDoctorClick(doctor)
            }
        }
    }
}