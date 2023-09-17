package com.example.cheapsleep.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class UserDbModel : ViewModel() {
    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private var storageRef = storage.reference
//    private lateinit var bindingReg: ActivityRegisterBinding
    var ErrorType:Int?=0

    fun registerUser(user: User, imageView:ImageView) {
        var url = user.profilePhotoUrl
        var imageRef: StorageReference? = storageRef.child("images/" + url)
        var userRef = storageRef.child(url)

        CoroutineScope(Dispatchers.Main).launch {

//                val result = withContext(Dispatchers.IO) {
//                    db.collection("users")
//                        .whereEqualTo("username", user.username)
//                        .get()
//                        .await()
//                }
//
//                if (!result.isEmpty) {
//
//                    ErrorType=1
//                    Log.w("TAGA", "User taken "+ErrorType)
//
//                } else {
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

//                    var imageView: ImageView = bindingReg.imgUser
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
    suspend fun userExists(username:String):Boolean {
            val result = withContext(Dispatchers.IO) {
                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()
            }

            if (!result.isEmpty) {
                Log.w("TAGA", "User taken ")
            }
            return  result.isEmpty
    }
}