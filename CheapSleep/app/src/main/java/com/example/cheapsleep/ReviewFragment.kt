package com.example.cheapsleep

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.ReviewPair
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentReviewBinding
import com.example.cheapsleep.model.PlacesDbModel
import com.example.cheapsleep.model.PlacesListView
import com.example.cheapsleep.model.UserDbModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ReviewFragment : Fragment() {
    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore
    var userName: String = UserObject.username!!
    private lateinit var userDbModel: UserDbModel
    private lateinit var placesDbModel: PlacesDbModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReviewBinding.inflate(inflater, container, false)

        userDbModel = ViewModelProvider(this)[UserDbModel::class.java] //maybe this needs to be moved in onCreate
        placesDbModel = ViewModelProvider(this)[PlacesDbModel::class.java]


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
        var review=ReviewPair("",0f)
        var oldRating=0f
        var oldComment=""


        try {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    review=placesDbModel.getPlaceReview(myPlacesViewModel.selected?.id.toString(),UserObject.username.toString())
                }
                if (review.comment!!.isNotEmpty() or (review.rating!=0f)) {
                    rate.rating=review.rating
                    oldRating=review.rating
                    kom.setText(review.comment)
                    oldComment=review.comment!!
                }
            }

        } catch (e: java.lang.Exception) {
//            Toast.makeText(this@ReviewFragment, e.toString(), Toast.LENGTH_SHORT).show()
            Log.w("TAGA", "Greska", e)
        }

        confirmbtn.setOnClickListener {
            var newRating=rate.rating
            var newComment=kom.text
            if (newRating != 0f)
                myPlacesViewModel.selected?.addGrade(userName, rate.rating.toDouble())
            if(newComment.isNotEmpty())
                myPlacesViewModel.selected?.addComment(userName, kom.text.toString())
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    placesDbModel.addPlaceReview(
                        myPlacesViewModel.selected?.id.toString(),
                        myPlacesViewModel.selected?.grades!!,
                        myPlacesViewModel.selected?.comments!!
                    )
                }
                var increaseCommentsCount=false
                var increaseStarsCount=false
                if(oldRating==0f && newRating!=0f) increaseStarsCount=true
                if(oldComment.isEmpty() && newComment.isNotEmpty()) increaseCommentsCount=true

                userDbModel.updateUserScore(userName,false,increaseStarsCount,increaseCommentsCount)
                findNavController().popBackStack()
            }
        }

            cancelbtn.setOnClickListener {
                findNavController().popBackStack()
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}