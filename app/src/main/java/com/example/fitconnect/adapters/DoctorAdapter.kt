package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitconnect.databinding.ItemDoctorBinding
import com.example.fitconnect.models.Doctor

class DoctorAdapter(
    private val onEditClick: (Doctor) -> Unit,
    private val onDeleteClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private var doctors = listOf<Doctor>()

    fun updateDoctors(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(doctors[position])
    }

    override fun getItemCount(): Int = doctors.size

    inner class DoctorViewHolder(private val binding: ItemDoctorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doctor: Doctor) {
            binding.tvDoctorName.text = doctor.name
            binding.tvSpecialty.text = doctor.categoryName
            binding.tvLocation.text = doctor.place

            Glide.with(binding.root.context)
                .load(doctor.imageUrl)
                .centerCrop()
                .into(binding.ivDoctorImage)

            binding.btnEdit.setOnClickListener {
                onEditClick(doctor)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(doctor)
            }
        }
    }
}