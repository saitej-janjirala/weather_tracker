package com.saitejajanjirala.weather_tracker.ui.main

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saitejajanjirala.weather_tracker.domain.repo.WeatherRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel  @Inject constructor(private val weatherRepo: WeatherRepo): ViewModel() {
    var searchQuery by mutableStateOf("")
        private set
    private val _state = MutableStateFlow<Result<SimplifiedWeatherResult>>(Result.Loading(false))
    val state: StateFlow<Result<SimplifiedWeatherResult>> = _state

    private val _isCurrentResult = MutableStateFlow(false)
    val isCurrentResult: StateFlow<Boolean> = _isCurrentResult

    init {
        viewModelScope.launch {
            weatherRepo.getWeather().collect{it->
                _isCurrentResult.value = false
                _state.value = it
            }
        }

    }
    fun search() {
        viewModelScope.launch {
            weatherRepo.getWeather(searchQuery).collect{
                _isCurrentResult.value = true
                _state.value = it
            }
        }
    }

    fun onNewSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }

    fun showSaved(){
        _isCurrentResult.value = false
        onNewSearchQuery("")
    }


}