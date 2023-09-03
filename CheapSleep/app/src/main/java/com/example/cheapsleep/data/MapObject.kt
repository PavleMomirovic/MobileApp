package com.example.cheapsleep.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint


data class MapObject(
    var id: String = "",
    var imeKorisnika: String = "",
    var longitude:String="",
    var latitude:String="",
    var name: String = "",
    var radius: Float = 0.0f,
    val datum: Timestamp? = null,
    var user: String? = null,
)