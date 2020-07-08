package com.example.covidcounter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.covidcounter.model.Summary
import com.example.covidcounter.repository.MainRepository
import com.example.covidcounter.utils.Resource
import kotlinx.coroutines.Dispatchers


class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {
        lateinit var summary: Summary
    val getSummary = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getSummary()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

}