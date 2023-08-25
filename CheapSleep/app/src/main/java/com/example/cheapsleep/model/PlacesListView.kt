package com.example.cheapsleep.model

import androidx.lifecycle.ViewModel
import com.example.cheapsleep.data.Place

class PlacesListView : ViewModel() {

    var myPlacesList: ArrayList<Place> = ArrayList()

    fun addPlace(place: Place) {
        myPlacesList.add(place)
    }
    var selected: Place?=null
}
