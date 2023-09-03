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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.cheapsleep.data.ILocationClient
import com.example.cheapsleep.data.MapObject
import com.example.cheapsleep.databinding.FragmentMapBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class MapFragment : Fragment(), ILocationClient {
    lateinit var map:MapView
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val myPlacesListView:PlacesListView by activityViewModels()
    private val db = Firebase.firestore
//    private val storage: StorageReference
    private lateinit var binding: FragmentMapBinding
    private lateinit var myMarker: Marker
    private var myPosition: GeoPoint = GeoPoint(0.0, 0.0)
    private var isFirstLocation = true
    private var objectsOnMapMarkers = mutableListOf<Overlay>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main,menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentMapBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onNewLocation(location: Location) {
        map.controller.animateTo(GeoPoint(location.latitude, location.longitude))
        myMarker.position = GeoPoint(location.latitude, location.longitude)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        var ctx: Context = requireActivity()!!.getApplicationContext()  //ovde treba:  Context? = getActivity()?...  ali onda dole dobijam type missmatch
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
                showObjects(getObjectsForShow())
            }

            binding.radiusEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    viewLifecycleOwner.lifecycleScope.launch() {
                        showObjects(getObjectsForShow())
                    }
                }
            })

            binding.radioGroup.setOnCheckedChangeListener { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch() {
                        showObjects(getObjectsForShow())
                    }
                }
//                binding.radiusEt.isEnabled = binding.objWithRadiusRadio.isChecked

            }
//            setMyLocationOverlay()
//            setOnMapClickOverlay()
//        map.controller.setZoom(15.0)
//        val startPoint=GeoPoint(43.32,21.89)
//        map.controller.setCenter(startPoint)
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
//            ovde da se doda za selected u PlacesListView
//            myPlacesListView.selected=true
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
    private suspend fun getObjectsForShow():MutableList<MapObject>{
        return suspendCoroutine { continuation ->

        db.collection("places").get()
//            .whereGreaterThanOrEqualTo("koord", GeoPoint(minLat, minLon))
//            .whereLessThanOrEqualTo("koord", GeoPoint(maxLat, maxLon)).get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                snapshot?.let {
                    val objects = it.documents.mapNotNull { documentSnapshot ->
                        val obj = documentSnapshot.toObject(MapObject::class.java)?.also { obj ->
                            obj.id = documentSnapshot.id
                        }
                        obj
                    }.toMutableList()
//                    for(obj in objects){
//                        if (obj.longitude.toDouble()<minLon || obj.longitude.toDouble()>maxLon ||obj.latitude.toDouble()<minLat || obj.latitude.toDouble()>maxLat ){
//                            objects.remove(obj)
//                        }
//                    }

                    continuation.resume(objects)
                }

                }
            }
    }
    private suspend fun showObjects(listOfObjects:MutableList<MapObject>) {
        for (marker in objectsOnMapMarkers)
            binding.map.overlays.remove(marker)
        objectsOnMapMarkers.clear()


        val myLocation = MainActivity.curLocation!!
        val earthRadius = 6371.0F
        var radius = earthRadius

        if (!binding.sviObjRadio.isChecked and binding.radiusEt.text.isNotEmpty()) {
            radius = binding.radiusEt.text.toString().toFloat()
        }
        val latDelta = radius / earthRadius
        val lonDelta = atan2(
            sin(latDelta) * cos(myLocation.latitude),
            cos(latDelta) - sin(myLocation.latitude) * sin(myLocation.latitude)
        )
        val minLat = myLocation.latitude - latDelta * (180 / PI)
        val maxLat = myLocation.latitude + latDelta * (180 / PI)
        val minLon = myLocation.longitude - lonDelta * (180 / PI)
        val maxLon = myLocation.longitude + lonDelta * (180 / PI)

//        for (obj in listOfObjects) {
//            if (obj.longitude.toDouble() < minLon || obj.longitude.toDouble() > maxLon || obj.latitude.toDouble() < minLat || obj.latitude.toDouble() > maxLat) {
//                listOfObjects.remove(obj)
//            }
//        }
        Log.d("TAGA", "$listOfObjects")
        for (obj in listOfObjects) {

            val center = GeoPoint(obj.latitude.toDouble(), obj.longitude.toDouble())
            if (obj.longitude.toDouble() > minLon && obj.longitude.toDouble() < maxLon && obj.latitude.toDouble() > minLat && obj.latitude.toDouble() < maxLat) {

                objectsOnMapMarkers.add(Marker(binding.map).apply {
                    this.position = center
                    title = obj.name
                })
            }
        }


        binding.map.overlays.addAll(objectsOnMapMarkers)
        binding.map.invalidate()

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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        var item=menu.findItem(R.id.action_places_list)
        item.isVisible=false
        item=menu.findItem(R.id.action_show_map)
        item.isVisible=false
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