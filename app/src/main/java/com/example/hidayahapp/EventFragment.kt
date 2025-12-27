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
import com.example.hidayahapp.adapter.EventAdapter
import com.example.hidayahapp.model.Event
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class EventFragment : Fragment() {

    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: EventAdapter

    private val eventList = mutableListOf<Event>()
    private val fullEventList = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        searchEditText = view.findViewById(R.id.editTextSearchEvent)
        recyclerView = view.findViewById(R.id.recyclerViewEvent)
        fab = view.findViewById(R.id.fab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(eventList)
        recyclerView.adapter = adapter

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
                filterEvent(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // no-op
            }
        })

        fab.setOnClickListener {
            val intent = Intent(requireContext(), EventActivity::class.java)
            startActivity(intent)
        }

        ambilDataEvent()

        return view
    }

    override fun onResume() {
        super.onResume()
        ambilDataEvent()
    }

    private fun ambilDataEvent() {
        db.collection("event")
            .get()
            .addOnSuccessListener { result ->
                eventList.clear()
                fullEventList.clear()

                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    eventList.add(event)
                    fullEventList.add(event)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengambil data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun filterEvent(query: String) {
        val filtered =
            if (query.isEmpty()) {
                fullEventList
            } else {
                fullEventList.filter {
                    it.judul
                        ?.contains(query, ignoreCase = true) == true
                }.toMutableList()
            }

        eventList.clear()
        eventList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}
