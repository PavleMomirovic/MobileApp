package com.example.cheapsleep

//import com.google.android.material.navigation.NavigationBarView
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentCreateBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesListView
import java.util.*

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
        val editName: EditText = requireView().findViewById(R.id.editText2)
        val editDesc: EditText = requireView().findViewById(R.id.editText)
        val editLongitude:EditText=requireView().findViewById<EditText>(R.id.edit_longitude)
        val editLatitude:EditText=requireView().findViewById<EditText>(R.id.edit_latitude)
        val dropDown : Spinner = requireView().findViewById(R.id.spinner)
        var typeSelected:String = "No type selected"
        val editPrice:EditText=requireView().findViewById(R.id.edit_price)

        if(myPlacesViewModel.selected!=null){
            editName.setText(myPlacesViewModel.selected?.name)
            editDesc.setText(myPlacesViewModel.selected?.description)
            editPrice.setText(myPlacesViewModel.selected?.price)
            editLongitude.setText(myPlacesViewModel.selected?.longitude)
            editLatitude.setText(myPlacesViewModel.selected?.latitude)
            dropDown.setSelection(getIndex(dropDown, myPlacesViewModel.selected?.type)) //please work
        }

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
        if(myPlacesViewModel.selected!=null) {
            addButton.setText(R.string.edit_save_btn)
            addButton.isEnabled=true
        }

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

            val name: String = editName.text.toString()
            val desc: String = editDesc.text.toString()
            val longitude: String = editLongitude.text.toString()
            val latitude: String = editLatitude.text.toString()
            val price:String= editPrice.text.toString()
            typeSelected = dropDown.selectedItem.toString()

            if(myPlacesViewModel.selected!=null){
                myPlacesViewModel.selected?.name=name
                myPlacesViewModel.selected?.description=desc
                myPlacesViewModel.selected?.longitude=longitude
                myPlacesViewModel.selected?.latitude=latitude
                myPlacesViewModel.selected?.price=price
                myPlacesViewModel.selected?.type=typeSelected
                myPlacesViewModel.selected?.author= UserObject.username.toString()
                myPlacesViewModel.selected?.date= Date()

            }
            else
                myPlacesViewModel.addPlace(Place(name, desc,longitude,latitude,price,typeSelected, UserObject.username.toString(),Date()))
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

//        dropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener,
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                if(parent.getItemAtPosition(position).toString()=="Choose accomodation type"){
//
//                }else{
//                    typeSelected = parent.getItemAtPosition(position).toString()
//                }
//            }
////            override fun onNothingSelected(p0: AdapterView<*>?) {
////                TODO("Not yet implemented")
////            }
////
////            override fun onNavigationItemSelected(item: MenuItem): Boolean {
////                TODO("Not yet implemented")
////            }
//        }


    }

    private fun getIndex(dropDown: Spinner, type: String?): Int {
        for (i in 0 until dropDown.getCount()) {
            if (dropDown.getItemAtPosition(i).toString() ==type) {  //please work
                return i
            }
        }

        return 0
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_new_place).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPlacesViewModel.selected=null
//        _binding = null
    }
}