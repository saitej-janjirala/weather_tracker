package com.saitejajanjirala.weather_tracker.domain.models.remote


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResultItem(
    @Json(name = "country")
    val country: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "lat")
    val lat: Double? = null,
    @Json(name = "lon")
    val lon: Double? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "region")
    val region: String? = null,
    @Json(name = "url")
    val url: String? = null
)