package com.example.covidcounter.repository

import ApiHelper


class MainRepository(private val apiHelper: ApiHelper) {
    suspend fun getSummary() = apiHelper.getSummary()
}