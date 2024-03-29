package com.example.cheapsleep

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.databinding.ActivityRegisterBinding
import com.example.cheapsleep.model.PlacesListView
import com.example.cheapsleep.model.UserDbModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class RegisterActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding
    private var CAMERA_REQUEST_CODE = 0
    private var GALLERY_REQUEST_CODE = 0
    private var db = Firebase.firestore
    private var storage = Firebase.storage

    //    private var userDbModel: UserDbModel by activityViewModel()
    private lateinit var userDbModel: UserDbModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDbModel = ViewModelProvider(this)[UserDbModel::class.java]
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var storageRef = storage.reference

        var editName: EditText = findViewById(R.id.regetname)
        var editSurname: EditText = findViewById(R.id.regetprezime)
        var editUserName: EditText = findViewById(R.id.regetusername)
        var editPassword: EditText = findViewById(R.id.regetpassword)
        var editPhone: EditText = findViewById(R.id.regetphone)

        var closeButton: Button = findViewById(R.id.regbtnclose)
        var okButton: Button = findViewById(R.id.regbtnReg)

        var cameraButton: Button = findViewById(R.id.regbtnKamera)
        var galerijaButton: Button = findViewById(R.id.regbtnGalerija)


        closeButton.setOnClickListener {
            var intent = Intent(this@RegisterActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()

        }
        cameraButton.setOnClickListener {
            val cameraPermission = android.Manifest.permission.CAMERA
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                this,
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
                this,
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

        okButton.setOnClickListener {

            var userName: String = editUserName.text.toString()
            var password: String = editPassword.text.toString()
            var name: String = editName.text.toString()
            var surname: String = editSurname.text.toString()
            var phone: String = editPhone.text.toString()
            if (userName.equals(""))
                Toast.makeText(this, "Username missing", Toast.LENGTH_SHORT).show()
            else if (password.equals(""))
                Toast.makeText(this, "Password missing", Toast.LENGTH_SHORT).show()
            else if (name.equals(""))
                Toast.makeText(this, "Name missing", Toast.LENGTH_SHORT).show()
            else if (surname.equals(""))
                Toast.makeText(this, "Surname missing", Toast.LENGTH_SHORT).show()
            else if (phone.equals(""))
                Toast.makeText(this, "Phone number missing", Toast.LENGTH_SHORT).show()
            else {
                var user: User = User(
                    editUserName.text.toString(),
                    editPassword.text.toString(),
                    editName.text.toString(),
                    editSurname.text.toString(),
                    editPhone.text.toString(),
                    "users/" + editUserName.text.toString() + ".jpg",
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    ""
                )
                try {
                    lifecycleScope.launch{
                        var exists = true
                        withContext(Dispatchers.IO){
                            exists=userDbModel.userExists(user.username)
                        }
                        if (!exists) {
                            Toast.makeText(this@RegisterActivity, "Username taken", Toast.LENGTH_SHORT).show()
                        } else {
                            userDbModel.registerUser(user,  binding.imgUser)
                            Toast.makeText(
                                this@RegisterActivity,
                                "Successfull registration ",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }


                } catch (e: java.lang.Exception) {
                    Toast.makeText(this@RegisterActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    Log.w("TAGA", "Greska", e)
                }
            }
        }
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
        val imageView: ImageView = findViewById<ImageView>(R.id.imgUser)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val image: Bitmap? = data.extras?.get("data") as Bitmap
            imageView.setImageBitmap(image)
            CAMERA_REQUEST_CODE = 0
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedImage: Uri? = data.data
            imageView.setImageURI(selectedImage)
            GALLERY_REQUEST_CODE = 0
        }
    }


}