package com.example.cheapsleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.databinding.FragmentViewPlaceBinding
import com.example.cheapsleep.model.PlacesListView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ViewPlaceFragment : Fragment() {

    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private var _binding : FragmentViewPlaceBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentViewPlaceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        val inputFormat: DateFormat =
            SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)


        binding.ViewFragmentName.text = myPlacesViewModel.selected?.name
        //binding.ViewFragmentDesc.text = myPlacesViewModel.selected?.description
        binding.ViewFragmentAutor.text = myPlacesViewModel.selected?.author
        binding.ViewFragmentDescription.text = myPlacesViewModel.selected?.description


        val date = inputFormat.parse(myPlacesViewModel.selected?.date.toString())
        val formattedDate: String = dateFormat.format(date)
        binding.ViewFragmentDate.text = formattedDate

        binding.ViewFragmentTip.text = myPlacesViewModel.selected?.type

//        var sum:Double = 0.0
//        for (el in myPlacesViewModel.selected?.grades!!)
//            sum += el.value
//        if (myPlacesViewModel.selected?.grades!!.size!= 0)
//            sum /= myPlacesViewModel.selected?.grades!!.size
//        binding.ratingBar2.rating = sum.toFloat()
//
//        var s:ArrayList<String> = ArrayList()
//        for (el in myPlacesViewModel.selected?.comments!!)
//            s.add(el.value)

//        binding.viewFragmentListView.adapter=ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1,s)


        binding.ViewFragmentClose.setOnClickListener {
            myPlacesViewModel.selected=null
            findNavController().navigate(R.id.action_ViewPlaceFragment_to_ListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        myPlacesViewModel.selected=null
    }
}