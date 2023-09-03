package com.example.cheapsleep.data

import android.location.Location

interface ILocationClient {
    fun onNewLocation(location: Location)

}