package com.saitejajanjirala.weather_tracker.ui.main

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saitejajanjirala.weather_tracker.domain.models.remote.SearchResultItem
import com.saitejajanjirala.weather_tracker.domain.repo.WeatherRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class MainViewModel  @Inject constructor(private val weatherRepo: WeatherRepo): ViewModel() {
    val _searchQuery = MutableStateFlow("")
    private val searchQuery : StateFlow<String>
        get() = _searchQuery

    private val _searchItems =  MutableStateFlow<Result<List<SearchResultItem>>>(Result.Loading(false))
    val searchItems :StateFlow<Result<List<SearchResultItem>>> = _searchItems

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
    fun search(city:String) {
        viewModelScope.launch {
            weatherRepo.getWeather(city).collect{
                _isCurrentResult.value = true
                _state.value = it
                _searchQuery.value = ""
            }
        }
    }

    @OptIn(FlowPreview::class)
    fun onNewSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
        viewModelScope.launch {
            searchQuery.debounce(300)
                .collect {
                    getCities(it)
                }
        }
    }

    suspend fun getCities(key:String){
        weatherRepo.getSearchResults(key).collect{
            _searchItems.value = it
        }
    }

    fun showSaved(){
        _isCurrentResult.value = false
        onNewSearchQuery("")
        viewModelScope.launch {
            state.value.d?.let { weatherRepo.saveWeatherToLocal(it) }
        }
    }


}