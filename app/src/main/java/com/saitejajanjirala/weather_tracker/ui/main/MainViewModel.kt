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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MainViewModel  @Inject constructor(private val weatherRepo: WeatherRepo): ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery : StateFlow<String>
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
        viewModelScope.launch {
            searchQuery.debounce(300)
                .distinctUntilChanged()
                .flatMapLatest {
                    if(it.isBlank()){
                        flowOf(Result.Loading(false))
                    }
                    else{
                        weatherRepo.getSearchResults(it)
                    }
                }
                .collect {
                    _searchItems.value = it
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

    fun onNewSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery

    }


    fun showSaved(){
        _isCurrentResult.value = false
        onNewSearchQuery("")
        viewModelScope.launch {
            state.value.d?.let { weatherRepo.saveWeatherToLocal(it) }
        }
    }


}