package com.example.covidcounter.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class CorUtility {
    companion object{
        const val ALPHABETICAL_ASCENDING = 101
        const val ALPHABETICAL_DESCENDING = 102
        const val TOTAL_ASCENDING = 103
        const val TOTAL_DESCENDING = 104
        const val DEATH_ASCENDING = 105
        const val DEATH_DESCENDING = 106
        const val REC_ASCENDING = 107
        const val REC_DESCENDING = 108

        public fun isLocationPermissionAvailable(context: Context): Boolean {
            val permission3 =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            val permission4 =
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            return (permission3 == PackageManager.PERMISSION_GRANTED || permission4 == PackageManager.PERMISSION_GRANTED)
        }
    }
}