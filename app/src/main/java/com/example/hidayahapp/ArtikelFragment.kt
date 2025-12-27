package com.example.hidayahapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hidayahapp.adapter.ArtikelAdapter
import com.example.hidayahapp.model.Artikel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ArtikelFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var artikelList: MutableList<Artikel>
    private lateinit var filteredList: MutableList<Artikel>
    private lateinit var adapter: ArtikelAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var searchEditText: EditText

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_artikel, container, false)

        // Init UI
        recyclerView = view.findViewById(R.id.recyclerViewArtikel)
        searchEditText = view.findViewById(R.id.editTextSearchArtikel)
        fab = view.findViewById(R.id.fab)

        // Setup RecyclerView
        artikelList = mutableListOf()
        filteredList = mutableListOf()
        adapter = ArtikelAdapter(filteredList)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            val intent = Intent(requireContext(), ArtikelActivity::class.java)
            startActivity(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // no-op
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterArtikel(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // no-op
            }
        })

        ambilDataArtikel()

        return view
    }

    override fun onResume() {
        super.onResume()
        ambilDataArtikel()
    }

    private fun ambilDataArtikel() {
        db.collection("artikel")
            .get()
            .addOnSuccessListener { result ->
                artikelList.clear()
                for (document in result) {
                    val artikel = document.toObject(Artikel::class.java)
                    artikelList.add(artikel)
                }
                filterArtikel(searchEditText.text.toString())
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun filterArtikel(query: String) {
        val lowerQuery = query.lowercase()
        filteredList.clear()
        filteredList.addAll(
            artikelList.filter { artikel ->
                artikel.judul
                    ?.lowercase()
                    ?.contains(lowerQuery) == true
            }
        )
        adapter.notifyDataSetChanged()
    }
}
