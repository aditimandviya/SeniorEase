package com.seniorease.app.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationResult: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        try {
            val hasFineLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (!hasFineLocation && !hasCoarseLocation) {
                onLocationResult(null)
                return
            }

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationResult(location)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        onLocationResult(lastLoc)
                    }.addOnFailureListener {
                        onLocationResult(null)
                    }
                }
            }.addOnFailureListener {
                onLocationResult(null)
            }
        } catch (e: Exception) {
            onLocationResult(null)
        }
    }
}
