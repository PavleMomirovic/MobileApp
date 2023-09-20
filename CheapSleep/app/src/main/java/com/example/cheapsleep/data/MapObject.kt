package com.example.cheapsleep.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint


data class MapObject(
    var longitude:String="",
    var latitude:String="",
    var name: String = ""
)