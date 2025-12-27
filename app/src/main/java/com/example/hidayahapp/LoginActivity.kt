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

class LoginActivity : AppCompatActivity() {

    private lateinit var textEmail: EditText
    private lateinit var textPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

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
        setContentView(R.layout.activity_login)

        textEmail = findViewById(R.id.email)
        textPassword = findViewById(R.id.password)
        loginButton = findViewById(R.id.btn_login)
        registerButton = findViewById(R.id.btn_register)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Logging")
            setMessage("Mohon tunggu...")
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            if (
                textEmail.text.toString().isNotEmpty() &&
                textPassword.text.toString().isNotEmpty()
            ) {
                processLogin()
            } else {
                Toast.makeText(
                    this,
                    "Lengkapi data dengan benar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun processLogin() {
        val email = textEmail.text.toString().trim()
        val password = textPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Email dan password harus diisi",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        progressDialog.show()

        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    val intent =
                        Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: "Login gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
