package com.example.covidcounter.api

import com.example.covidcounter.model.Countries
import com.example.covidcounter.model.Summary
import io.reactivex.Single
import retrofit2.http.GET

interface ApiService {

    @GET("summary")
   suspend  fun getSummary(): Summary

}