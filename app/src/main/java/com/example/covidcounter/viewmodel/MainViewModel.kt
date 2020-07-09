package com.example.covidcounter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.covidcounter.api.RetrofitBuilder
import com.example.covidcounter.model.Summary
import com.example.covidcounter.repository.MainRepository
import com.example.covidcounter.utils.Resource
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModel(application: Application, private val mainRepository: MainRepository) : AndroidViewModel(application) {
    public var summary = MutableLiveData<Summary>()
    val getSummary = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getSummary()))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

    fun getLatestSummary(): MutableLiveData<Summary> {
        return summary
    }

    fun fetchLatestData() {
        val callbackSumCall: Call<Summary?>? = RetrofitBuilder.apiService.getLatestSummary()
        callbackSumCall!!.enqueue(object : Callback<Summary?> {
            override fun onResponse(call: Call<Summary?>, response: Response<Summary?>) {
                summary.postValue(response.body())
            }

            override fun onFailure(call: Call<Summary?>, t: Throwable) {

            }
        })
    }
}