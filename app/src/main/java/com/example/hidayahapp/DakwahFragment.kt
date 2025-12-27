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
import com.example.hidayahapp.adapter.CarouselAdapter
import com.example.hidayahapp.adapter.DakwahAdapter
import com.example.hidayahapp.model.Dakwah
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class DakwahFragment : Fragment() {

    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var carouselRecyclerView: RecyclerView
    private lateinit var dakwahAdapter: DakwahAdapter
    private lateinit var searchEditText: EditText

    private val listDakwah = mutableListOf<Dakwah>()
    private val fullListDakwah = mutableListOf<Dakwah>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dakwah, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        fab = view.findViewById(R.id.fab)

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
                filterDakwah(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // no-op
            }
        })

        fab.setOnClickListener {
            val intent = Intent(requireContext(), DakwahActivity::class.java)
            startActivity(intent)
        }

        // Setup carousel RecyclerView (horizontal)
        carouselRecyclerView = view.findViewById(R.id.carouselRecyclerView)
        carouselRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        carouselRecyclerView.adapter = CarouselAdapter(
            listOf(
                R.drawable.dakwah1,
                R.drawable.dakwah2,
                R.drawable.dakwah3
            )
        )

        // Setup dakwah list RecyclerView (vertical)
        recyclerView = view.findViewById(R.id.recyclerViewDakwah)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dakwahAdapter = DakwahAdapter(listDakwah)
        recyclerView.adapter = dakwahAdapter

        ambilDataDakwah()

        return view
    }

    override fun onResume() {
        super.onResume()
        ambilDataDakwah()
    }

    private fun filterDakwah(query: String) {
        val filteredList =
            if (query.isEmpty()) {
                fullListDakwah
            } else {
                fullListDakwah.filter {
                    it.judul
                        ?.contains(query, ignoreCase = true) == true
                }.toMutableList()
            }

        listDakwah.clear()
        listDakwah.addAll(filteredList)
        dakwahAdapter.notifyDataSetChanged()
    }

    private fun ambilDataDakwah() {
        db.collection("dakwah")
            .get()
            .addOnSuccessListener { documents ->
                listDakwah.clear()
                fullListDakwah.clear()

                for (document in documents) {
                    val dakwah = document.toObject(Dakwah::class.java)
                    listDakwah.add(dakwah)
                    fullListDakwah.add(dakwah)
                }

                dakwahAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
