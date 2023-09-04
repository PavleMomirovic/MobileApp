package com.example.cheapsleep

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentReviewBinding
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
class ReviewFragment : Fragment() {
    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    var userName: String = UserObject.username!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReviewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var tvName: TextView = view.findViewById(R.id.ReviewFragmentName)
        tvName.setText(myPlacesViewModel.selected?.name)
        var rate: RatingBar = view.findViewById(R.id.ReviewFragmentRating)
        var kom: EditText = view.findViewById(R.id.ReviewFragmentKomentar)
        var confirmbtn: Button = view.findViewById(R.id.ReviewFragmentbtnConfirm)
        var cancelbtn: Button = view.findViewById(R.id.ReviewFragmentbtnCancel)

//        val documentRef = db.collection("places").document(myPlacesViewModel.selected!!.id)
//        val place = hashMapOf(
//            "name" to name,
//            "type" to typeSelected,
//            "date" to Date(),
//            "description" to desc,
//            "price" to price,
//        )
//        var fragmentContext = requireContext()
//        documentRef.update(place as Map<String, Any>)
//            .addOnSuccessListener {
//                Toast.makeText(
//                    fragmentContext,
//                    "Uspesno promenjene informacije o mestu",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//            }
//            .addOnFailureListener { exception ->
//
//                Log.e("TAG", "Greška pri ažuriranju dokumenta", exception)
//            }


        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    myPlacesViewModel.selected?.id?.let {
                        db.collection("places")
                            .document(it)
                            .get()
                            .await()
                    }

                }

                if (result != null) {
                    val document = result.data
                    var hmGrades: HashMap<String, Double>? =
                        document?.get("grades") as HashMap<String, Double>?

                    if (hmGrades?.get(userName) != null)
                        rate.rating = (hmGrades[userName] as Double).toFloat()

                    var hmComments: HashMap<String, String>? =
                        document?.get("comments") as HashMap<String, String>?
                    if (hmComments?.get(userName) != null)
                        kom.setText(hmComments[userName]!!)

                    var oldRate = rate.rating
                    val oldKomm = kom.text.toString()

                    confirmbtn.setOnClickListener {
                        if (rate.rating != 0f)
                            myPlacesViewModel.selected?.addGrade(userName, rate.rating.toDouble())


                        myPlacesViewModel.selected?.addComment(userName, kom.text.toString())
                        if (document != null) {
                            document["grades"] = myPlacesViewModel.selected?.grades!!
                            document["comments"] = myPlacesViewModel.selected?.comments!!

                            result.reference.set(document)
                        }

//                        var starsCount: Long = 0
//                        if (rate.rating != 0f && oldRate == 0f)
//                            starsCount = 2
//                        var kommCount: Long = 0
//                        if (kom.text.isNotEmpty() && oldKomm.isEmpty())
//                            kommCount = 1


                        db.collection("users")
                            .whereEqualTo("username", userName)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                for (documentSnapshot in querySnapshot.documents) {
                                    val documentRef =
                                        db.collection("users").document(documentSnapshot.id)

                                    var starsCount = documentSnapshot.get("starsCount") as Long
                                    if(oldRate==0f && rate.rating!=0f) starsCount++
                                    var kommCount = documentSnapshot.get("commentsCount") as Long
                                    if(oldKomm.isEmpty() && kom.text.isNotEmpty()) kommCount++
                                    var addCount = documentSnapshot.get("addCount") as Long
                                    var tmpOverall = addCount*10+kommCount*3+starsCount

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


//                        myPlacesViewModel.selected = null



                        findNavController().popBackStack()
                    }
                }

            } catch (e: java.lang.Exception) {
                Log.w("TAGA", "Greska", e)
            }
            cancelbtn.setOnClickListener {
                findNavController().popBackStack()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}