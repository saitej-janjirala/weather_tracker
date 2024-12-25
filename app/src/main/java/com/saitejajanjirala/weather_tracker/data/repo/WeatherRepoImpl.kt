package com.saitejajanjirala.weather_tracker.data.repo

import com.saitejajanjirala.weather_tracker.data.local.DataStoreManager
import com.saitejajanjirala.weather_tracker.data.remote.ApiService
import com.saitejajanjirala.weather_tracker.di.NoInternetException
import com.saitejajanjirala.weather_tracker.domain.models.remote.SearchResultItem
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import com.saitejajanjirala.weather_tracker.domain.repo.WeatherRepo
import com.saitejajanjirala.weather_tracker.util.Util.mapToSimplifiedWeatherResult
import com.saitejajanjirala.weather_tracker.util.Util.toModel
import com.saitejajanjirala.weather_tracker.util.Util.toProtoModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeatherRepoImpl  @Inject constructor(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
    ): WeatherRepo {
    override suspend fun getWeather(location: String): Flow<Result<SimplifiedWeatherResult>> = flow{
        emit(Result.Loading(true))
        val response = apiService.getWeather(location)
        emit(Result.Loading(false))
        if(response.isSuccessful){
            val weatherResult = response.body()
            if(weatherResult!=null){
                val simplifiedWeatherResult = weatherResult.mapToSimplifiedWeatherResult()
                emit(Result.Success(simplifiedWeatherResult))
            }else{
                emit(Result.Empty())
            }
        }else{
            emit(Result.Error(response.message()))
        }

    }.catch { e->
        emit(Result.Loading(false))
        when (e) {
            is NoInternetException -> emit(Result.Error(e.message ?: "No Internet Connection"))
            else -> emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun getWeather(): Flow<Result<SimplifiedWeatherResult>> = flow{
        emit(Result.Loading(true))
        try {
            dataStoreManager.weatherFlow.collect { protoWeather ->
                val res = protoWeather.toModel()
                if (res.locationName.isNullOrBlank()) {
                    emit(Result.Empty())
                } else {
                    emit(Result.Success(res))
                }
            }
        } catch (e: Exception) {
            emit(Result.Empty())
        }

        emit(Result.Loading(false))
    }

    override suspend fun saveWeatherToLocal(simplifiedWeatherResult: SimplifiedWeatherResult){
        dataStoreManager.saveWeather(simplifiedWeatherResult.toProtoModel())

    }

    override suspend fun getSearchResults(key: String): Flow<Result<List<SearchResultItem>>> =flow<Result<List<SearchResultItem>>> {
        emit(Result.Loading(true))
        val res = apiService.getSearchResults(key)
        emit(Result.Loading(false))
        if(res.isSuccessful) {
            val data = res.body()
            if (!data.isNullOrEmpty()) {
                emit(Result.Success(data))
            } else {
                emit(Result.Empty())
            }
        }
        else{
            emit(Result.Error(res.message()))
        }

    }.catch {
        e->
        when (e) {
            is NoInternetException -> emit(Result.Error(e.message ?: "No Internet Connection"))
            else -> emit(Result.Error(e.message ?: "An unknown error occurred"))
        }
    }
}