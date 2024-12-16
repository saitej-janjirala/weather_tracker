package com.saitejajanjirala.weather_tracker.domain.models.util

sealed class Result<T>(val d: T? = null, val m: String? = null,val loading: Boolean = false) {
    data class Success<T>(val data: T) : Result<T>(d=data)
    data class Error<T>(val message: String) : Result<T>(m=message)
    class Empty<T>():Result<T>()
    class Loading<T>(val isLoading: Boolean) : Result<T>(loading = isLoading)
}