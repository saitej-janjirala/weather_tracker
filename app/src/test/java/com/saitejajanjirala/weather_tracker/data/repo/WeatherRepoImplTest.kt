package com.saitejajanjirala.weather_tracker.data.repo

import androidx.datastore.core.CorruptionException
import app.cash.turbine.test
import org.junit.Assert.*
import com.saitejajanjirala.weather_tracker.data.local.DataStoreManager
import com.saitejajanjirala.weather_tracker.data.remote.ApiService
import com.saitejajanjirala.weather_tracker.data.repo.WeatherRepoImpl
import com.saitejajanjirala.weather_tracker.di.NoInternetException
import com.saitejajanjirala.weather_tracker.domain.models.remote.Condition
import com.saitejajanjirala.weather_tracker.domain.models.remote.Current
import com.saitejajanjirala.weather_tracker.domain.models.remote.Location
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import com.saitejajanjirala.weather_tracker.util.Util.toProtoModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import com.saitejajanjirala.weather_tracker.domain.models.remote.WeatherResult
import com.saitejajanjirala.weather_tracker.util.Util
import com.saitejajanjirala.weather_tracker.util.Util.mapToSimplifiedWeatherResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepoImplTest {

    private val apiService: ApiService = mockk()
    private val dataStoreManager: DataStoreManager = mockk()
    private val weatherRepo = WeatherRepoImpl(apiService, dataStoreManager)

    @Before
    fun setUp(){
        Dispatchers.setMain(StandardTestDispatcher())
    }
    @Test
    fun `getWeather() emits success when API call is successful`() = runTest {
        val location = "Mumbai"

        val weatherResult = WeatherResult(
            location = Location(name = "Mumbai"),
            current = Current(tempC = 30.0, feelslikeC = 33.0, humidity = 70, uv = 5.0, condition = Condition(
                icon = "icon_url",
                text = "Sunny",
                code = 1000
            ))
        )
        val simplifiedWeatherResult = weatherResult.mapToSimplifiedWeatherResult()
        coEvery { apiService.getWeather(location) } returns Response.success(weatherResult)
        coEvery { dataStoreManager.saveWeather(any()) } returns Unit

        weatherRepo.getWeather(location).test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            val third = awaitItem()
            assert(third is Result.Success)
            assert(third.d == simplifiedWeatherResult)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { apiService.getWeather(location) }
        coVerify { dataStoreManager.saveWeather(any()) }
    }

    @Test
    fun `getWeather() emits emits loading then Error when No Internet Exception when API call occurs`() = runTest {
        val location = "Mumbai"
        val e = NoInternetException("No internet connection")
        coEvery { apiService.getWeather(location) } throws e

        weatherRepo.getWeather(location).test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            val res = awaitItem()
            assert(res is Result.Error)
            assert(res.m == "No internet connection")
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { apiService.getWeather(location) }
    }
    @Test
    fun `getWeather() emits emits loading then Error when Other Exception when API call occurs`() = runTest {
        val location = "Mumbai"
        val e = Exception("Unexpected Error Occurred")
        coEvery { apiService.getWeather(location) } throws e

        weatherRepo.getWeather(location).test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            val res = awaitItem()
            assert(res is Result.Error)
            assert(res.m == "Unexpected Error Occurred")
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { apiService.getWeather(location) }
    }

    @Test
    fun `getWeather() emits emits loading then Error when API call is Not SuccessFull occurs`()= runTest{
        val location = "mumbai"
        val m = "Api Call Failed"
        coEvery { apiService.getWeather(location) } returns Response.error(
            m.toResponseBody("text/plain".toMediaType()),
            okhttp3.Response.Builder()
                .request(
                    okhttp3.Request.Builder()
                        .url(Util.API_BASE_URL)
                        .build()
                )
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(404)
                .message(m)
                .build()
        )
        weatherRepo.getWeather(location).test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            val res = awaitItem()
            assert(res is Result.Error)
            assert(res.m == m)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { apiService.getWeather(location) }
    }

    @Test
    fun `getWeather() emits emits loading then Empty when API call is SuccessFull But Body is Empty`()= runTest{
        val location = "mumbai"
        coEvery { apiService.getWeather(location) } returns Response.success(null)

        weatherRepo.getWeather(location).test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            val res = awaitItem()
            assert(res is Result.Empty)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { apiService.getWeather(location) }
    }

    @Test
    fun `getWeather() from DataStore emits saved result`() = runTest {
        val savedResult = SimplifiedWeatherResult(
            locationName = "Saved City", tempC = 25.0, feelslikeC = 27.0, humidity = 60, uv = 4.0, icon = "icon_url"
        )
        coEvery { dataStoreManager.weatherFlow } returns flowOf(savedResult.toProtoModel())

        weatherRepo.getWeather().test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            assertEquals(Result.Success(savedResult), awaitItem())
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { dataStoreManager.weatherFlow }
    }

    @Test
    fun `getWeather() from Datastore emits Empty when there is no saved result`() = runTest {
        val e = CorruptionException("No Saved City.")
        coEvery { dataStoreManager.weatherFlow } throws e
        weatherRepo.getWeather().test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            assert(awaitItem() is Result.Empty)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            cancelAndIgnoreRemainingEvents()
            coVerify { dataStoreManager.weatherFlow }
        }
    }

    @Test
    fun `getWeather() from Datastore emits Empty when data doesn't have name`() = runTest {
        val savedResult = SimplifiedWeatherResult(
            locationName = null, tempC = 0.0, feelslikeC = 0.0, humidity = 0, uv = 0.0, icon = ""
        )
        coEvery { dataStoreManager.weatherFlow } returns flowOf(savedResult.toProtoModel())
        weatherRepo.getWeather().test {
            val first = awaitItem()
            assert(first is Result.Loading && first.loading)
            assert(awaitItem() is Result.Empty)
            val second = awaitItem()
            assert(second is Result.Loading && !second.loading)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { dataStoreManager.weatherFlow }
    }

    @After
    fun TearDown(){
        Dispatchers.resetMain()
    }
}