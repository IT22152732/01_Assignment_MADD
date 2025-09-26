package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.databinding.ItemAdminCategoryBinding

class CategoryAdminAdapter(
    private val categories: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdminAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemAdminCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemAdminCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, isSelected: Boolean) {
            binding.tvCategory.text = category

            if (isSelected) {
                binding.tvCategory.setBackgroundResource(com.example.fitconnect.R.drawable.category_selected_shape)
                binding.tvCategory.setTextColor(android.graphics.Color.WHITE)
            } else {
                binding.tvCategory.setBackgroundResource(com.example.fitconnect.R.drawable.category_unselected_shape)
                binding.tvCategory.setTextColor(android.graphics.Color.parseColor("#2C3E50"))
            }

            binding.tvCategory.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onCategoryClick(category)
            }
        }
    }
}