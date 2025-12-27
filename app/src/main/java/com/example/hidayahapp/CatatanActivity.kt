package com.example.hidayahapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.hidayahapp.model.Catatan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class CatatanActivity : AppCompatActivity() {

    private lateinit var btnSimpan: Button
    private lateinit var judul: EditText
    private lateinit var isi: EditText
    private lateinit var btnAddGambar: Button
    private lateinit var layoutGambar: LinearLayout

    private val gambarList = mutableListOf<String>()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catatan)

        btnSimpan = findViewById(R.id.btn_simpan)
        judul = findViewById(R.id.catatanJudul)
        isi = findViewById(R.id.catatanIsi)
        btnAddGambar = findViewById(R.id.btn_add_image)
        layoutGambar = findViewById(R.id.layoutGambar)

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("catatan")
        val currentUser = FirebaseAuth.getInstance()
        val uid = currentUser.uid
        val key = intent.getStringExtra("key")

        // Load data catatan jika edit
        if (key != null && uid != null) {
            val catatanRef = ref.child(uid).child(key)
            catatanRef.get().addOnSuccessListener {
                val catatan = it.getValue(Catatan::class.java)
                if (catatan != null) {
                    judul.setText(catatan.judul)
                    isi.setText(catatan.isi)
                    gambarList.clear()
                    gambarList.addAll(catatan.gambarList ?: emptyList())
                    tampilkanGambar()
                }
            }
        }

        btnAddGambar.setOnClickListener {
            openImageChooser()
        }

        btnSimpan.setOnClickListener {
            val judulCatatan = judul.text.toString().trim()
            val isiCatatan = isi.text.toString().trim()

            if (judulCatatan.isNotEmpty() && isiCatatan.isNotEmpty() && uid != null) {
                val userRef = ref.child(uid)

                if (key != null) {
                    // Update catatan lama
                    val catatanUpdate = Catatan(
                        uid = uid,
                        judul = judulCatatan,
                        isi = isiCatatan,
                        key = key,
                        gambarList = gambarList.toList()
                    )

                    userRef.child(key)
                        .setValue(catatanUpdate)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Catatan berhasil diupdate",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(this, ListCatatanActivity::class.java)
                            )
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal mengupdate catatan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Simpan catatan baru
                    val catatanBaru = Catatan(
                        uid = uid,
                        judul = judulCatatan,
                        isi = isiCatatan,
                        key = null,
                        gambarList = gambarList.toList()
                    )

                    val catatanRef = userRef.push()
                    catatanBaru.key = catatanRef.key

                    catatanRef
                        .setValue(catatanBaru)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Catatan berhasil disimpan",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(this, ListCatatanActivity::class.java)
                            )
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal menyimpan: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(
                    this,
                    "Judul dan isi catatan harus diisi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                Toast.makeText(
                    this,
                    "Mengunggah gambar ke Cloudinary...",
                    Toast.LENGTH_SHORT
                ).show()

                uploadToCloudinary(imageUri) { url ->
                    if (url != null) {
                        gambarList.add(url)
                        tampilkanGambar()
                        Toast.makeText(
                            this,
                            "Gambar berhasil diunggah",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal mengunggah gambar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun tampilkanGambar() {
        layoutGambar.removeAllViews()

        for (uriString in gambarList) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this)
                .load(uriString)
                .into(imageView)

            layoutGambar.addView(imageView)
        }
    }

    private fun uploadToCloudinary(
        uri: Uri,
        callback: (String?) -> Unit
    ) {
        val file = getFileFromUri(uri)
        if (file == null) {
            callback(null)
            return
        }

        val config = hashMapOf(
            "cloud_name" to "dvxrarx3e",
            "api_key" to "539844321735987",
            "api_secret" to "quhDbxWuCtcVIQ4mix7e9D3uhDs"
        )

        val cloudinary = Cloudinary(config)

        Thread {
            try {
                val result =
                    cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                val url = result["secure_url"] as String
                runOnUiThread { callback(url) }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { callback(null) }
            }
        }.start()
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream =
                contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile(
                "upload",
                ".jpg",
                cacheDir
            )
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
