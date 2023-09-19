package com.example.cheapsleep

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.cheapsleep.databinding.FragmentViewPlaceBinding
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ViewPlaceFragment : Fragment() {

    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private var _binding: FragmentViewPlaceBinding? = null
    private val binding get() = _binding!!
    private var db = Firebase.firestore
    private var storage = Firebase.storage

    var storageRef = storage.reference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        val inputFormat: DateFormat =
            SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        var imageView = binding.imgPlaceView
        viewLifecycleOwner.lifecycleScope.launch() {
            try {
                val path = myPlacesViewModel.selected?.imageUrl.toString()
                val imageRef = storageRef.child(path).downloadUrl.await()

                Log.d("TAGA", myPlacesViewModel.selected?.imageUrl.toString())
                Glide.with(this@ViewPlaceFragment).clear(imageView)
                Glide.with(this@ViewPlaceFragment)
                    .load(imageRef)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        binding.ViewFragmentName.text = myPlacesViewModel.selected?.name
        binding.ViewFragmentCena.text = myPlacesViewModel.selected?.price
        binding.ViewFragmentAutor.text = myPlacesViewModel.selected?.author
        binding.ViewFragmentDescription.text = myPlacesViewModel.selected?.description


        val date = inputFormat.parse(myPlacesViewModel.selected?.date.toString())
        val formattedDate: String = dateFormat.format(date)
        binding.ViewFragmentDate.text = formattedDate

        binding.ViewFragmentTip.text = myPlacesViewModel.selected?.type

        var sum: Double = 0.0
        for (el in myPlacesViewModel.selected?.grades!!)
            sum += el.value
        if (myPlacesViewModel.selected?.grades!!.size != 0)
            sum /= myPlacesViewModel.selected?.grades!!.size
        binding.ratingBar2.rating = sum.toFloat()

        var s: ArrayList<String> = ArrayList()
        for (el in myPlacesViewModel.selected?.comments!!)
            s.add(el.value)

        binding.viewFragmentListView.adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, s)


        binding.ViewFragmentClose.setOnClickListener {
//            myPlacesViewModel.selected=null
            findNavController().popBackStack()

        }

        binding.RatingButton.setOnClickListener {
            val placeLocation = Location("x").apply {
                latitude = myPlacesViewModel.selected?.latitude?.toDouble() ?: 0.0
                longitude = myPlacesViewModel.selected?.longitude?.toDouble() ?: 0.0
            }
            if (MainActivity.curLocation.distanceTo(placeLocation) > 100) {
                Toast.makeText(
                    context,
                    "You can make a review only when you are on place",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                findNavController().navigate(R.id.action_ViewPlaceFragment_to_ReviewFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        myPlacesViewModel.selected=null
    }
}