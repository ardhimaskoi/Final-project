package com.example.hidayahapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Calendar

class EventActivity : AppCompatActivity() {

    private lateinit var inputJudul: EditText
    private lateinit var inputDeskripsi: EditText
    private lateinit var inputTanggal: EditText
    private lateinit var btnPilihGambar: Button
    private lateinit var imagePreview: ImageView
    private lateinit var btnSimpan: Button

    private val REQUEST_IMAGE_PICK = 101
    private var selectedImageUri: Uri? = null
    private var oldImageUriString: String? = null
    private var id: String? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        inputJudul = findViewById(R.id.inputJudul)
        inputDeskripsi = findViewById(R.id.inputDeskripsi)
        inputTanggal = findViewById(R.id.inputTanggal)
        btnPilihGambar = findViewById(R.id.btnPilihGambar)
        imagePreview = findViewById(R.id.imagePreview)
        btnSimpan = findViewById(R.id.btnSimpan)

        id = intent.getStringExtra("id")

        if (id != null) {
            loadEventData(id!!)
            btnSimpan.text = "Update"
        }

        btnSimpan.setOnClickListener {
            simpanEvent()
        }

        inputTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, y, m, d ->
                    inputTanggal.setText(
                        String.format("%02d-%02d-%04d", d, m + 1, y)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
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
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK &&
            resultCode == RESULT_OK &&
            data != null
        ) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .placeholder(R.drawable.bg_gray)
                .into(imagePreview)
        }
    }

    private fun loadEventData(eventId: String) {
        db.collection("event")
            .document(eventId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    inputJudul.setText(document.getString("judul") ?: "")
                    inputDeskripsi.setText(document.getString("deskripsi") ?: "")
                    inputTanggal.setText(document.getString("tanggal") ?: "")
                    oldImageUriString = document.getString("gambarUrl")

                    if (!oldImageUriString.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(Uri.parse(oldImageUriString))
                            .placeholder(R.drawable.bg_gray)
                            .into(imagePreview)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Gagal memuat data event",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun simpanEvent() {
        val judul = inputJudul.text.toString().trim()
        val deskripsi = inputDeskripsi.text.toString().trim()
        val tanggal = inputTanggal.text.toString().trim()

        if (judul.isEmpty() || deskripsi.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(
                this,
                "Judul, deskripsi, dan tanggal harus diisi",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when {
            selectedImageUri != null -> {
                uploadGambarToCloudinary(selectedImageUri!!) { url ->
                    if (url != null) {
                        simpanKeFirestore(judul, deskripsi, tanggal, url)
                    } else {
                        Toast.makeText(
                            this,
                            "Gagal upload gambar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            !oldImageUriString.isNullOrEmpty() -> {
                simpanKeFirestore(
                    judul,
                    deskripsi,
                    tanggal,
                    oldImageUriString!!
                )
            }

            else -> {
                Toast.makeText(
                    this,
                    "Harap pilih gambar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun simpanKeFirestore(
        judul: String,
        deskripsi: String,
        tanggal: String,
        gambarUrl: String
    ) {
        val event = hashMapOf(
            "judul" to judul,
            "deskripsi" to deskripsi,
            "tanggal" to tanggal,
            "gambarUrl" to gambarUrl
        )

        if (id != null) {
            db.collection("event")
                .document(id!!)
                .update(event as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Event berhasil diupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Gagal update event: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            db.collection("event")
                .add(event)
                .addOnSuccessListener { docRef ->
                    db.collection("event")
                        .document(docRef.id)
                        .update("id", docRef.id)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Event berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Gagal menyimpan ID event",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Gagal menambahkan event: ${e.message}",
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
