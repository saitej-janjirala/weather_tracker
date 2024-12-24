package com.saitejajanjirala.weather_tracker.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.saitejajanjirala.weather_tracker.R
import com.saitejajanjirala.weather_tracker.domain.models.remote.SearchResultItem
import com.saitejajanjirala.weather_tracker.domain.models.util.Result
import com.saitejajanjirala.weather_tracker.domain.models.util.SimplifiedWeatherResult
import com.saitejajanjirala.weather_tracker.ui.theme.Weather_trackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Weather_trackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { innerPadding ->
                        MainScreen(viewModel, Modifier.padding(innerPadding))
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel,modifier: Modifier){

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val state by viewModel.state.collectAsState()
        val searchCities by viewModel.searchItems.collectAsState()
        val query by viewModel._searchQuery.collectAsState()
        val currentResult by viewModel.isCurrentResult.collectAsState()
        val focusManager = LocalFocusManager.current
        CustomSearchBar(viewModel, focusManager)
        Spacer(modifier = Modifier.height(16.dp))
        if(query.isNotEmpty()) {
            SearchCities(searchCities, viewModel)
        }
        when(state){
            is Result.Empty -> {
                NoCitySelected()
            }
            is Result.Error -> {
                var message = state.m
                if(message.isNullOrBlank()){
                    message = "Unknown Error Try Again"
                }
                Text(
                    text = message,
                    fontSize = 18.sp,
                    color = Color.Red,
                )
            }
            is Result.Loading ->{
                if(state.loading){
                    CircularProgressIndicator()
                }
            }
            is Result.Success -> {
                state.d?.let {
                    if(currentResult){
                        CurrentWeatherCard(it){
                            viewModel.showSaved()
                        }
                    }else{
                        SavedWeatherCard(it)
                    }
                }
            }
        }

    }
}


@Composable
fun SearchCities(searchCities : Result<List<SearchResultItem>>,viewModel: MainViewModel){

    when(searchCities){
        is Result.Empty -> {
            Text(text = "No Results Found")
        }
        is Result.Error -> {
            Text(text = searchCities.m?:"Unknown error occured")
        }
        is Result.Loading -> {
            if(searchCities.loading) {
                CircularProgressIndicator()
            }
        }
        is Result.Success -> {
            LazyColumn {
                items(searchCities.data){searchItem->
                    Column (Modifier.padding(8.dp).clickable {
                        viewModel.search(searchItem.name?:"a")
                    }){
                        Text(text = searchItem.name?:"N/A", fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(text = searchItem.country?:"N/A", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}



@Composable
fun SavedWeatherCard(weatherResult: SimplifiedWeatherResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        weatherResult.icon?.let { iconUrl ->
            AsyncImage(
                model = "https:$iconUrl",
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = weatherResult.locationName ?: "Unknown City",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = Typography().titleLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.nav_icon),
                contentDescription = "Navigation Icon",
                modifier = Modifier
                    .size(30.dp)
                    .padding(top = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${weatherResult.tempC?.toInt()}°",
            fontSize = 70.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            style = Typography().titleLarge
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F3F3)),
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailedItems("Humidity", "${weatherResult.humidity}%")
                DetailedItems("UV", "${weatherResult.uv} km/h")
                DetailedItems("Feels Like", "${weatherResult.feelslikeC?.toInt()}°")
            }
        }
    }
}

@Composable
fun DetailedItems(text1 :String, text2 : String){
    Column (horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            text = text1,
            fontSize = 12.sp,
            color = Color(0xFFBDB9B9)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = text2,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun CurrentWeatherCard(weatherResult: SimplifiedWeatherResult,onCurrentClicked:()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F3F3))
            .clickable {
                onCurrentClicked()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = weatherResult.locationName ?: "Unknown City",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${weatherResult.tempC?.toInt()}°",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        weatherResult.icon?.let { iconUrl ->
            AsyncImage(
                model = "https:$iconUrl",
                contentDescription = "Weather Icon",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                , alignment = Alignment.TopEnd
            )
        }
    }
}

@Composable
fun NoCitySelected(){
    Column (Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Text(
            text = "No City Selected",
            fontSize = 28.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please Search For A City",
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }

}

@Composable
fun CustomSearchBar(viewModel: MainViewModel, focusManager: FocusManager) {
    val searchQuery by  viewModel._searchQuery.collectAsState()
    Spacer(Modifier.height(24.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchText ->
                    viewModel.onNewSearchQuery(
                        searchText
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.Gray,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
//                        viewModel.search()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Search Location", color = Color.LightGray)
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                }
            )
        }
    }
}
