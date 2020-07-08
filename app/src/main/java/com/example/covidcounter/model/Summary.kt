package com.example.covidcounter.model

import com.google.gson.annotations.SerializedName


public data class Summary(
    @SerializedName("Global")
   public var global: Global,
    @SerializedName("Countries")
   public var listCountries: List<Countries>

)