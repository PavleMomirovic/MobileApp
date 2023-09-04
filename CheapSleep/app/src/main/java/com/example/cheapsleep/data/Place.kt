package com.example.cheapsleep.data

import java.util.*

class Place(
    var name: String,
    var description: String,
    var longitude:String,
    var latitude:String,
    var price:String,
    var type:String,
    var author:String,
    var date: Date?,
    var imageUrl:String,
    var grades:HashMap<String,Double>,
    var comments:HashMap<String,String>,
    @Transient var id:String
    ) {
    override fun toString(): String = name

    fun addGrade(username:String,grade:Double){

        if(grades==null)
            grades = HashMap<String,Double>()
        grades[username] = grade
    }
    fun  addComment(username: String,comm:String){
        if(comments == null)
            comments = HashMap<String,String>()
        comments[username] = comm
    }
}