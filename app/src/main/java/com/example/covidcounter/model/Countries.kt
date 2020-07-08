package com.example.covidcounter.model

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.io.Serializable

class Countries : Serializable {

    public val Country: String = ""
    public val CountryCode: String = ""
    public val Slug: String = ""
    public val NewConfirmed: Int = 0
    public val TotalConfirmed: Int = 0
    public val NewDeaths: Int = 0
    public val TotalDeaths: Int = 0
    public val NewRecovered: Int = 0
    public val TotalRecovered: Int = 0
    public var Date: String = ""

}