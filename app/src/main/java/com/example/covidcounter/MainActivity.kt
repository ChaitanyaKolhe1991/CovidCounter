package com.example.covidcounter

import ApiHelper
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.covidcounter.adapter.MyItemRecyclerViewAdapter
import com.example.covidcounter.api.RetrofitBuilder
import com.example.covidcounter.model.Countries
import com.example.covidcounter.model.Global
import com.example.covidcounter.utils.Status
import com.example.covidcounter.viewmodel.MainViewModel
import com.example.covidcounter.viewmodel.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    lateinit var viewTotalRecover: View
    lateinit var viewTotalDeath: View
    lateinit var viewTotal: View
    lateinit var viewLatest: View

    lateinit var textTRec: TextView
    lateinit var textTRecN: TextView

    lateinit var textTDeath: TextView
    lateinit var textTDeathN: TextView

    lateinit var textTAffect: TextView
    lateinit var textTAffectN: TextView
    lateinit var itemAdapter: MyItemRecyclerViewAdapter
    private lateinit var viewModel: MainViewModel
    var countryList = mutableListOf<Countries>()

    lateinit var fabFilter: FloatingActionButton
    lateinit var fabSort: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initializeView()
        manageFab()
        setupViewModel()
        setUpRecyclerView()
        setupObservers()
    }

    private fun setUpRecyclerView() {

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
//            setItemViewCacheSize(30)
            itemAdapter =
                MyItemRecyclerViewAdapter(
                    countryList
                )
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    (recyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
            adapter = itemAdapter
            itemAdapter.notifyDataSetChanged()

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        fabSort.hide()
                        fabFilter.hide()
                    } else if (dy < 0) {
                        fabSort.show()
                        fabFilter.show()
                    }
                }
            })
        }

    }
    /* fun hideFabButtons() {
         layoutFabButton.animate()
             .translationY(layoutFabButton.getHeight().toFloat())
             .setListener(object : AnimatorListenerAdapter() {
                 override fun onAnimationEnd(animation: Animator) {
                     super.onAnimationEnd(animation)
                     layoutFabButton.setVisibility(View.GONE)
                 }
             })
     }

     fun showFabButtons() {
         layoutFabButton.setVisibility(View.VISIBLE)
         layoutFabButton.animate()
             .translationY(0f)
             .setListener(object : AnimatorListenerAdapter() {
                 override fun onAnimationEnd(animation: Animator) {
                     super.onAnimationEnd(animation)
                     layoutFabButton.setVisibility(View.VISIBLE)
                 }
             })
     }*/

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(MainViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.getSummary.observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        recyclerView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        resource.data?.let { users ->
                            retrieveGlobal(users.global)
                            retrieveCountryList(users.listCountries)
                        }
                    }
                    Status.ERROR -> {
                        recyclerView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        Log.e("Main Activity ", "it.message " + it.message)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun retrieveGlobal(global: Global) {

        viewTotalRecover.findViewById<TextView>(R.id.text_total).text =
            global.TotalRecovered.toString()
        viewTotalDeath.findViewById<TextView>(R.id.text_total).text = global.TotalDeaths.toString()
        viewTotal.findViewById<TextView>(R.id.text_total).text = global.TotalConfirmed.toString()
        viewLatest.findViewById<TextView>(R.id.text_total).text = global.TotalRecovered.toString()

        viewTotalRecover.findViewById<TextView>(R.id.text_new).text =
            "+" + global.NewRecovered.toString()
        viewTotalDeath.findViewById<TextView>(R.id.text_new).text =
            "+" + global.NewDeaths.toString()
        viewTotal.findViewById<TextView>(R.id.text_new).text = "+" + global.NewConfirmed.toString()
//        viewLatest.findViewById<TextView>(R.id.text_new).text =  "+"+global.NewRecovered.toString()
    }

    private fun retrieveCountryList(users: List<Countries>) {
        Log.d("Countries ", " Countries--- " + users.size)
        countryList.clear()
        countryList.addAll(users.filter { countries -> countries.TotalConfirmed!=0 })
//        countryList = users //.filter { countries -> countries.TotalConfirmed==0 }
        itemAdapter.notifyDataSetChanged()
    }


    private fun manageFab() {
        fabFilter.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", View.OnClickListener {

                }).show()
        }
        fabSort.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

        }
    }

    private fun initializeView() {
        viewTotalRecover = findViewById(R.id.card_total_recover)
        viewTotalDeath = findViewById(R.id.card_total_death)
        viewTotal = findViewById(R.id.card_total)
        viewLatest = findViewById(R.id.card_last_update)

        fabFilter = findViewById(R.id.fab_filter)
        fabSort = findViewById(R.id.fab_sort)

        viewTotalRecover.findViewById<TextView>(R.id.text_title).text = "Total Recovered"
        viewTotalDeath.findViewById<TextView>(R.id.text_title).text = "Total Death"
        viewTotal.findViewById<TextView>(R.id.text_title).text = "Total Affected"
        viewLatest.findViewById<TextView>(R.id.text_title).text = "Last Updated"



        viewTotalRecover.findViewById<CardView>(R.id.card_total_recover)
            .setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        viewTotalDeath.findViewById<CardView>(R.id.card_total_death)
            .setCardBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        viewTotal.findViewById<CardView>(R.id.card_total)
            .setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        viewLatest.findViewById<CardView>(R.id.card_last_update)
            .setCardBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        viewLatest.findViewById<TextView>(R.id.text_new).visibility = View.GONE

    }
}