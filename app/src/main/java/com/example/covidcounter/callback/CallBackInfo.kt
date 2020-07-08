package com.example.covidcounter.callback

import com.example.covidcounter.model.Countries
import com.example.covidcounter.model.Global
import com.google.gson.annotations.SerializedName
import java.io.Serializable

public class CallBackInfo : Serializable {

    @SerializedName(value = "Global")
    public var global: Global? = null

    @SerializedName(value = "Countries")
    public var countriesList: List<Countries>? = null
}