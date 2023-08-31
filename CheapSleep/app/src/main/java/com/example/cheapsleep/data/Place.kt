package com.example.cheapsleep.data

class Place(var name: String, var description: String, var longitude:String, var latitude:String, var price:String, var type:String) {
    override fun toString(): String = name
}