package com.example.covidcounter.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.covidcounter.R
import com.example.covidcounter.databinding.CountryDataBinding
import com.example.covidcounter.model.Countries
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MyItemRecyclerViewAdapter(
    private val values: MutableList<Countries>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        Log.e("Adapter", item.TotalConfirmed.toString())
        item.Date = getConvertedDate(item.Date)
        holder.bindData(item)
    }

    fun getConvertedDate(date: String): String {
        val input = SimpleDateFormat("yyyyy-MM-dd'T'HH:mm:ss")
        val output = SimpleDateFormat("ddMMMyy HH:mm")

        var d: Date? = null
        try {
            d = input.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return date
        }
        return output.format(d)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val countryDataBinding: CountryDataBinding = DataBindingUtil.bind(view)!!
        fun bindData(countries: Countries) {
            countryDataBinding.countries = countries
        }
    }

}