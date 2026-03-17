package com.weatheralert.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.weatheralert.app.adapters.AlertAdapter
import com.weatheralert.app.databinding.ActivityAlertsBinding
import com.weatheralert.app.utils.PreferencesManager

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
        val prefs = PreferencesManager(this)
        val stored = prefs.getStoredAlerts()
        alertAdapter.updateAlerts(stored)

        if (stored.isEmpty()) {
            binding.tvNoAlerts.visibility = android.view.View.VISIBLE
            binding.recyclerAllAlerts.visibility = android.view.View.GONE
        } else {
            binding.tvNoAlerts.visibility = android.view.View.GONE
            binding.recyclerAllAlerts.visibility = android.view.View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
