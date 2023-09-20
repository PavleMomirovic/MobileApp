package com.example.cheapsleep

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.cheapsleep.data.ILocationClient
import com.example.cheapsleep.data.MapObject
import com.example.cheapsleep.databinding.FragmentMapBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesDbModel
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay



class MapFragment : Fragment(), ILocationClient {
    lateinit var map:MapView
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentMapBinding
    private lateinit var myMarker: Marker
    private var objectsOnMapMarkers = mutableListOf<Overlay>()
    private lateinit var placesDbModel: PlacesDbModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        placesDbModel = ViewModelProvider(this)[PlacesDbModel::class.java]

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentMapBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onNewLocation(location: Location) {
        map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
        myMarker.position = GeoPoint(location.latitude, location.longitude)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        var ctx: Context = requireActivity()!!.getApplicationContext()
        Configuration.getInstance().load(ctx,PreferenceManager.getDefaultSharedPreferences((ctx)))
        map=requireView().findViewById<MapView>(R.id.map)
        map.setMultiTouchControls(true)
        if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            setupMap()
            viewLifecycleOwner.lifecycleScope.launch() {
                showObjects()
            }

            binding.radiusEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    viewLifecycleOwner.lifecycleScope.launch() {
                        showObjects()
                    }
                }
            })

            binding.radioGroup.setOnCheckedChangeListener { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch() {
                        showObjects()
                    }
                }

            }

        MainActivity.iLocationClient = this

    }

    private fun setupMap(){
        myMarker=Marker(map)

        var startPoint:GeoPoint=GeoPoint(43.32,21.89)
        map.controller.setZoom(15.0)
        if(locationViewModel.setLocation){
            setOnMapClickOverlay()
        }
        else{

            setMyLocationOverlay()
        }
        map.controller.animateTo(startPoint)
    }

    private fun setMyLocationOverlay(){
        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(activity),map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add((myLocationOverlay))
    }

    private val requestPermissionLauncher=
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted:Boolean ->
            if(isGranted){
                setOnMapClickOverlay()
                setMyLocationOverlay()
            }
        }
    private fun setOnMapClickOverlay(){
        var receive=object:MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                var lon=p!!.longitude.toString()
                var lat=p!!.latitude.toString()
                locationViewModel.setLocation(lon,lat)
                findNavController().popBackStack()
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        var overlayEvents=MapEventsOverlay(receive)
        map.overlays.add(overlayEvents)
    }
    private suspend fun showObjects() {
        for (marker in objectsOnMapMarkers)
            binding.map.overlays.remove(marker)
        objectsOnMapMarkers.clear()

        var radius=0f
        if (!binding.sviObjRadio.isChecked and binding.radiusEt.text.isNotEmpty()) {
            radius = binding.radiusEt.text.toString().toFloat()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var listOfObjects= arrayListOf<MapObject>()
                withContext(Dispatchers.IO) {
                    listOfObjects=placesDbModel.getPlacesInRadius(radius)
                }
                for (obj in listOfObjects) {
                    objectsOnMapMarkers.add(Marker(binding.map).apply {
                        this.position = GeoPoint(obj.latitude.toDouble(), obj.longitude.toDouble())
                        title = obj.name
                    })
                }

                binding.map.overlays.addAll(objectsOnMapMarkers)
                binding.map.invalidate()

            } catch (e: Exception) {
                Log.w("TAGA", "Greska", e)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_new_place->{
                this.findNavController().navigate(R.id.action_MapFragment_to_CreateFragment)
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        MainActivity.iLocationClient=null
        super.onDestroyView()
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}