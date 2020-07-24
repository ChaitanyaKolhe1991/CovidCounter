package com.example.covidcounter

import ApiHelper
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.covidcounter.adapter.MyItemRecyclerViewAdapter
import com.example.covidcounter.api.RetrofitBuilder
import com.example.covidcounter.model.Countries
import com.example.covidcounter.model.Global
import com.example.covidcounter.model.Summary
import com.example.covidcounter.utils.CorUtility
import com.example.covidcounter.utils.Status
import com.example.covidcounter.viewmodel.MainViewModel
import com.example.covidcounter.viewmodel.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {

    //Views
    lateinit var viewTotalRecover: View
    lateinit var viewTotalDeath: View
    lateinit var viewTotal: View
    lateinit var viewLatest: View

    lateinit var fabFilter: FloatingActionButton
    lateinit var fabSort: FloatingActionButton

    lateinit var itemAdapter: MyItemRecyclerViewAdapter

    private lateinit var viewModel: MainViewModel
    var countryList = mutableListOf<Countries>()
    var originalList = mutableListOf<Countries>()
    val output = SimpleDateFormat("ddMMMyy HH:mm:ss aa")

    //Get Location
    var lastKnownLocation: Location? = null

    //Sorting

    var tglBtnAlphabeticalUp: ToggleButton? = null
    var tglBtnAlphabeticalDown: ToggleButton? = null
    var tglBtnTotalUp: ToggleButton? = null
    var tglBtnTotalDown: ToggleButton? = null
    var tglBtnDeathUp: ToggleButton? = null
    var tglBtnDeathDown: ToggleButton? = null
    var tglBtnRecUp: ToggleButton? = null
    var tglBtnRecDown: ToggleButton? = null
    var sortAlertDialog: AlertDialog? = null

    //Default Sorting
    var sortId = CorUtility.TOTAL_DESCENDING

    //Highlite Current Country
    lateinit var currentCountry: Countries

    //Call Every 2 min
    val dayInMillis: Long = 1000 * 60 * 2
    lateinit var mainHandler: Handler

    //Filter
    val items = listOf("Total Cases", "Deaths", "Recovered")

    var selectedPos = 0
    var greaterThan = 0
    var lessThan = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        initializeView()
        manageFab()
        setupViewModel()
        setUpRecyclerView()
        setupObservers()
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.fetchLatestData()
            }
            R.id.action_clear_filter -> {
                clearAllFilter()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Iniitalize All views
    private fun initializeView() {
        viewTotalRecover = findViewById(R.id.card_total_recover)
        viewTotalDeath = findViewById(R.id.card_total_death)
        viewTotal = findViewById(R.id.card_total)
        viewLatest = findViewById(R.id.card_last_update)

        fabFilter = findViewById(R.id.fab_filter)
        fabSort = findViewById(R.id.fab_sort)

        viewTotalRecover.findViewById<TextView>(R.id.text_title).text =
            getString(R.string.str_Total)
        viewTotalDeath.findViewById<TextView>(R.id.text_title).text = getString(R.string.str_death)
        viewTotal.findViewById<TextView>(R.id.text_title).text = getString(R.string.str_affected)
        viewLatest.findViewById<TextView>(R.id.text_title).text = getString(R.string.str_update)


        viewTotalRecover.findViewById<CardView>(R.id.card_total_recover)
            .setCardBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
        viewTotalDeath.findViewById<CardView>(R.id.card_total_death)
            .setCardBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
        viewTotal.findViewById<CardView>(R.id.card_total)
            .setCardBackgroundColor(resources.getColor(R.color.colorAccent))
        viewLatest.findViewById<CardView>(R.id.card_last_update)
            .setCardBackgroundColor(resources.getColor(R.color.colorPrimaryDark))

        viewLatest.findViewById<TextView>(R.id.text_new).visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = getWindow()
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = resources.getColor(R.color.colorPrimary)
                navigationBarColor = resources.getColor(R.color.colorPrimary)
            }
        }
    }

    private fun setUpRecyclerView() {

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
//            setItemViewCacheSize(30)
            itemAdapter =
                MyItemRecyclerViewAdapter(
                    context, countryList
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

    //Viewmodel - Initialize and set observrs
    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(application, ApiHelper(RetrofitBuilder.apiService))
        ).get(MainViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.getSummary.observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        recyclerView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        textNoData.visibility = View.GONE
                        resource.data?.let { summary ->
                            bindSummary(summary)
                        }
                    }
                    Status.ERROR -> {
                        recyclerView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                        textNoData.visibility = View.VISIBLE
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        Log.e("Main Activity ", "it.message " + it.message)
                    }
                    Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        textNoData.visibility = View.GONE
                    }
                }
            }
        })

        viewModel.getLatestSummary().observe(this, Observer { summary ->
            bindSummary(summary)
        })
    }

    fun bindSummary(summary: Summary) {
        setFilterAndSortList(selectedPos, greaterThan, lessThan, summary.listCountries)
        retrieveGlobal(summary.global)
        originalList = summary.listCountries as MutableList<Countries>
    }

    //set global 
    private fun retrieveGlobal(global: Global) {

        viewTotalRecover.findViewById<TextView>(R.id.text_total).text =
            global.TotalRecovered.toString()
        viewTotalDeath.findViewById<TextView>(R.id.text_total).text = global.TotalDeaths.toString()
        viewTotal.findViewById<TextView>(R.id.text_total).text = global.TotalConfirmed.toString()

        viewLatest.findViewById<TextView>(R.id.text_total).text =
            output.format(Calendar.getInstance().time)

        viewTotalRecover.findViewById<TextView>(R.id.text_new).text =
            "+" + global.NewRecovered.toString()
        viewTotalDeath.findViewById<TextView>(R.id.text_new).text =
            "+" + global.NewDeaths.toString()
        viewTotal.findViewById<TextView>(R.id.text_new).text = "+" + global.NewConfirmed.toString()
//        viewLatest.findViewById<TextView>(R.id.text_new).text =  "+"+global.NewRecovered.toString()
    }

    private fun setSortedList(countryList: List<Countries>) {
        var sortedAppsList = countryList.sortedBy { it.Country }
        if (sortId == CorUtility.ALPHABETICAL_ASCENDING) {
            sortedAppsList = countryList.sortedBy { it.Country }
        } else if (sortId == CorUtility.ALPHABETICAL_DESCENDING) {
            sortedAppsList = countryList.sortedByDescending { it.Country }

        } else if (sortId == CorUtility.TOTAL_ASCENDING) {
            sortedAppsList = countryList.sortedBy { it.TotalConfirmed }

        } else if (sortId == CorUtility.TOTAL_DESCENDING) {
            sortedAppsList = countryList.sortedByDescending { it.TotalConfirmed }

        } else if (sortId == CorUtility.DEATH_ASCENDING) {
            sortedAppsList = countryList.sortedBy { it.TotalDeaths }

        } else if (sortId == CorUtility.DEATH_DESCENDING) {
            sortedAppsList = countryList.sortedByDescending { it.TotalDeaths }

        } else if (sortId == CorUtility.REC_ASCENDING) {
            sortedAppsList = countryList.sortedBy { it.TotalRecovered }

        } else if (sortId == CorUtility.REC_DESCENDING) {
            sortedAppsList = countryList.sortedByDescending { it.TotalRecovered }

        }
        setCountryListToAdapter(sortedAppsList)

    }

    private fun setCountryListToAdapter(users: List<Countries>) {

        countryList.clear()
        countryList.addAll(users.filter { countries -> countries.TotalConfirmed != 0 })

        if (this::currentCountry.isInitialized && countryList.contains(currentCountry)) {
            val index = countryList.indexOf(currentCountry)
            currentCountry = countryList.get(index)
            countryList.removeAt(index)
            countryList[0] = currentCountry
        }

        itemAdapter.notifyDataSetChanged()
        if (countryList.size == 0)
            textNoData.visibility = View.VISIBLE else textNoData.visibility = View.GONE
    }


    private fun manageFab() {
        fabFilter.setOnClickListener { view ->
            if (countryList.size > 0)
                showFilterDialog()
            else
                Snackbar.make(
                    view,
                    "Sorry , Some error occurred . Please try again lated",
                    Snackbar.LENGTH_SHORT
                ).show()

        }
        fabSort.setOnClickListener { view ->
            if (countryList.size > 0)
                showSortByDialog()
            else
                Snackbar.make(
                    view,
                    "Sorry , Some error occurred . Please try again lated",
                    Snackbar.LENGTH_SHORT
                ).show()

        }
    }

    override fun onResume() {
        super.onResume()
        mainHandler.postDelayed(updateSummaryTask, dayInMillis)

        if (CorUtility.isLocationPermissionAvailable(context = this)) {
            getDeviceLastKnownLocation()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    fun getDeviceLastKnownLocation() {
        val mLocationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers =
            mLocationManager.getProviders(true)
        for (provider in providers) {
            try {
                val l =
                    mLocationManager.getLastKnownLocation(provider) ?: continue
                if (lastKnownLocation == null || l.accuracy > lastKnownLocation!!.getAccuracy()) {
                    lastKnownLocation = l
                }
            } catch (e: SecurityException) {
            }
        }
        if (null != lastKnownLocation) {
            try {
                val mGeocoder =
                    Geocoder(this, Locale.getDefault())

                val addresses: List<Address> =
                    mGeocoder.getFromLocation(
                        lastKnownLocation!!.latitude,
                        lastKnownLocation!!.longitude, 1
                    )
                if (addresses != null && addresses.size > 0 && itemAdapter != null) {

                    itemAdapter.setCountryCode(addresses[0].countryCode)
                    Log.e(
                        "Main Activity",
                        "Location " + addresses[0].countryCode + "//"
                                + addresses[0].countryName + "//"
                    )
                    currentCountry = Countries().apply {
                        Country = addresses[0].countryName
                        CountryCode = addresses[0].countryCode
                    }

                    if (this::currentCountry.isInitialized && countryList.contains(currentCountry)) {
                        val index = countryList.indexOf(currentCountry)
                        currentCountry = countryList.get(index)
                        countryList.removeAt(index)
                        countryList[0] = currentCountry
                        itemAdapter.notifyItemRemoved(index)
                        itemAdapter.notifyItemChanged(0)
                    }


                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    private fun showFilterDialog() {
        val alertDialogBuilder =
            AlertDialog.Builder(this)
        val view: View =
            getLayoutInflater().inflate(R.layout.layout_filter, null, false)
        alertDialogBuilder.setView(view)


        val arrayAdapter = ArrayAdapter(this@MainActivity, R.layout.layout_spinner_text, items)

        var spinner = view.findViewById<Spinner>(R.id.spinner)
        var txtGreater = view.findViewById<TextInputEditText>(R.id.edit_greater)
        var txtLess = view.findViewById<TextInputEditText>(R.id.edit_less)
        spinner?.adapter = arrayAdapter

        spinner.setSelection(selectedPos)

        if (greaterThan != 0)
            txtGreater.setText(greaterThan.toString())

        if (lessThan != 0)
            txtLess.setText(lessThan.toString())

        alertDialogBuilder.setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int ->
            sortAlertDialog!!.dismiss()
        }
        alertDialogBuilder.setPositiveButton(resources.getString(R.string.str_ok)) { dialogInterface: DialogInterface, i: Int ->
            val grater =
                if (!TextUtils.isEmpty(txtGreater?.text.toString())) txtGreater?.text.toString()
                    .toInt() else 0

            val less =
                if (!TextUtils.isEmpty(txtLess?.text.toString())) txtLess?.text.toString()
                    .toInt() else 0

            setFilterAndSortList(spinner.selectedItemPosition, grater, less, originalList)
        }
        alertDialogBuilder.setNegativeButton(resources.getString(R.string.str_clear)) { dialogInterface: DialogInterface, i: Int ->
            clearAllFilter()
        }

        sortAlertDialog = alertDialogBuilder.create()

        sortAlertDialog!!.show()
    }

    private fun clearAllFilter() {
        selectedPos = 0
        greaterThan = 0
        lessThan = 0
        setSortedList(originalList)
    }

    private fun setFilterAndSortList(
        selectedItemPosition: Int,
        grater: Int,
        less: Int,
        countryList: List<Countries>
    ) {

        val filterList = mutableListOf<Countries>()
        Log.e("Main ", "originalList " + countryList.size)

        selectedPos = selectedItemPosition
        greaterThan = grater
        lessThan = less

        if (selectedItemPosition == 0) {
// TotalConfirmed
            if (grater == 0 && less == 0) { //No filter
                filterList.addAll(countryList)
            } else if (grater != 0 && less == 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalConfirmed >= grater
                })
            } else if (grater == 0 && less != 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalConfirmed <= less
                })
            } else {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalConfirmed in grater..less
                })
            }
        } else if (selectedItemPosition == 1) {
// TotalDeaths
            if (grater == 0 && less == 0) {//No filter
                filterList.addAll(countryList)
            } else if (grater != 0 && less == 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalDeaths >= grater
                })
            } else if (grater == 0 && less != 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalDeaths <= less
                })
            } else {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalDeaths in grater..less
                })
            }
        } else {
            //TotalRecovered
            if (grater == 0 && less == 0) {//No filter
                filterList.addAll(countryList)
            } else if (grater != 0 && less == 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalRecovered >= grater
                })
            } else if (grater == 0 && less != 0) {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalRecovered <= less
                })
            } else {
                filterList.addAll(originalList.filter { countries ->
                    countries.TotalRecovered in grater..less
                })
            }
        }

        Log.e("Main ", "after originalList " + originalList.size)
        Log.e("Main ", "after filterList " + filterList.size)
        setSortedList(filterList)

    }


    private fun showSortByDialog() {
        val alertDialogBuilder =
            AlertDialog.Builder(this)
        val view: View =
            getLayoutInflater().inflate(R.layout.layout_sort, null, false)
        alertDialogBuilder.setView(view)


        alertDialogBuilder.setNeutralButton(resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int ->
            sortAlertDialog?.dismiss()
        }
        alertDialogBuilder.setPositiveButton(resources.getString(R.string.str_ok)) { dialogInterface: DialogInterface, i: Int ->
            sortOkClick()
        }

        sortAlertDialog = alertDialogBuilder.create()
        initializeDialogControls(view)
        setInitialToggleSelection()
        sortAlertDialog!!.show()
    }


    fun sortOkClick() {
        if (anythingChecked()) {
            var sortedAppsList = countryList.sortedBy { it.Country }
            if (tglBtnAlphabeticalUp!!.isChecked) {
                sortId = CorUtility.ALPHABETICAL_ASCENDING
                sortedAppsList = countryList.sortedBy { it.Country }
            } else if (tglBtnAlphabeticalDown!!.isChecked) {
                sortId = CorUtility.ALPHABETICAL_DESCENDING
                sortedAppsList = countryList.sortedByDescending { it.Country }
            } else if (tglBtnTotalUp!!.isChecked) {
                sortId = CorUtility.TOTAL_ASCENDING
                sortedAppsList = countryList.sortedBy { it.TotalConfirmed }

            } else if (tglBtnTotalDown!!.isChecked) {
                sortId = CorUtility.TOTAL_DESCENDING
                sortedAppsList = countryList.sortedByDescending { it.TotalConfirmed }

            } else if (tglBtnDeathUp!!.isChecked) {
                sortId = CorUtility.DEATH_ASCENDING
                sortedAppsList = countryList.sortedBy { it.TotalDeaths }

            } else if (tglBtnDeathDown!!.isChecked) {
                sortId = CorUtility.DEATH_DESCENDING
                sortedAppsList = countryList.sortedByDescending { it.TotalDeaths }

            } else if (tglBtnRecUp!!.isChecked) {
                sortId = CorUtility.REC_ASCENDING
                sortedAppsList = countryList.sortedBy { it.TotalRecovered }

            } else if (tglBtnRecDown!!.isChecked) {
                sortId = CorUtility.REC_DESCENDING
                sortedAppsList = countryList.sortedByDescending { it.TotalRecovered }
            }
            setCountryListToAdapter(sortedAppsList)
        } else {
            Toast.makeText(
                this@MainActivity,
                "You haven't selected any sorting method",
                Toast.LENGTH_SHORT
            ).show()
        }
        sortAlertDialog!!.dismiss()
    }

    private fun initializeDialogControls(view: View) {
        tglBtnAlphabeticalUp =
            view.findViewById<View>(R.id.toggle_btn_alphabetical_up) as ToggleButton
        tglBtnAlphabeticalDown =
            view.findViewById<View>(R.id.toggle_btn_alphabetical_down) as ToggleButton
        tglBtnTotalUp =
            view.findViewById<View>(R.id.toggle_btn_total_up) as ToggleButton
        tglBtnTotalDown =
            view.findViewById<View>(R.id.toggle_btn_total_down) as ToggleButton
        tglBtnDeathUp =
            view.findViewById<View>(R.id.toggle_btn_death_up) as ToggleButton
        tglBtnDeathDown =
            view.findViewById<View>(R.id.toggle_btn_death_down) as ToggleButton
        tglBtnRecUp =
            view.findViewById<View>(R.id.toggle_btn_rec_up) as ToggleButton
        tglBtnRecDown =
            view.findViewById<View>(R.id.toggle_btn_rec_down) as ToggleButton

        tglBtnAlphabeticalUp!!.setOnCheckedChangeListener(this)
        tglBtnAlphabeticalDown!!.setOnCheckedChangeListener(this)
        tglBtnTotalUp!!.setOnCheckedChangeListener(this)
        tglBtnTotalDown!!.setOnCheckedChangeListener(this)
        tglBtnDeathUp!!.setOnCheckedChangeListener(this)
        tglBtnDeathDown!!.setOnCheckedChangeListener(this)
        tglBtnRecUp!!.setOnCheckedChangeListener(this)
        tglBtnRecDown!!.setOnCheckedChangeListener(this)
    }

    private fun setInitialToggleSelection() {
        var buttonView: CompoundButton? = null

        if (sortId == CorUtility.ALPHABETICAL_ASCENDING) {
            buttonView = tglBtnAlphabeticalUp
        } else if (sortId == CorUtility.ALPHABETICAL_DESCENDING) {
            buttonView = tglBtnAlphabeticalDown
        } else if (sortId == CorUtility.TOTAL_ASCENDING) {
            buttonView = tglBtnTotalUp
        } else if (sortId == CorUtility.TOTAL_DESCENDING) {
            buttonView = tglBtnTotalDown
        } else if (sortId == CorUtility.DEATH_ASCENDING) {
            buttonView = tglBtnDeathUp
        } else if (sortId == CorUtility.DEATH_DESCENDING) {
            buttonView = tglBtnDeathDown
        } else if (sortId == CorUtility.REC_ASCENDING) {
            buttonView = tglBtnRecUp
        } else if (sortId == CorUtility.REC_DESCENDING) {
            buttonView = tglBtnRecDown
        }

        onCheckedChanged(buttonView, true)
    }

    private fun anythingChecked(): Boolean {
        return tglBtnAlphabeticalUp!!.isChecked ||
                tglBtnAlphabeticalDown!!.isChecked ||
                tglBtnTotalUp!!.isChecked ||
                tglBtnTotalDown!!.isChecked ||
                tglBtnDeathUp!!.isChecked ||
                tglBtnDeathDown!!.isChecked ||
                tglBtnRecUp!!.isChecked ||
                tglBtnRecDown!!.isChecked
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            tglBtnAlphabeticalUp!!.isChecked = false
            tglBtnAlphabeticalDown!!.isChecked = false
            tglBtnTotalUp!!.isChecked = false
            tglBtnTotalDown!!.isChecked = false
            tglBtnDeathUp!!.isChecked = false
            tglBtnDeathDown!!.isChecked = false
            tglBtnRecUp!!.isChecked = false
            tglBtnRecDown!!.isChecked = false
            buttonView!!.isChecked = true
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_sort -> if (itemAdapter != null && itemAdapter.itemCount > 0) showSortByDialog()

        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateSummaryTask)
    }

    private val updateSummaryTask = object : Runnable {
        override fun run() {
            viewModel.fetchLatestData()
            mainHandler.postDelayed(this, dayInMillis)
        }
    }
}
