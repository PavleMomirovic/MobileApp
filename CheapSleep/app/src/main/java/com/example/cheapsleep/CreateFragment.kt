package com.example.cheapsleep

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cheapsleep.data.Place
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.FragmentCreateBinding
import com.example.cheapsleep.model.LocationViewModel
import com.example.cheapsleep.model.PlacesDbModel
import com.example.cheapsleep.model.PlacesListView
import com.example.cheapsleep.model.UserDbModel
import kotlinx.coroutines.launch
import java.util.*

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val myPlacesViewModel: PlacesListView by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var userDbModel: UserDbModel
    private lateinit var placesDbModel: PlacesDbModel


    private var CAMERA_REQUEST_CODE = 0
    private var GALLERY_REQUEST_CODE = 0

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        userDbModel = ViewModelProvider(this)[UserDbModel::class.java] //maybe this needs to be moved in onCreate
        placesDbModel = ViewModelProvider(this)[PlacesDbModel::class.java]

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
            )

            viewLifecycleOwner.lifecycleScope.launch() {
                try {

                    val path = myPlacesViewModel.selected?.imageUrl.toString()
                    placesDbModel.getPlaceImg(path,requireContext(),imageView)

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
            var longitude: String = editLongitude.text.toString()
            if (longitude.isEmpty())
                longitude="20"

            var latitude: String = editLatitude.text.toString()
            if (latitude.isEmpty())
                latitude="40"
            val price: String = editPrice.text.toString()
            typeSelected = dropDown.selectedItem.toString()

            if (myPlacesViewModel.selected != null) {

                //EDIT MODE:

                val place = Place(
                    name,
                    desc,
                    "",
                    "",
                    price,
                    typeSelected,
                    "",
                    Date(),
                    myPlacesViewModel.selected?.imageUrl!!,
                    HashMap(),
                    HashMap(),
                    myPlacesViewModel.selected?.id!!
                    )
                try{

                    placesDbModel.editPlace(place,imageView)

                    Toast.makeText(requireContext(),
                        "Place information updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    myPlacesViewModel.selected?.name = name
                    myPlacesViewModel.selected?.description = desc
                    myPlacesViewModel.selected?.longitude = longitude
                    myPlacesViewModel.selected?.latitude = latitude
                    myPlacesViewModel.selected?.price = price
                    myPlacesViewModel.selected?.type = typeSelected
                    myPlacesViewModel.selected?.author = UserObject.username.toString()
                    myPlacesViewModel.selected?.date = Date()

                }catch (e: java.lang.Exception) {
                    Toast.makeText(requireContext(),
                        "An Error occurred while trying to edit this place",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w("TAGA", "Greska", e)
                }
            } else {

                //CREATE MODE:

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

                placesDbModel.createPlace(myPlace,imageView)

                userDbModel.updateUserScore(UserObject.username.toString(),true,false,false)

            }
            myPlacesViewModel.selected = null
            locationViewModel.setLocation("", "")
            findNavController().popBackStack()
        }

        cancelButton.setOnClickListener {
            myPlacesViewModel.selected = null
            locationViewModel.setLocation("", "")
            findNavController().popBackStack()
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPlacesViewModel.selected = null
    }
}