package com.example.covidcounter.api

import com.example.covidcounter.callback.CallBackInfo
import com.example.covidcounter.model.Summary
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("summary")
   suspend  fun getSummary(): Summary

    @GET("summary")
    fun getLatestSummary(): Call<Summary?>?
}