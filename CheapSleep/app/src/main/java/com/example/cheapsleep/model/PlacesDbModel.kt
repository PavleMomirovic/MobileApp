package com.example.cheapsleep.model

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.example.cheapsleep.data.MapObject
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.ReviewPair
import com.example.cheapsleep.data.UserObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlacesDbModel: ViewModel() {
    private var db = Firebase.firestore
    private var storage = Firebase.storage
    private var storageRef = storage.reference


    fun createPlace(myPlace:Place,imageView:ImageView){

        val place = hashMapOf(
            "name" to myPlace.name,
            "latitude" to myPlace.latitude,
            "longitude" to myPlace.longitude,
            "author" to UserObject.username.toString(),
            "type" to myPlace.type,
            "date" to Date(),
            "price" to myPlace.price,
            "description" to myPlace.description,
            "grades" to myPlace.grades,
            "comments" to myPlace.comments,
        )

        db.collection("places")
            .add(place)
            .addOnSuccessListener { documentReference ->
//
                myPlace.id = documentReference.id.toString()
                myPlace.imageUrl = "places/" + myPlace.id + ".jpg"
                val url = "places/" + myPlace.id + ".jpg"
                var imageRef: StorageReference? =
                    storageRef.child("images/" + url)
                var PlaceRef = storageRef.child(url)


                imageView.isDrawingCacheEnabled = true
                imageView.buildDrawingCache()

                if (imageView.drawable is BitmapDrawable) {
                    val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    var uploadTask = PlaceRef.putBytes(data)
                    uploadTask.addOnFailureListener { e ->
                        Log.w("TAGA", "Greska", e)
                    }
                }
                val documentRef = db.collection("places").document(myPlace.id)
                val placeUrl = hashMapOf(
                    "imageUrl" to myPlace.imageUrl
                )

                documentRef.update(placeUrl as Map<String, Any>)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                    }

            }
            .addOnFailureListener { e ->
                Log.w("TAGA", "Error", e)
            }
    }

    fun editPlace(place: Place, imageView:ImageView) {
        val documentRef = db.collection("places").document(place.id!!.toString())
        if (place.imageUrl != null) {
            var PlaceRef = storageRef.child(place.imageUrl)

            if (imageView.drawable is BitmapDrawable) {
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                var uploadTask = PlaceRef.putBytes(data)
                uploadTask.addOnFailureListener { e ->
                    Log.w("TAGA", "Greska", e)
                }
            }
        }

        val editPlace = hashMapOf(
            "name" to place.name,
            "type" to place.type,
            "date" to place.date,
            "description" to place.description,
            "price" to place.price,
        )

//        var fragmentContext = requireContext()
        documentRef.update(editPlace as Map<String, Any>)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Update error", exception)
            }

    }
    suspend fun addPlaceReview(id:String,grades:HashMap<String,Double>,comments:HashMap<String,String>) {
        val result = withContext(Dispatchers.IO) {
            id?.let {
                db.collection("places")
                    .document(it)
                    .get()
                    .await()
            }
        }
        if (result != null) {
            val document = result.data
            if (document != null) {
                document["grades"] =grades!!
                document["comments"] =comments!!

                result.reference.set(document)
            }
        }
    }

    suspend fun getPlaceReview(id:String,userName:String): ReviewPair {
        var tmpReview=ReviewPair("",0f)
        var rating:Float=0f
        var comment:String=""
        val result = withContext(Dispatchers.IO) {
            id?.let {
                db.collection("places")
                    .document(it)
                    .get()
                    .await()
            }
        }

        if (result != null) {
            val document = result.data
            var hmGrades: java.util.HashMap<String, Double>? =
                document?.get("grades") as java.util.HashMap<String, Double>?

            if (hmGrades?.get(userName) != null)
                rating = (hmGrades[userName] as Double).toFloat()


            var hmComments: java.util.HashMap<String, String>? =
                document?.get("comments") as java.util.HashMap<String, String>?
            if (hmComments?.get(userName) != null)
                comment=hmComments[userName]!!

        }
        tmpReview=ReviewPair(comment,rating)
        return tmpReview
    }

    suspend fun getPlaces(): kotlin.collections.ArrayList<Place>{
        val result = withContext(Dispatchers.IO) {
            db.collection("places")
                .get()
                .await()
        }
        var list: kotlin.collections.ArrayList<Place> = ArrayList()
        for (document in result) {
            var data = document.data
            var grades = java.util.HashMap<String, Double>()
            if (data["grades"] != null) {
                for (g in data["grades"] as java.util.HashMap<String, Double>)
                    grades[g.key] = g.value
            }
            var comments = java.util.HashMap<String, String>()
            if (data["comments"] != null) {
                for (c in data["comments"] as java.util.HashMap<String, String>)
                    comments[c.key] = c.value
            }
            var date: Date? = null
            if (data["date"] != null) {

                val timestamp: com.google.firebase.Timestamp? =
                    document.getTimestamp("date")
                date = timestamp?.toDate()
//
//            }

                list.add(
                    Place(
                        data["name"].toString(),
                        data["description"].toString(),
                        data["longitude"].toString(),
                        data["latitude"].toString(),
                        data["price"].toString(),
                        data["type"].toString(),
                        data["author"].toString(),
                        date,
                        data["imageUrl"].toString(),
                        grades,
                        comments,
                        document.id
                    )
                )

            }
        }
        return list
    }

    fun deletePlace(id:String){
        db.collection("places").document(id)
            .delete()
            .addOnSuccessListener { Log.d("TAGA", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("TAGA", "Error deleting document", e) }
    }

//    suspend fun getPlacesInRadius(){
//        val result = withContext(Dispatchers.IO) {
//            db.collection("places")
//                .get()
//                .await()
//        }
//        var list: kotlin.collections.ArrayList<MapObject> = ArrayList()
//        for (document in result) {
//            var data = document.data
//
//            var date: Date? = null
//            if (data["date"] != null) {
//
//            list.add(
//                MapObject(
//                    data["name"].toString(),
//                    data["description"].toString(),
//                    data["longitude"].toString(),
//                    data["latitude"].toString(),
//                    data["price"].toString(),
//                    data["type"].toString(),
//                    data["author"].toString(),
//                    date,
//                    data["imageUrl"].toString(),
//                    grades,
//                    comments,
//                    document.id
//                )
//            )
//
//            }
//        }
//        return list
//
//    }

}