package com.weatheralert.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.weatheralert.app.R
import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter(private val alerts: MutableList<CalamityAlert> = mutableListOf()) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.alert_icon)
        val title: TextView = itemView.findViewById(R.id.alert_title)
        val description: TextView = itemView.findViewById(R.id.alert_description)
        val severity: TextView = itemView.findViewById(R.id.alert_severity)
        val timestamp: TextView = itemView.findViewById(R.id.alert_timestamp)
        val city: TextView = itemView.findViewById(R.id.alert_city)
    }

    private class AlertDiffCallback(
        private val oldList: List<CalamityAlert>,
        private val newList: List<CalamityAlert>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].type == newList[newPos].type &&
            oldList[oldPos].timestamp == newList[newPos].timestamp
        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.title.text = alert.title
        holder.description.text = alert.description
        holder.severity.text = alert.severity.name
        holder.city.text = alert.city
        holder.timestamp.text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date(alert.timestamp))

        val iconRes = when (alert.type) {
            AlertType.FLOOD -> R.drawable.ic_flood
            AlertType.THUNDERSTORM -> R.drawable.ic_storm
            AlertType.EXTREME_HEAT, AlertType.EXTREME_COLD -> R.drawable.ic_temperature_high
            else -> R.drawable.ic_notification
        }
        holder.icon.setImageResource(iconRes)

        val severityColor = when (alert.severity) {
            CalamityAlert.Severity.CRITICAL -> 0xFFD32F2F.toInt()
            CalamityAlert.Severity.HIGH -> 0xFFF57C00.toInt()
            CalamityAlert.Severity.MEDIUM -> 0xFFFBC02D.toInt()
            CalamityAlert.Severity.LOW -> 0xFF388E3C.toInt()
        }
        holder.severity.setTextColor(severityColor)
    }

    override fun getItemCount() = alerts.size

    fun updateAlerts(newAlerts: List<CalamityAlert>) {
        val diffResult = DiffUtil.calculateDiff(AlertDiffCallback(alerts, newAlerts))
        alerts.clear()
        alerts.addAll(newAlerts)
        diffResult.dispatchUpdatesTo(this)
    }
}
