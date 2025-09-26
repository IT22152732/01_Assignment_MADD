package com.example.fitconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private var notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconBackground: View = itemView.findViewById(R.id.iconBackground)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        val time: TextView = itemView.findViewById(R.id.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.title.text = notification.title
        holder.subtitle.text = notification.subtitle
        holder.time.text = getFormattedTime(notification.createdAt)

        when (notification.title.lowercase()) {
            "appointment success" -> {
                holder.iconBackground.setBackgroundResource(R.drawable.circle_green_bg)
                holder.icon.setImageResource(R.drawable.ic_calendar_check)
            }
            "appointment cancelled" -> {
                holder.iconBackground.setBackgroundResource(R.drawable.circle_red_bg)
                holder.icon.setImageResource(R.drawable.ic_calendar_x)
            }
            "scheduled changed" -> {
                holder.iconBackground.setBackgroundResource(R.drawable.circle_gray_bg)
                holder.icon.setImageResource(R.drawable.ic_calendar_edit)
            }
            else -> {
                holder.iconBackground.setBackgroundResource(R.drawable.circle_gray_bg)
                holder.icon.setImageResource(R.drawable.ic_calendar_check)
            }
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }

    private fun getFormattedTime(createdAt: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(createdAt)
            val now = Date()
            val diffInMs = now.time - (date?.time ?: 0)

            when {
                diffInMs < 60 * 1000 -> "Just now"
                diffInMs < 60 * 60 * 1000 -> "${diffInMs / (60 * 1000)}m"
                diffInMs < 24 * 60 * 60 * 1000 -> "${diffInMs / (60 * 60 * 1000)}h"
                else -> "${diffInMs / (24 * 60 * 60 * 1000)}d"
            }
        } catch (e: Exception) {
            "Now"
        }
    }
}