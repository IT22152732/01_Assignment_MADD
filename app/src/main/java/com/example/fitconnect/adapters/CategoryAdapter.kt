package com.example.fitconnect.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.models.Category

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var filteredCategories = categories.toList()

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardIcon: CardView = itemView.findViewById(R.id.cardIcon)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)

        fun bind(category: Category) {
            cardIcon.setCardBackgroundColor(Color.parseColor(category.backgroundColor))
            ivIcon.setImageResource(category.icon)
            tvName.text = category.name

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(filteredCategories[position])
    }

    override fun getItemCount(): Int = filteredCategories.size

    fun filter(query: String) {
        filteredCategories = if (query.isEmpty()) {
            categories
        } else {
            categories.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}