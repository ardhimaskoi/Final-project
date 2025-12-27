package com.example.hidayahapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtUserEmail: TextView
    private lateinit var btnLogout: Button

    private val REQUEST_IMAGE_PICK = 101
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imgProfile = findViewById(R.id.imgProfile)
        txtUserEmail = findViewById(R.id.txtUserEmail)
        btnLogout = findViewById(R.id.btnLogout)

        userId = auth.currentUser?.uid
        txtUserEmail.text = auth.currentUser?.email ?: "user@mail.com"

        userId?.let { uid ->
            // Buat dokumen jika belum ada
            val userDoc = db.collection("users").document(uid)
            userDoc.get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    val newUser = mapOf(
                        "email" to (auth.currentUser?.email ?: ""),
                        "photoUri" to ""
                    )
                    userDoc.set(newUser)
                        .addOnSuccessListener {
                            loadUserProfile(uid)
                        }
                } else {
                    loadUserProfile(uid)
                }
            }
        }

        imgProfile.setOnClickListener {
            openImageChooser()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(
                this,
                "Berhasil logout",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserProfile(uid: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val photoUriString = document.getString("photoUri")
                if (!photoUriString.isNullOrEmpty()) {
                    val uri = Uri.parse(photoUriString)
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.bg_gray)
                        .into(imgProfile)
                } else {
                    imgProfile.setImageResource(R.drawable.bg_gray)
                }
            }
            .addOnFailureListener {
                imgProfile.setImageResource(R.drawable.bg_gray)
                Toast.makeText(
                    this,
                    "Gagal memuat profil",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            setDataAndType(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

                // Tampilkan gambar ke ImageView
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.bg_gray)
                    .into(imgProfile)

                // Simpan ke Firestore
                userId?.let { uid ->
                    val userData = mapOf(
                        "photoUri" to uri.toString(),
                        "email" to (auth.currentUser?.email ?: "")
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Foto profil berhasil disimpan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal menyimpan foto profil",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }
}
