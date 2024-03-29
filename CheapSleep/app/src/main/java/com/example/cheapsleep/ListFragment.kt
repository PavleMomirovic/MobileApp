package com.example.cheapsleep

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.MapObject
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentListBinding
import com.example.cheapsleep.model.PlacesDbModel
import com.example.cheapsleep.model.PlacesListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val placesListView: PlacesListView by activityViewModels()
    lateinit var arrayAdapter: ArrayAdapter<Place>
    private lateinit var placesDbModel: PlacesDbModel


    var datOdMillis: Long = -1
    var datDoMillis: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        placesDbModel = ViewModelProvider(this)[PlacesDbModel::class.java]

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1, placesListView.myPlacesList
        )
        binding.myPlacesList.adapter = arrayAdapter

        binding.btnDatOd.setOnClickListener {
            showDateTimePicker(requireContext(), onPick = { calendar ->
                datOdMillis = calendar.timeInMillis
                val formatter = SimpleDateFormat("dd.MM.yyyy")
                binding.btnDatOd.text = formatter.format(calendar.time)
            })
        }

        binding.btnDatDo.setOnClickListener {
            showDateTimePicker(requireContext(), onPick = { calendar ->
                datDoMillis = calendar.timeInMillis
                val formatter = SimpleDateFormat("dd.MM.yyyy")
                binding.btnDatDo.text = formatter.format(calendar.time)
            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var tmpList= arrayListOf<Place>()
                withContext(Dispatchers.IO) {
                    tmpList=placesDbModel.getPlaces()
                }
                arrayAdapter.clear()
                arrayAdapter.addAll(tmpList)
                showList(requireView())
                Log.d("TAGA", "s" + placesListView.myPlacesList.size.toString())
            } catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }


        var btnOk: Button = binding.btnOk
        binding.rgTable.setOnCheckedChangeListener { _, i ->
            if (i != R.id.rbDatum) {
                datDoMillis = 0
                datOdMillis = 0
                binding.btnDatDo.text = ""
                binding.btnDatOd.text = ""
            }
            binding.btnDatDo.isVisible = i == R.id.rbDatum
            binding.btnDatOd.isVisible = i == R.id.rbDatum
            binding.tvDatumDo.isVisible = i == R.id.rbDatum
            binding.tvDatumOd.isVisible = i == R.id.rbDatum
        }

        binding.ponistibtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {

                    var tmpList= arrayListOf<Place>()
                    withContext(Dispatchers.IO) {
                        tmpList=placesDbModel.getPlaces()
                    }
                    arrayAdapter.clear()
                    arrayAdapter.addAll(tmpList)
                    showList(requireView())
                    datDoMillis = 0
                    datOdMillis = 0
                    binding.btnDatDo.text = ""
                    binding.btnDatOd.text = ""
                    Log.d("TAGA", "s" + placesListView.myPlacesList.size.toString())
                } catch (e: Exception) {
                    Log.w("TAGA", "Greska", e)
                }
            }
        }


        btnOk.setOnClickListener {
            binding.svTable.clearFocus()
            var textZaPretragu = ""
            val selected = when (binding.rgTable.checkedRadioButtonId) {
                R.id.rbAuthor -> {
                    textZaPretragu = binding.svTable.text.toString().trim()
                    if (textZaPretragu.isEmpty()) {
                        Toast.makeText(context, "You have to type author", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }
                    1
                }
                R.id.rbTip -> {
                    textZaPretragu = binding.svTable.text.toString().trim()
                    if (textZaPretragu.isEmpty()) {
                        Toast.makeText(context, "You have to enter type", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (!requireContext().resources.getStringArray(R.array.TypesOfPlaces).contains(textZaPretragu)) {
                        Toast.makeText(context, "Not a valid type, valid types are:${requireContext().resources.getStringArray(R.array.TypesOfPlaces).toList()} ", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    2
                }
                R.id.rbCena -> {
                    textZaPretragu = binding.svTable.text.toString().trim()
                    if (textZaPretragu.toDoubleOrNull() == null) {
                        Toast.makeText(context, "You have to enter a number", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }
                    3
                }
                R.id.rbDatum -> {
                    if (datOdMillis == 0L && datDoMillis == 0L) {
                        Toast.makeText(context, "You have to select a date", Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }
                    4
                }
                else -> {
                    1
                }
            }
            binding.svTable.setText("")

            viewLifecycleOwner.lifecycleScope.launch {
                try {

                    var tmpList:MutableList<Place>
                    withContext(Dispatchers.IO) {
                        tmpList=placesDbModel.getPlaces().toMutableList()
                    }
                    val newList=filterList(tmpList,selected,textZaPretragu)
                    arrayAdapter.clear()
                    arrayAdapter.addAll(newList)
                    showList(requireView())
                    Log.d("TAGA", "aaa" + placesListView.myPlacesList.size.toString())
                } catch (e: Exception) {
                    Log.w("TAGA", "Greska", e)
                }
            }
        }
    }

    private fun showList(view: View) {

        arrayAdapter.notifyDataSetChanged()
        val listView: ListView = binding.myPlacesList

        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Toast.makeText(view.context, "Hold for more options!", Toast.LENGTH_SHORT).show()
            }
        })

        listView.setOnItemLongClickListener { parent, view, position, id ->
            var myPlace: Place = parent?.adapter?.getItem(position) as Place
            placesListView.selected = myPlace

            showPopupMenu(view, position)
            true
        }
    }

    private fun filterList(tmpList:MutableList<Place>,selected:Int,textZaPretragu:String): MutableList<Place> {
        val newList = mutableListOf<Place>()
        for (item in tmpList) {
            when (selected) {
                1 -> {
                    if (item.author.isNotEmpty() && item.author == textZaPretragu) newList.add(item)
                }
                2 -> {
                    if (item.type.isNotEmpty() && item.type == textZaPretragu) newList.add(item)
                }
                3 -> {
                    if (item.price.isNotEmpty() && item.price.toDouble() <= textZaPretragu.toDouble()) newList.add(
                        item
                    )
                }
                4 -> {
                    if (datDoMillis > 0) {
                        if (datOdMillis > 0) {
                            if (((item.date?.time
                                    ?: datOdMillis) >= datOdMillis) && ((item.date?.time
                                    ?: datDoMillis) <= datDoMillis)
                            ) {
                                newList.add(item)
                            }
                        } else {
                            if ((item.date?.time ?: datDoMillis) <= datDoMillis) {
                                newList.add(item)
                            }
                        }
                    } else {
                        if (((item.date?.time ?: datOdMillis) >= datOdMillis)
                        ) {
                            newList.add(item)
                        }
                    }
                }
            }
        }
        newList.sortBy { it.price }
        return newList
    }


    private fun showDateTimePicker(
        context: Context,
        currentMillis: Long = -1,
        onPick: (date: Calendar) -> Unit
    ) {
        val currentDate: Calendar = Calendar.getInstance()
        if (currentMillis != -1L) currentDate.timeInMillis = currentMillis
        val date: Calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                date.set(year, monthOfYear, dayOfMonth)
                onPick(date)
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DATE)
        ).show()
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        if (!placesListView.selected!!.author.equals(UserObject.username)) {
            val myEditItem = popupMenu.menu.findItem(R.id.editPlace)
            val myDeleteItem = popupMenu.menu.findItem(R.id.delete)
            myEditItem.isVisible = false
            myDeleteItem.isVisible=false
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.viewPlace -> {
                    findNavController().navigate(R.id.action_ListFragment_to_ViewPlaceFragment)
                    true
                }
                R.id.editPlace -> {
                    findNavController().navigate(R.id.action_ListFragment_to_CreateFragment)
                    true
                }
                R.id.showOnMap -> {
                    this.findNavController().navigate(R.id.action_ListFragment_to_MapFragment)
                    true
                }
                R.id.delete ->{
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            var tmpList= arrayListOf<Place>()
                            withContext(Dispatchers.IO) {
                                placesDbModel.deletePlace(placesListView.selected?.id.toString())

                                tmpList=placesDbModel.getPlaces()
                            }
                            arrayAdapter.clear()
                            arrayAdapter.addAll(tmpList)
                            showList(requireView())
                            Log.d("TAGA", "s" + placesListView.myPlacesList.size.toString())
                        } catch (e: Exception) {
                            Log.w("TAGA", "Greska", e)
                        }
                    }
                    Toast.makeText(requireContext(),"Your place deleted",Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
//        menu.findItem(R.id.action_places_list).isVisible = false
        menu.findItem(R.id.action_new_place).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
