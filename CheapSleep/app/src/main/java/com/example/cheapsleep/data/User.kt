package com.example.cheapsleep.data

data class User(val username:String,
                val password:String,
                val name:String,
                val surname: String,
                val phoneNumber: String,
                val profilePhotoUrl:String,
                val addCount:Double,
                val startCount:Double,
                val commentsCount:Double,
                @Transient val id:String
):java.io.Serializable {

}