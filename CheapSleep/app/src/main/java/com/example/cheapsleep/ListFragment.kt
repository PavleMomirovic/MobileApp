package com.example.cheapsleep

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentListBinding
import com.example.cheapsleep.model.PlacesListView

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

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

        /*  PlacesListView.PlacesList.addAll(
              arrayOf(
                  "Tvrdjava", "Car", "Park Svetog Save", "Trg Kralja Milana"
              )
          )*/

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
