package com.example.cheapsleep

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.databinding.FragmentViewPlaceBinding
import com.example.cheapsleep.model.PlacesDbModel
import com.example.cheapsleep.model.PlacesListView
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ViewPlaceFragment : Fragment() {

    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private var _binding: FragmentViewPlaceBinding? = null
    private val binding get() = _binding!!
    private lateinit var placesDbModel: PlacesDbModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPlaceBinding.inflate(inflater, container, false)
        placesDbModel = ViewModelProvider(this)[PlacesDbModel::class.java]

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
                placesDbModel.getPlaceImg(path,requireContext(),imageView)

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
    }
}