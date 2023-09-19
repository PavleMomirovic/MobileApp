package com.example.cheapsleep.model

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.example.cheapsleep.MainActivity
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.checkerframework.checker.units.qual.s
import java.io.ByteArrayOutputStream

class UserDbModel : ViewModel() {
    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private var storageRef = storage.reference

    //    private lateinit var bindingReg: ActivityRegisterBinding
    var ErrorType: Int? = 0

    fun registerUser(user: User, imageView: ImageView) {
        var url = user.profilePhotoUrl
        var imageRef: StorageReference? = storageRef.child("images/" + url)
        var userRef = storageRef.child(url)

        CoroutineScope(Dispatchers.Main).launch {

            var userHM = hashMapOf(
                "username" to user.username,
                "password" to user.password,
                "name" to user.name,
                "surname" to user.surname,
                "phone" to user.phoneNumber,
                "addCount" to 0,
                "starsCount" to 0,
                "commentsCount" to 0,
                "url" to "users/" + user.username + ".jpg"
            )

            imageView.isDrawingCacheEnabled = true
            imageView.buildDrawingCache()
            if (imageView.drawable is BitmapDrawable) {
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                userRef.putBytes(data).await()
            }

            db.collection("users")
                .add(userHM)
                .addOnSuccessListener { documentReference ->

                    var id = documentReference.id.toString()
                    var user: User = User(
                        user.username,
                        user.password,
                        user.name,
                        user.surname,
                        user.phoneNumber,
                        user.profilePhotoUrl,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        id
                    )
                    UserObject.apply {
                        this.username = user.username
                        this.password = user.password
                        this.name = user.name
                        this.surname = user.surname
                        this.addCount = user.addCount
                        this.commentsCount = user.commentsCount
                        this.startCount = user.startCount
                        this.overallScore = user.overallScore

                    }

                }
                .addOnFailureListener { e ->
                    Log.w("TAGA", "Error", e)
                }

//                }
        }
    }

    suspend fun userExists(username: String): Boolean {
        val result = withContext(Dispatchers.IO) {
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
        }

        if (!result.isEmpty) {
            Log.w("TAGA", "User taken ")
        }
        return result.isEmpty
    }

    suspend fun loginUser(username:String,password:String): Boolean {
        val result = withContext(Dispatchers.IO) {
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
        }

        if (!result.isEmpty) {
            for (document in result.documents) {
                if (document != null)
                    if (document.data?.get("password").toString().equals(password)) {

                        var user: User = User(
                            username,
                            password,
                            document.data?.get("firstname").toString(),
                            document.data?.get("lastname").toString(),
                            document.data?.get("phoneNumber").toString(),
                            document.data?.get("url").toString(),
                            (document.data?.get("addCount") as? Number)?.toDouble()
                                ?: 0.0,
                            (document.data?.get("starsCount") as? Number)?.toDouble()
                                ?: 0.0,
                            (document.data?.get("commentsCount") as? Number)?.toDouble()
                                ?: 0.0,
                            (document.data?.get("overallScore") as? Number)?.toDouble()
                                ?: 0.0,
                            document.reference.id.toString()
                        )

                        UserObject.apply {
                            this.username = user.username
                            this.password = user.password
                            this.name = user.name
                            this.surname = user.surname
                            this.addCount = user.addCount
                            this.commentsCount = user.commentsCount
                            this.startCount = user.startCount
                        }
                    }
            }
        }
        return !result.isEmpty
    }

    suspend fun getUsers(): ArrayList<User> {
        var list: ArrayList<User> = ArrayList()

        val result: QuerySnapshot = withContext(Dispatchers.IO) {
            db.collection("users")
                .orderBy("overallScore", Query.Direction.DESCENDING)
                .get()
                .await()
        }


        for (document in result) {
            var data = document.data

            list.add(
                User(
                    data["username"].toString()!!,
                    data.get("password").toString()!!,
                    data["name"].toString()!!,
                    data.get("surname").toString()!!,
                    data.get("phone").toString()!!,
                    data.get("url").toString()!!,
                    data.get("addCount").toString().toDouble()!!,
                    data.get("starsCount").toString().toDouble()!!,
                    data.get("commentsCount").toString().toDouble()!!,
                    data["overallScore"].toString().toDouble(),
                    document.id
                )
            )

        }
        return list
    }

    fun updateUserScore(username:String,firstComment:Boolean,firstRate:Boolean,rated:Boolean,commented:Boolean) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (documentSnapshot in querySnapshot.documents) {
                    val documentRef =
                        db.collection("users").document(documentSnapshot.id)

                    var starsCount = documentSnapshot.get("starsCount") as Long
                    if (firstRate && rated) starsCount++
                    var kommCount = documentSnapshot.get("commentsCount") as Long
                    if (firstComment && commented) kommCount++
                    var addCount = documentSnapshot.get("addCount") as Long
                    var tmpOverall = addCount * 10 + kommCount * 3 + starsCount

                    val noviPodaci = hashMapOf<String, Any>(
                        "starsCount" to starsCount,
                        "commentsCount" to kommCount,
                        "overallScore" to tmpOverall
                    )
                    documentRef.update(noviPodaci)
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { exception ->
                            Log.w("TAGA", "Error", exception)
                        }
                }
            }
    }
}

