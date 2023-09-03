package com.example.cheapsleep

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.User
import com.example.cheapsleep.databinding.FragmentLeaderboardBinding
import com.example.cheapsleep.model.LeaderboardAdapter
//import com.example.cheapsleep.model.UserListAdapter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null


    private val binding get() = _binding!!
    var list: ArrayList<User> = ArrayList()
    var db = Firebase.firestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createList()

    }

    fun createList() {
        list.clear()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result: QuerySnapshot

                result = withContext(Dispatchers.IO) {
                    db.collection("users")
                        .orderBy("overallScore", Query.Direction.DESCENDING)
                        .get()
                        .await()
                }


                for (document in result) {
                    var data = document.data

                    list.add(
                        User(
                            data["username"].toString()!!,
                            data.get("password").toString()!!,
                            data["name"].toString()!!,
                            data.get("surname").toString()!!,
                            data.get("phone").toString()!!,
                            data.get("url").toString()!!,
                            data.get("addCount").toString().toDouble()!!,
                            data.get("starsCount").toString().toDouble()!!,
                            data.get("commentsCount").toString().toDouble()!!,
                            data["overallScore"].toString().toDouble(),
                            document.id
                        )
                    )

                }
                showList(requireView(), list)
            } catch (e: java.lang.Exception) {
                Log.w("TAGA", "GReska", e)
            }
        }
    }

    fun showList(view: View, arrayList: java.util.ArrayList<User>) {


        val listView: ListView = requireView().findViewById(R.id.listUsers)

        val arrayAdapter = LeaderboardAdapter(
            view.context,
            arrayList
        )
        listView.adapter = arrayAdapter

//        val arrayAdapter = ArrayAdapter(
//            view.context,
//            android.R.layout.simple_list_item_1, arrayList
//        )
//
//        listView.adapter = arrayAdapter

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

//            R.id.action_new_place -> {
//                this.findNavController().navigate(R.id.action_ListFragment_to_EditFragment)
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}