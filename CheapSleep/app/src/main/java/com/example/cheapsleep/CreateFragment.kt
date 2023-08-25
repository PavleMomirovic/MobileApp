package com.example.cheapsleep

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.databinding.FragmentCreateBinding
import com.example.cheapsleep.model.PlacesListView

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val myPlacesViewModel: PlacesListView by activityViewModels()

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
        val editName: EditText = requireView().findViewById<EditText>(R.id.editText)
        val addButton: Button = requireView().findViewById(R.id.button2)
        addButton.isEnabled=false
        val cancelButton: Button = requireView().findViewById(R.id.button)
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
            myPlacesViewModel.addPlace(Place(name, desc))
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }

        cancelButton.setOnClickListener {
            findNavController().popBackStack()

            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}