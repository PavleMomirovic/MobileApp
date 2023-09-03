package com.example.cheapsleep

import android.content.Intent
import android.location.Location
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.navigation.NavController
import com.example.cheapsleep.data.ILocationClient
import com.example.cheapsleep.data.LocationClient
import com.example.cheapsleep.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ILocationClient {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        var curLocation: Location = Location("default").apply {
            latitude=45.213
            longitude=21.343
        }
        var iLocationClient: ILocationClient? = null
        var locationClient: LocationClient? = null

    }
    override fun onNewLocation(location: Location) {
        curLocation = location
        iLocationClient?.onNewLocation(location)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationClient = LocationClient(applicationContext, this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id==R.id.CreateFragment || destination.id==R.id.ViewPlaceFragment)
                binding.fab.hide()
            else
                binding.fab.show()

        }

        binding.fab.setOnClickListener { view ->
            if(navController.currentDestination?.id==R.id.ListFragment)
                navController.navigate(R.id.action_ListFragment_to_CreateFragment)
            else if(navController.currentDestination?.id==R.id.MapFragment)
                navController.navigate(R.id.action_MapFragment_to_CreateFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_new_place -> {
                if(navController.currentDestination?.id==R.id.ListFragment)
                    navController.navigate(R.id.action_ListFragment_to_CreateFragment)
            }
            R.id.action_show_map -> {
                if(navController.currentDestination?.id==R.id.ListFragment)
                    navController.navigate(R.id.action_ListFragment_to_MapFragment)
            }
            R.id.action_leaderboard -> {
                if(navController.currentDestination?.id==R.id.ListFragment)
                    navController.navigate(R.id.action_ListFragment_to_LeaderboardFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}