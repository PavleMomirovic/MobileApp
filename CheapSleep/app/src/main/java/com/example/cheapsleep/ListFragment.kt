package com.example.cheapsleep

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentListBinding
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val db = Firebase.firestore

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var userName: String = UserObject.username!!

    private val placesListView: PlacesListView by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView: ListView = requireView().findViewById(R.id.my_places_list)

        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val result = withContext(Dispatchers.IO) {
                    db.collection("places") //add condition author==UserObject.username
                        .get()
                        .await()
                }
                placesListView.myPlacesList.clear()
                placesListView.myPlacesList.addAll(createList(result))
                Log.d("TAGA","s"+placesListView.myPlacesList.size.toString())
//                showList(requireView(), placesListView.myPlacesList)
            } catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }

        val arrayAdapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_list_item_1, placesListView.myPlacesList
        )

        listView.adapter = arrayAdapter
        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var Place: Place = p0?.adapter?.getItem(p2) as Place
                Toast.makeText(view.context,"Hold for more options!", Toast.LENGTH_SHORT).show()
            }
        })

        listView.setOnItemLongClickListener { parent, view, position, id ->
            var myPlace: Place = parent?.adapter?.getItem(position) as Place
            placesListView.selected = myPlace

            showPopupMenu(view, position)
            true
        }
    }
    private fun createList(result: QuerySnapshot): kotlin.collections.ArrayList<Place> {

        var list: kotlin.collections.ArrayList<Place> = ArrayList()
        for (document in result) {
            var data = document.data
//            var grades = HashMap<String, Double>()
//            if (data["grades"] != null) {
//                for (g in data["grades"] as HashMap<String, Double>)
//                    grades[g.key] = g.value
//            }
//            var comments = HashMap<String, String>()
//            if (data["comments"] != null) {
//                for (c in data["comments"] as HashMap<String, String>)
//                    comments[c.key] = c.value
//            }
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
//                    data["url"].toString(),
                        data["id"].toString()
//                    grades,
//                    comments,
//                    document.id
                    )
                )

            }
        }
        return list
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
//        if (!placesListView.selected!!.author.equals(userName)) {
//            val menu = popupMenu.menu.findItem(R.id.editPlace)
//            menu.isVisible = false
//
//        }

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
//                R.id.rankPlace -> {
//
//                    this.findNavController().navigate(R.id.action_ListFragment_to_RankFragment)
//                    true
//                }

                else -> false
            }
        }

        popupMenu.show()
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_places_list).isVisible = false
        menu.findItem(R.id.action_new_place).isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
