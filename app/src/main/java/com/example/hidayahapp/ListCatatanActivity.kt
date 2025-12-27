package com.example.hidayahapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hidayahapp.adapter.CatatanAdapter
import com.example.hidayahapp.model.Catatan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListCatatanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var catatanList: MutableList<Catatan>
    private lateinit var adapter: CatatanAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var uid: String
    private lateinit var buttonTambah: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_catatan)

        recyclerView = findViewById(R.id.recyclerView)
        buttonTambah = findViewById(R.id.fab)

        catatanList = mutableListOf()
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        databaseRef = FirebaseDatabase
            .getInstance()
            .getReference("catatan")
            .child(uid)

        adapter = CatatanAdapter(this, catatanList, uid)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonTambah.setOnClickListener {
            val intent = Intent(this, CatatanActivity::class.java)
            startActivity(intent)
        }

        ambilDataCatatan()
    }

    private fun ambilDataCatatan() {
        databaseRef.addValueEventListener(object : ValueEventListener {

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                catatanList.clear()

                for (catatanSnapshot in snapshot.children) {
                    val catatan =
                        catatanSnapshot.getValue(Catatan::class.java)
                    catatan?.let {
                        it.key = catatanSnapshot.key
                        catatanList.add(it)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // tidak diubah (logic sama)
            }
        })
    }
}
