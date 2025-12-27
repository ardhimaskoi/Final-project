package com.example.hidayahapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var textFullName: EditText
    private lateinit var textEmail: EditText
    private lateinit var textPassword: EditText
    private lateinit var textPasswordConf: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    private lateinit var progressDialog: ProgressDialog
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        textFullName = findViewById(R.id.full_name)
        textEmail = findViewById(R.id.email)
        textPassword = findViewById(R.id.password)
        textPasswordConf = findViewById(R.id.password_conf)
        registerButton = findViewById(R.id.register)
        loginButton = findViewById(R.id.login)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Register")
            setMessage("Mohon tunggu...")
        }

        registerButton.setOnClickListener {
            if (
                textFullName.text.toString().isNotEmpty() &&
                textEmail.text.toString().isNotEmpty() &&
                textPassword.text.toString().isNotEmpty() &&
                textPasswordConf.text.toString().isNotEmpty()
            ) {
                if (textPassword.text.toString() ==
                    textPasswordConf.text.toString()
                ) {
                    processRegister()
                } else {
                    Toast.makeText(
                        this,
                        "Password tidak sama",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Lengkapi data dengan benar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun processRegister() {
        val fullName = textFullName.text.toString()
        val email = textEmail.text.toString()
        val password = textPassword.text.toString()

        progressDialog.show()

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    val profileUpdate =
                        userProfileChangeRequest {
                            displayName = fullName
                        }

                    user?.updateProfile(profileUpdate)
                        ?.addOnCompleteListener { updateTask ->
                            progressDialog.dismiss()
                            if (updateTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Register Berhasil",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    updateTask.exception?.message
                                        ?: "Gagal update profil",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        task.exception?.message
                            ?: "Register gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
