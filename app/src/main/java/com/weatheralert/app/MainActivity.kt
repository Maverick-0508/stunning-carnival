package com.weatheralert.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.weatheralert.app.adapters.AlertAdapter
import com.weatheralert.app.api.WeatherApiClient
import com.weatheralert.app.databinding.ActivityMainBinding
import com.weatheralert.app.notifications.NotificationHelper
import com.weatheralert.app.utils.CalamityDetector
import com.weatheralert.app.utils.PreferencesManager
import com.weatheralert.app.workers.WeatherCheckWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled. You won't receive calamity alerts.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)
        notificationHelper = NotificationHelper(this)

        setupRecyclerView()
        setupClickListeners()
        requestNotificationPermission()
        scheduleWeatherMonitoring()
        loadWeatherData()
    }

    private fun setupRecyclerView() {
        alertAdapter = AlertAdapter()
        binding.recyclerAlerts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alertAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener { loadWeatherData() }
        binding.btnSettings.setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }
        binding.btnViewAlerts.setOnClickListener {
            startActivity(android.content.Intent(this, AlertsActivity::class.java))
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleWeatherMonitoring() {
        val intervalMinutes = prefsManager.monitoringIntervalMinutes.toLong()
        val workRequest = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            intervalMinutes, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeatherCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun loadWeatherData() {
        val apiKey = prefsManager.apiKey
        val city = prefsManager.city

        binding.progressBar.visibility = View.VISIBLE
        binding.layoutWeatherInfo.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        if (apiKey == PreferencesManager.DEFAULT_API_KEY || apiKey.isBlank()) {
            binding.progressBar.visibility = View.GONE
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.error_no_api_key)
            return
        }

        lifecycleScope.launch {
            try {
                val response = WeatherApiClient.apiService.getCurrentWeather(city, apiKey)
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val weather = response.body()
                    if (weather != null) {
                        binding.layoutWeatherInfo.visibility = View.VISIBLE
                        binding.tvCityName.text = "${weather.name}, ${weather.sys?.country ?: ""}"
                        binding.tvTemperature.text = "${String.format("%.1f", weather.main.temp)}°C"
                        binding.tvDescription.text = weather.weather.firstOrNull()?.description
                            ?.replaceFirstChar { it.uppercase() } ?: ""
                        binding.tvHumidity.text = "Humidity: ${weather.main.humidity}%"
                        binding.tvWindSpeed.text = "Wind: ${String.format("%.1f", weather.wind?.speed ?: 0.0)} m/s"
                        binding.tvFeelsLike.text = "Feels like: ${String.format("%.1f", weather.main.feelsLike)}°C"

                        val alerts = CalamityDetector.detectCalamities(weather)
                        alertAdapter.updateAlerts(alerts)

                        binding.tvAlertsHeader.text = if (alerts.isEmpty()) {
                            getString(R.string.no_active_alerts)
                        } else {
                            "${alerts.size} ${getString(R.string.active_alerts)}"
                        }

                        alerts.forEach { alert ->
                            if (prefsManager.isAlertTypeEnabled(alert.type)) {
                                notificationHelper.showCalamityNotification(alert)
                            }
                        }

                        val widgetPrefs = getSharedPreferences("widget_data", MODE_PRIVATE)
                        widgetPrefs.edit()
                            .putString("current_temp", String.format("%.1f", weather.main.temp))
                            .putString("current_description", weather.weather.firstOrNull()?.description ?: "")
                            .putString("active_alerts", alerts.joinToString(", ") { it.type.displayName })
                            .apply()

                        val updateIntent = android.content.Intent("com.weatheralert.app.UPDATE_WIDGET")
                        sendBroadcast(updateIntent)
                    }
                } else {
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "Failed to load weather: ${e.message}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadWeatherData()
    }
}
