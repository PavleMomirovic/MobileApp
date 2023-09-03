package com.example.cheapsleep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cheapsleep.data.User
import com.example.cheapsleep.data.UserObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                Toast.makeText(this@LogInActivity, "Password incorrect", Toast.LENGTH_SHORT)
                    .show()
            else {

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            db.collection("users")
                                .whereEqualTo("username", userName)
                                .get()
                                .await()
                        }


                        if (!result.isEmpty) {
                            for (document in result.documents) {
                                if (document != null)
                                    if (document.data?.get("password").toString()
                                            .equals(password)
                                    ) {
                                        val intent =
                                            Intent(this@LogInActivity, MainActivity::class.java)

                                        var user: User = User(
                                            userName,
                                            password,
                                            document.data?.get("firstname").toString(),
                                            document.data?.get("lastname").toString(),
                                            document.data?.get("phoneNumber").toString(),
                                            document.data?.get("url").toString(),
                                            (document.data?.get("addCount") as? Number)?.toDouble()
                                                ?: 0.0,
                                            (document.data?.get("starsCount") as? Number)?.toDouble()
                                                ?: 0.0,
                                            (document.data?.get("commentsCount") as? Number)?.toDouble()
                                                ?: 0.0,
                                            (document.data?.get("overallScore") as? Number)?.toDouble()
                                                ?:0.0,
                                            document.reference.id.toString()
                                        )

                                        UserObject.apply {
                                            this.username = user.username
                                            this.password = user.password
                                            this.name = user.name
                                            this.surname = user.surname
                                            this.addCount = user.addCount
                                            this.commentsCount = user.commentsCount
                                            this.startCount = user.startCount


                                        }

                                        startActivity(intent)
                                        finish()
                                    } else
                                        Toast.makeText(
                                            this@LogInActivity,
                                            "Password incorrect",
                                            Toast.LENGTH_SHORT
                                        ).show()
                            }

                        } else
                            Toast.makeText(
                                this@LogInActivity,
                                "There is no user named: "+userName.toString(),
                                Toast.LENGTH_SHORT
                            ).show()

                    } catch (e: java.lang.Exception) {
                        Log.w("TAGA", "Error", e)
                    }
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