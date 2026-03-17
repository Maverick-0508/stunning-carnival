package com.weatheralert.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.weatheralert.app.adapters.AlertAdapter
import com.weatheralert.app.databinding.ActivityAlertsBinding
import com.weatheralert.app.models.AlertType
import com.weatheralert.app.models.CalamityAlert

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private lateinit var alertAdapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_alerts)

        alertAdapter = AlertAdapter()
        binding.recyclerAllAlerts.apply {
            layoutManager = LinearLayoutManager(this@AlertsActivity)
            adapter = alertAdapter
        }

        loadAlerts()
    }

    private fun loadAlerts() {
        val sampleAlerts = listOf(
            CalamityAlert(
                type = AlertType.HEAVY_RAIN,
                title = "Heavy Rain Warning",
                description = "Heavy rainfall expected in the coming hours.",
                severity = CalamityAlert.Severity.HIGH,
                city = "Nairobi"
            ),
            CalamityAlert(
                type = AlertType.FLOOD,
                title = "Flood Risk Advisory",
                description = "River levels rising. Avoid low-lying areas.",
                severity = CalamityAlert.Severity.MEDIUM,
                city = "Nairobi"
            )
        )
        alertAdapter.updateAlerts(sampleAlerts)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
