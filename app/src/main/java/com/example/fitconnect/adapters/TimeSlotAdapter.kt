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
import com.example.fitconnect.models.TimeSlot

class TimeSlotAdapter(
    private val onTimeClick: (TimeSlot) -> Unit
) : ListAdapter<TimeSlot, TimeSlotAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun selectTime(timeSlot: TimeSlot) {
        val newPosition = currentList.indexOfFirst { it.time == timeSlot.time }
        val oldPosition = selectedPosition
        selectedPosition = newPosition

        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (newPosition != -1) {
            notifyItemChanged(newPosition)
        }
    }

    fun clearSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
    }

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(timeSlot: TimeSlot, isSelected: Boolean) {
            timeText.text = timeSlot.time

            if (isSelected) {
                timeText.background = ContextCompat.getDrawable(itemView.context, R.drawable.selected_time_background)
                timeText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            } else {
                timeText.background = ContextCompat.getDrawable(itemView.context, R.drawable.time_slot_background)
                timeText.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_text))
            }

            itemView.setOnClickListener {
                onTimeClick(timeSlot)
            }
        }
    }

    class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
        override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem == newItem
        }
    }
}