package com.example.cheapsleep

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.databinding.FragmentCreateBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesListView

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
        val editName: EditText = requireView().findViewById<EditText>(R.id.editText2)
        val editLongitude:EditText=requireView().findViewById<EditText>(R.id.edit_longitude)
        val editLatitude:EditText=requireView().findViewById<EditText>(R.id.edit_latitude)
        val lonObserver= Observer<String>{ newValue->
            editLongitude.setText((newValue.toString()))
        }
        locationViewModel.longitude.observe(viewLifecycleOwner,lonObserver)
        val latObserver= Observer<String>{ newValue->
            editLatitude.setText((newValue.toString()))
        }
        locationViewModel.latitude.observe(viewLifecycleOwner,latObserver)
        val addButton: Button = requireView().findViewById(R.id.button2)
        addButton.isEnabled=false
        val cancelButton: Button = requireView().findViewById(R.id.button)
        val setButton:Button=requireView().findViewById<Button>(R.id.edit_location_btn)
        setButton.setOnClickListener{
            locationViewModel.setLocation=true
            findNavController().navigate(R.id.actionCreateFragment_to_MapFragment)
        }
        editName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                addButton.isEnabled=(editName.text.length>0)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        addButton.setOnClickListener {
            val editName: EditText = requireView().findViewById(R.id.editText2)
            val editDesc: EditText = requireView().findViewById(R.id.editText)

            val name: String = editName.text.toString()
            val desc: String = editDesc.text.toString()
            val longitude: String = editLongitude.text.toString()
            val latitude: String = editLatitude.text.toString()
            if(myPlacesViewModel.selected!=null){
                myPlacesViewModel.selected?.name=name
                myPlacesViewModel.selected?.description=desc
                myPlacesViewModel.selected?.longitude=longitude
                myPlacesViewModel.selected?.latitude=latitude
            }
            else
                myPlacesViewModel.addPlace(Place(name, desc,longitude,latitude))
            myPlacesViewModel.selected=null
            locationViewModel.setLocation("","")
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }

        cancelButton.setOnClickListener {
            myPlacesViewModel.selected=null
            locationViewModel.setLocation("","")
            findNavController().popBackStack()

            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_new_place).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}