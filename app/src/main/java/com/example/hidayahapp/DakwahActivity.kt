package com.example.hidayahapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class DakwahActivity : AppCompatActivity() {

    private lateinit var judul: EditText
    private lateinit var deskripsi: EditText
    private lateinit var video: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnPilihGambar: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null
    private val REQUEST_IMAGE_PICK = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dakwah)

        // Init view
        judul = findViewById(R.id.editTextJudul)
        deskripsi = findViewById(R.id.editTextDeskripsi)
        video = findViewById(R.id.editTextVideoUrl)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnPilihGambar = findViewById(R.id.btnPilihGambar)
        imagePreview = findViewById(R.id.imagePreview)

        // Ambil data intent (mode edit)
        val idExtra = intent.getStringExtra("id")
        val judulExtra = intent.getStringExtra("judul")
        val deskripsiExtra = intent.getStringExtra("deskripsi")
        val videoExtra = intent.getStringExtra("video")

        if (idExtra != null) {
            judul.setText(judulExtra)
            deskripsi.setText(deskripsiExtra)
            video.setText(videoExtra)
            btnSimpan.text = "Update Data"
        }

        btnPilihGambar.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        btnSimpan.setOnClickListener {
            val judulText = judul.text.toString().trim()
            val deskripsiText = deskripsi.text.toString().trim()
            val videoText = video.text.toString().trim()
            val thumbnailUrl = getThumbnailFromUrl(videoText)

            if (judulText.isEmpty() ||
                deskripsiText.isEmpty() ||
                videoText.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Data tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadGambarToCloudinary(selectedImageUri!!) { imageUrl ->
                    if (imageUrl != null) {
                        simpanKeFirestore(
                            idExtra,
                            judulText,
                            deskripsiText,
                            videoText,
                            thumbnailUrl,
                            imageUrl
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal upload gambar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                simpanKeFirestore(
                    idExtra,
                    judulText,
                    deskripsiText,
                    videoText,
                    thumbnailUrl,
                    ""
                )
            }
        }
    }

    private fun simpanKeFirestore(
        idExtra: String?,
        judul: String,
        deskripsi: String,
        videoUrl: String,
        thumbnailUrl: String,
        gambarUrl: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "judul" to judul,
            "deskripsi" to deskripsi,
            "videoUrl" to videoUrl,
            "thumbnailUrl" to thumbnailUrl,
            "gambarUrl" to gambarUrl
        )

        if (idExtra != null) {
            db.collection("dakwah")
                .document(idExtra)
                .update(data as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Data berhasil diupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Data gagal diupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            val id = System.currentTimeMillis().toString()
            val dakwah = data + ("id" to id)

            db.collection("dakwah")
                .document(id)
                .set(dakwah)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Data berhasil ditambahkan",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Data gagal ditambahkan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun uploadGambarToCloudinary(
        fileUri: Uri,
        callback: (String?) -> Unit
    ) {
        val file = getFileFromUri(fileUri)
        if (file == null) {
            callback(null)
            return
        }

        val cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "dvxrarx3e",
                "api_key", "539844321735987",
                "api_secret", "quhDbxWuCtcVIQ4mix7e9D3uhDs"
            )
        )

        Thread {
            try {
                val result =
                    cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                val imageUrl = result["secure_url"] as String
                runOnUiThread { callback(imageUrl) }
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
            imagePreview.setImageURI(selectedImageUri)
        }
    }

    private fun getThumbnailFromUrl(videoUrl: String): String {
        val videoId = videoUrl
            .substringAfter("v=")
            .substringBefore("&")
        return "https://img.youtube.com/vi/$videoId/0.jpg"
    }
}
