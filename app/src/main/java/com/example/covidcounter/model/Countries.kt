package com.example.covidcounter.model

import android.util.Log
import java.io.Serializable

class Countries : Serializable {

    public var Country: String = ""
    public var CountryCode: String = ""
    public var Slug: String = ""
    public var NewConfirmed: Int = 0
    public var TotalConfirmed: Int = 0
    public var NewDeaths: Int = 0
    public var TotalDeaths: Int = 0
    public var NewRecovered: Int = 0
    public var TotalRecovered: Int = 0
    public var Date: String = ""

    override fun equals(other: Any?): Boolean {
//        return super.equals(other)
        var retVal = false
        if (other is Countries) {
            val ptr: Countries = other as Countries
            retVal = ptr.CountryCode.equals(this.CountryCode)
        }
        return retVal
    }

    override fun hashCode(): Int {
//        return super.hashCode()
        var hash = 7
        hash = 17 * hash + if (this.CountryCode != null) this.CountryCode.hashCode() else 0
        return hash
    }
    override fun toString(): String {
        return super.toString()
    }
}