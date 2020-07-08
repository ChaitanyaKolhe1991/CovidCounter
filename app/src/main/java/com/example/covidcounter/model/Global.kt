package com.example.covidcounter.model

import java.io.Serializable

class Global : Serializable {
    var NewConfirmed: Int = 0
    var TotalConfirmed: Int = 0
    var NewDeaths: Int = 0
    var TotalDeaths: Int = 0
    var NewRecovered: Int = 0
    var TotalRecovered: Int = 0
}