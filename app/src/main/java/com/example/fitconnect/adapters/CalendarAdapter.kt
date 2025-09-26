package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.models.CalendarDay

class CalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarAdapter.CalendarViewHolder>(CalendarDiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun selectDay(day: CalendarDay) {
        val newPosition = currentList.indexOf(day)
        val oldPosition = selectedPosition
        selectedPosition = newPosition

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (newPosition != -1) {
            notifyItemChanged(newPosition)
        }
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)

        fun bind(day: CalendarDay, isSelected: Boolean) {
            if (day.day.isEmpty()) {
                dayText.text = ""
                dayText.background = null
                itemView.setOnClickListener(null)
                return
            }

            dayText.text = day.day

            when {
                isSelected -> {
                    dayText.background = ContextCompat.getDrawable(itemView.context, R.drawable.selected_day_background)
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                !day.isSelectable -> {
                    dayText.background = null
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.disabled_text))
                }
                else -> {
                    dayText.background = null
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))
                }
            }

            if (day.isSelectable) {
                itemView.setOnClickListener {
                    onDayClick(day)
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }

    class CalendarDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
