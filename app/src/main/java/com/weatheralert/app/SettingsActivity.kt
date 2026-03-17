package com.weatheralert.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weatheralert.app.databinding.ActivitySettingsBinding
import com.weatheralert.app.models.AlertType
import com.weatheralert.app.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_settings)

        prefsManager = PreferencesManager(this)
        loadSettings()

        binding.btnSaveSettings.setOnClickListener { saveSettings() }
    }

    private fun loadSettings() {
        binding.etCity.setText(prefsManager.city)
        binding.etApiKey.setText(prefsManager.apiKey)

        binding.switchFlood.isChecked = prefsManager.isAlertTypeEnabled(AlertType.FLOOD)
        binding.switchHeat.isChecked = prefsManager.isAlertTypeEnabled(AlertType.EXTREME_HEAT)
        binding.switchCold.isChecked = prefsManager.isAlertTypeEnabled(AlertType.EXTREME_COLD)
        binding.switchRain.isChecked = prefsManager.isAlertTypeEnabled(AlertType.HEAVY_RAIN)
        binding.switchWind.isChecked = prefsManager.isAlertTypeEnabled(AlertType.STRONG_WIND)
        binding.switchStorm.isChecked = prefsManager.isAlertTypeEnabled(AlertType.THUNDERSTORM)
        binding.switchDrought.isChecked = prefsManager.isAlertTypeEnabled(AlertType.DROUGHT)

        val intervals = resources.getStringArray(R.array.interval_values)
        val currentInterval = prefsManager.monitoringIntervalMinutes.toString()
        val index = intervals.indexOf(currentInterval)
        if (index >= 0) binding.spinnerInterval.setSelection(index)
    }

    private fun saveSettings() {
        val city = binding.etCity.text.toString().trim()
        val apiKey = binding.etApiKey.text.toString().trim()

        if (city.isEmpty()) {
            binding.etCity.error = getString(R.string.error_city_empty)
            return
        }

        prefsManager.city = city
        if (apiKey.isNotEmpty()) prefsManager.apiKey = apiKey

        prefsManager.setAlertTypeEnabled(AlertType.FLOOD, binding.switchFlood.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.EXTREME_HEAT, binding.switchHeat.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.EXTREME_COLD, binding.switchCold.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.HEAVY_RAIN, binding.switchRain.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.STRONG_WIND, binding.switchWind.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.THUNDERSTORM, binding.switchStorm.isChecked)
        prefsManager.setAlertTypeEnabled(AlertType.DROUGHT, binding.switchDrought.isChecked)

        val intervalValues = resources.getStringArray(R.array.interval_values)
        val selectedInterval = intervalValues[binding.spinnerInterval.selectedItemPosition].toIntOrNull() ?: 180
        prefsManager.monitoringIntervalMinutes = selectedInterval

        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
