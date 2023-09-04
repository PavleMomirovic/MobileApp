package com.example.cheapsleep

//import com.google.android.material.navigation.NavigationBarView
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentCreateBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesListView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()

    private var CAMERA_REQUEST_CODE = 0
    private var GALLERY_REQUEST_CODE = 0
    private var db = Firebase.firestore
    private var storage = Firebase.storage

    var storageRef = storage.reference

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
        val editLongitude: EditText = requireView().findViewById<EditText>(R.id.edit_longitude)
        val editLatitude: EditText = requireView().findViewById<EditText>(R.id.edit_latitude)
        val dropDown: Spinner = requireView().findViewById(R.id.spinner)
        var typeSelected: String = "No type selected"
        val editPrice: EditText = requireView().findViewById(R.id.edit_price)

        val cancelButton: Button = requireView().findViewById(R.id.button)
        val setButton: Button = requireView().findViewById<Button>(R.id.edit_location_btn)

        var cameraButton: Button = requireView().findViewById(R.id.btnCameraPlace)
        var galerijaButton: Button = requireView().findViewById(R.id.btnGalleryPlace)
        var imageView: ImageView = binding.imgPlace


        if (myPlacesViewModel.selected != null) {
            editName.setText(myPlacesViewModel.selected?.name)
            editDesc.setText(myPlacesViewModel.selected?.description)
            editPrice.setText(myPlacesViewModel.selected?.price)
            editLongitude.setText(myPlacesViewModel.selected?.longitude.toString())
            editLatitude.setText(myPlacesViewModel.selected?.latitude.toString())
            dropDown.setSelection(
                getIndex(
                    dropDown,
                    myPlacesViewModel.selected?.type
                )
            ) //please work

            viewLifecycleOwner.lifecycleScope.launch() {
                try {

                    val path = myPlacesViewModel.selected?.imageUrl.toString()
                    val imageRef = storageRef.child(path).downloadUrl.await()

                    Log.d("TAGA", myPlacesViewModel.selected?.imageUrl.toString())
                    Glide.with(this@CreateFragment).clear(imageView)
                    Glide.with(this@CreateFragment)
                        .load(imageRef)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)

                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }

        val lonObserver = Observer<String> { newValue ->
            editLongitude.setText((newValue.toString()))
        }
        locationViewModel.longitude.observe(viewLifecycleOwner, lonObserver)
        val latObserver = Observer<String> { newValue ->
            editLatitude.setText((newValue.toString()))
        }
        locationViewModel.latitude.observe(viewLifecycleOwner, latObserver)
        val addButton: Button = requireView().findViewById(R.id.button2)
        addButton.isEnabled = false
        if (myPlacesViewModel.selected != null) {
            addButton.setText(R.string.edit_save_btn)
            setButton.isVisible = false
            editLongitude.isVisible = false
            editLatitude.isVisible = false
            binding.textViewLatitude.isVisible=false
            binding.textViewLongitude.isVisible=false
//            requireView().findViewById<TextView>(R.id.text_view_longitude).isVisible=false
//            requireView().findViewById<TextView>(R.id.text_view_latitude).isVisible=false
//          you cannot just change location of places
            addButton.isEnabled = true
        }

        setButton.setOnClickListener {
            locationViewModel.setLocation = true
            findNavController().navigate(R.id.actionCreateFragment_to_MapFragment)
        }
        editName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                addButton.isEnabled = (editName.text.length > 0)
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
            val price: String = editPrice.text.toString()
            typeSelected = dropDown.selectedItem.toString()

            if (myPlacesViewModel.selected != null) {
                myPlacesViewModel.selected?.name = name
                myPlacesViewModel.selected?.description = desc
                myPlacesViewModel.selected?.longitude = longitude
                myPlacesViewModel.selected?.latitude = latitude
                myPlacesViewModel.selected?.price = price
                myPlacesViewModel.selected?.type = typeSelected
                myPlacesViewModel.selected?.author = UserObject.username.toString()
                myPlacesViewModel.selected?.date = Date()

                val documentRef = db.collection("places").document(myPlacesViewModel.selected!!.id)
                if (myPlacesViewModel.selected?.imageUrl != null) {
                    var PlaceRef = storageRef.child(myPlacesViewModel.selected?.imageUrl!!)

                    if (imageView.drawable is BitmapDrawable) {
                        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        var uploadTask = PlaceRef.putBytes(data)
                        uploadTask.addOnFailureListener { e ->
                            Log.w("TAGA", "Greska", e)
                        }
                    }
                }

                val place = hashMapOf(
                    "name" to name,
                    "type" to typeSelected,
                    "date" to Date(),
                    "description" to desc,
                    "price" to price,
                )

                var fragmentContext = requireContext()
                documentRef.update(place as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(
                            fragmentContext,
                            "Uspesno promenjene informacije o mestu",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    .addOnFailureListener { exception ->

                        Log.e("TAG", "Greška pri ažuriranju dokumenta", exception)
                    }

            } else {
                var myPlace: Place = Place(
                    name,
                    desc,
                    longitude,
                    latitude,
                    price,
                    typeSelected,
                    author = UserObject.username.toString(),
                    Date(),
                    "",
                    HashMap(),
                    HashMap(),
                    "",
                )
                myPlacesViewModel.addPlace(
                    myPlace
                )
//                val hash = GeoFireUtils.getGeoHashForLocation(
//                    GeoLocation(
//                        Place.latitude.toDouble(),
//                        Place.longitude.toDouble()
//                    )
//                )

                val place = hashMapOf(
                    "name" to name,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "author" to UserObject.username.toString(),
                    "type" to typeSelected,
                    "date" to Date(),
                    "price" to price,
                    "description" to desc,
                    "grades" to myPlace.grades,
                    "comments" to myPlace.comments,
                )

                db.collection("places")
                    .add(place)
                    .addOnSuccessListener { documentReference ->
//
                        myPlace.id = documentReference.id.toString()
                        myPlace.imageUrl = "places/" + myPlace.id + ".jpg"
                        val url = "places/" + myPlace.id + ".jpg"
                        var imageRef: StorageReference? =
                            storageRef.child("images/" + url)
                        var PlaceRef = storageRef.child(url)


                        imageView.isDrawingCacheEnabled = true
                        imageView.buildDrawingCache()

                        if (imageView.drawable is BitmapDrawable) {
                            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()
                            var uploadTask = PlaceRef.putBytes(data)
                            uploadTask.addOnFailureListener { e ->
                                Log.w("TAGA", "Greska", e)
                            }
                        }
                        val documentRef = db.collection("places").document(myPlace.id)
                        val placeUrl = hashMapOf(
                            "imageUrl" to myPlace.imageUrl
                        )

                        documentRef.update(placeUrl as Map<String, Any>)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { exception ->

                            }

                    }
                    .addOnFailureListener { e ->
                        Log.w("TAGA", "Error", e)
                    }
                //update user's add count:
                db.collection("users")
                    .whereEqualTo("username", UserObject.username)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (documentSnapshot in querySnapshot.documents) {
                            val documentRef =
                                db.collection("users").document(documentSnapshot.id)
                            val addCount: Long = documentSnapshot.get("addCount") as Long
                            val starCount: Long = documentSnapshot.get("starsCount") as Long
                            val commCount: Long = documentSnapshot.get("commentsCount") as Long
                            val tmpScore = addCount * 10 + commCount * 3 + starCount
                            val noviPodaci = hashMapOf<String, Any>(
                                "addCount" to (addCount + 1).toString().toLong(),
                                "overallScore" to tmpScore
                            )
                            documentRef.update(noviPodaci)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener { exception ->
                                    Log.w("TAGA", "Error", exception)
                                }
                        }
                    }

            }
            myPlacesViewModel.selected = null
            locationViewModel.setLocation("", "")
            findNavController().popBackStack()
            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }

        cancelButton.setOnClickListener {
            myPlacesViewModel.selected = null
            locationViewModel.setLocation("", "")
            findNavController().popBackStack()

            //findNavController().navigate(R.id.action_EditFragment_to_ListFragment)
        }
        cameraButton.setOnClickListener {
            val cameraPermission = android.Manifest.permission.CAMERA
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
            CAMERA_REQUEST_CODE = 1
            if (!hasCameraPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            } else {
                startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
            }

        }
        galerijaButton.setOnClickListener {
            val galleryPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
            val hasGalleryPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                galleryPermission
            ) == PackageManager.PERMISSION_GRANTED
            GALLERY_REQUEST_CODE = 1
            if (!hasGalleryPermission) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                startActivityForResult(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ), GALLERY_REQUEST_CODE
                )
            }
        }


    }

    private fun getIndex(dropDown: Spinner, type: String?): Int {
        for (i in 0 until dropDown.getCount()) {
            if (dropDown.getItemAtPosition(i).toString() == type) {  //please work
                return i
            }
        }

        return 0
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (CAMERA_REQUEST_CODE == 1) {

                    startActivityForResult(
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                        CAMERA_REQUEST_CODE
                    )
                } else if (GALLERY_REQUEST_CODE == 1) {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        ), GALLERY_REQUEST_CODE
                    )
                }
            }
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView: ImageView = requireView().findViewById<ImageView>(R.id.imgPlace)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val image: Bitmap? = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(image)
            CAMERA_REQUEST_CODE = 0
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            imageView.setImageURI(selectedImage)
            GALLERY_REQUEST_CODE = 0
        }
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
//        menu.setGroupVisible(R.id.menu_group,false)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPlacesViewModel.selected = null
//        _binding = null
    }
}