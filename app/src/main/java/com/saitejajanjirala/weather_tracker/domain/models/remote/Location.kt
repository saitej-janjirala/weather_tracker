package com.saitejajanjirala.weather_tracker.domain.models.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(
    @Json(name = "country")
    val country: String? = null,
    @Json(name = "name")
    val name: String? = null,
)