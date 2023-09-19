package com.example.cheapsleep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.User
import com.example.cheapsleep.databinding.FragmentLeaderboardBinding
import com.example.cheapsleep.model.LeaderboardAdapter
import com.example.cheapsleep.model.UserDbModel
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
    private lateinit var userDbModel: UserDbModel



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
            try {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    list=userDbModel.getUsers()
                }
                if (list.isNotEmpty()) {
                   showList(requireView(),list)
                }
            }


        } catch (e: java.lang.Exception) {
//            Toast.makeText(this@LeaderboardFragment, e.toString(), Toast.LENGTH_SHORT).show()
            Log.w("TAGA", "Greska", e)
        }
    }

    fun showList(view: View, arrayList: java.util.ArrayList<User>) {


        val listView: ListView = requireView().findViewById(R.id.listUsers)

        val arrayAdapter = LeaderboardAdapter(
            view.context,
            arrayList
        )
        listView.adapter = arrayAdapter


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDbModel = ViewModelProvider(this)[UserDbModel::class.java]

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

            else -> super.onOptionsItemSelected(item)
        }
    }
}
