package com.example.cheapsleep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.example.cheapsleep.model.UserDbModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LogInActivity : AppCompatActivity() {
    private lateinit var userDbModel: UserDbModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDbModel = ViewModelProvider(this)[UserDbModel::class.java]

        setContentView(R.layout.activity_log_in)

        var editUserName: EditText = findViewById(R.id.loginUserNameET)
        var editPassword: EditText = findViewById(R.id.loginSifraET)
        var db = Firebase.firestore

        var loginBtn: Button = findViewById(R.id.loginBtnLog)
        loginBtn.setOnClickListener {
            var userName: String = editUserName.text.toString()
            var password: String = editPassword.text.toString()
            if (userName.equals(""))
                Toast.makeText(
                    this@LogInActivity,
                    "Username missing",
                    Toast.LENGTH_SHORT
                ).show()
            else if (password.equals(""))
                Toast.makeText(this@LogInActivity, "Password missing", Toast.LENGTH_SHORT)
                    .show()
            else {
                try {
                    //create user
                    lifecycleScope.launch{
                        var exists = true
                        withContext(Dispatchers.IO){
                            exists=userDbModel.loginUser(userName,password)
                        }
                        if (!exists) {
                            Toast.makeText(this@LogInActivity, "Username or password are incorrect", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                this@LogInActivity,
                                "Log in successful ",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@LogInActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }


                } catch (e: java.lang.Exception) {
                    Toast.makeText(this@LogInActivity, e.toString(), Toast.LENGTH_SHORT).show()
                    Log.w("TAGA", "Greska", e)
                }

            }
        }

        var registerButton: Button = findViewById(R.id.loginbtnRegister)
        registerButton.setOnClickListener {
            var intent: Intent = Intent(this@LogInActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}