package com.example.weatherinfo.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.weatherinfo.BuildConfig
import com.example.weatherinfo.R
import com.example.weatherinfo.databinding.FragmentWeatherBinding
import com.example.weatherinfo.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by viewModels()

    //for retrieving device location
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    //to store and retrieve last searched city
    @Inject
    lateinit var appPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentWeatherBinding.bind(view)

        //// Retrieve the last searched city from SharedPreferences
        val lastSearchedCity = appPreferences.getString("last_searched_city", null)
        if (lastSearchedCity != null) {
            viewModel.fetchWeather(lastSearchedCity, BuildConfig.API_KEY)
            Toast.makeText(
                requireContext(),
                "Loading weather for $lastSearchedCity",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonGetWeather.setOnClickListener {
            val cityName = binding.editTextCity.text.toString()

            if (cityName.isNotEmpty()) {
                appPreferences.edit().putString("last_searched_city", cityName).apply()
                // Fetch weather data
                viewModel.fetchWeather(cityName, BuildConfig.API_KEY)
            }
        }

        // Collect and observe weather data for a city and apply it to the UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherData.collect { weather ->
                    if (weather != null) {
                        Log.d("WeatherFragment", "City weather received: ${weather.main.temp}")

                        // Convert temperature from Kelvin to Celsius
                        val temperatureInCelsius = weather.main.temp - 273.15
                        val feelsLikeCelsius = weather.main.feels_like - 273.15
                        val minTempCelsius = weather.main.temp_min - 273.15
                        val maxTempCelsius = weather.main.temp_max - 273.15

                        binding.textViewCityName.text = weather.name

                        binding.textViewTemperature.text =
                            "Temperature: %.2f °C".format(temperatureInCelsius)
                        binding.textViewFeelsLike.text =
                            "Feels like: %.2f °C".format(feelsLikeCelsius)
                        binding.textViewMinMaxTemp.text =
                            "Min Temp: %.2f °C, Max Temp: %.2f °C".format(
                                minTempCelsius,
                                maxTempCelsius
                            )

                        binding.textViewHumidity.text = "Humidity: ${weather.main.humidity} %"
                        binding.textViewWeatherCondition.text =
                            "Condition: ${weather.weather[0].description}"

                        val iconCode = weather.weather[0].icon
                        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                        Glide.with(this@WeatherFragment)
                            .load(iconUrl)
                            .into(binding.imageViewWeatherIcon)
                    } else {
                        Log.d("WeatherFragment", "No weather data available")
                    }
                }
            }
        }

        //current location weather
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentCityWeatherData.collect { weather ->
                    if (weather != null) {
                        Log.d(
                            "WeatherFragment",
                            "Current city weather received: ${weather.main.temp}"
                        )
                        val temperatureInCelsius = weather.main.temp - 273.15
                        val feelsLikeCelsius = weather.main.feels_like - 273.15
                        val minTempCelsius = weather.main.temp_min - 273.15
                        val maxTempCelsius = weather.main.temp_max - 273.15

                        binding.textViewCurrentCityName.text = weather.name
                        binding.textViewCurrentTemperature.text =
                            "Temperature: %.2f °C".format(temperatureInCelsius)
                        binding.textViewCurrentFeelsLike.text =
                            "Feels like: %.2f °C".format(feelsLikeCelsius)
                        binding.textViewCurrentMinMaxTemp.text =
                            "Min Temp: %.2f °C, Max Temp: %.2f °C".format(
                                minTempCelsius,
                                maxTempCelsius
                            )

                        binding.textViewCurrentHumidity.text =
                            "Humidity: ${weather.main.humidity} %"
                        binding.textViewCurrentCondition.text =
                            "Condition: ${weather.weather[0].description}"

                        // Load weather icon
                        val iconCode = weather.weather[0].icon
                        val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                        Glide.with(this@WeatherFragment)
                            .load(iconUrl)
                            .into(binding.imageViewCurrentWeatherIcon)
                    }
                }
            }
        }
        // Request location permission to get weather data
        requestLocationPermission()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        Log.d("WeatherFragment", "getCurrentLocation")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    getWeatherForLocation(latitude, longitude)
                } else {
                    Log.d("WeatherFragment", "Unable to get location")
                }
            }
    }

    private fun getWeatherForLocation(latitude: Double, longitude: Double) {
        Log.d("WeatherFragment", "getWeatherForLocation")
        viewModel.getCurrentLocationWeatherDetails(latitude, longitude, BuildConfig.API_KEY)
    }

    // checking the location access permission
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission is required to get weather data.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}