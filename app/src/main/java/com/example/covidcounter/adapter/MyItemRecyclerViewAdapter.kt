package com.example.covidcounter.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.covidcounter.R
import com.example.covidcounter.databinding.CountryDataBinding
import com.example.covidcounter.model.Countries
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MyItemRecyclerViewAdapter(
    val context: Context,
    private val values: MutableList<Countries>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {
    private var countryCode = "";
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        item.Date = getConvertedDate(item.Date)
        holder.bindData(item)
        if (!!TextUtils.isEmpty(countryCode) && position == 0) {
            holder.layoutContainer.setBackgroundColor(context.resources.getColor(android.R.color.darker_gray))
        } else {
            holder.layoutContainer.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }
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

    fun setCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutContainer: LinearLayout = view.findViewById(R.id.layout_container)
        private val countryDataBinding: CountryDataBinding = DataBindingUtil.bind(view)!!
        fun bindData(countries: Countries) {
            countryDataBinding.countries = countries
        }
    }

}