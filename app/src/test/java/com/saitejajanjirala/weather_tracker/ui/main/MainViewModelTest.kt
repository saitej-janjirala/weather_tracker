package com.saitejajanjirala.weather_tracker.ui.main

import app.cash.turbine.test
import com.saitejajanjirala.weather_tracker.domain.repo.WeatherRepo
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var weatherRepo: WeatherRepo
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        weatherRepo = mockk(relaxed = true)
        viewModel = MainViewModel(weatherRepo)
    }

    @Test
    fun `initial state is correct`() = runTest {
        assertEquals("", viewModel.searchQuery)
        coEvery { weatherRepo.getWeather() } returns flowOf(Result.Empty())
        viewModel.state.test {
            val first = awaitItem()
            assert(first is Result.Loading && !first.loading)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.isCurrentResult.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search updates state with repository result`() = runTest {
        val mockResult = SimplifiedWeatherResult(
            locationName = "Mumbai",
            tempC = 30.0,
            feelslikeC = 33.0,
            humidity = 70,
            uv = 5.0,
            icon = "icon_url"
        )
        coEvery { weatherRepo.getWeather("Mumbai") } returns flowOf(
            Result.Loading(true),
            Result.Success(mockResult)
        )

        viewModel.onNewSearchQuery("Mumbai")
        viewModel.search()

        viewModel.state.test {
            val first = awaitItem()
            assert(first is Result.Loading && !first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && second.loading)
            val res = awaitItem()
            assert(res is Result.Success)
            assertEquals(mockResult, res.d)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.isCurrentResult.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { weatherRepo.getWeather("Mumbai") }
    }

    @Test
    fun `showSaved resets searchQuery and isCurrentResult`() = runTest {
        viewModel.showSaved()

        assertEquals("", viewModel.searchQuery)
        viewModel.isCurrentResult.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search handles error correctly`() = runTest {
        val errorMessage = "Error fetching weather"
        coEvery { weatherRepo.getWeather("InvalidCity") } returns flowOf(
            Result.Loading(true),
            Result.Error(errorMessage)
        )

        viewModel.onNewSearchQuery("InvalidCity")
        viewModel.search()

        viewModel.state.test {
            val first = awaitItem()
            assert(first is Result.Loading && !first.loading)
            val second = awaitItem()
            assert(second is Result.Loading && second.loading)
            val error = awaitItem()
            assert(error is Result.Error)
            assertEquals(errorMessage, error.m)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.isCurrentResult.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { weatherRepo.getWeather("InvalidCity") }
    }

    @Test
    fun `repository emits Empty result`() = runTest {
        coEvery { weatherRepo.getWeather() } returns flowOf(Result.Empty())

        viewModel = MainViewModel(weatherRepo)

        viewModel.state.test {
            val first = awaitItem()
            assert(first is Result.Loading && !first.loading)
            assert(awaitItem() is Result.Empty)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { weatherRepo.getWeather() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}