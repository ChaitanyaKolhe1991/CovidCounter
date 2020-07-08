package com.example.covidcounter.model

import com.google.gson.annotations.SerializedName


data class Summary(
    @SerializedName("Global")
    var global: Global,
    @SerializedName("Countries")
    var listCountries: List<Countries>

)