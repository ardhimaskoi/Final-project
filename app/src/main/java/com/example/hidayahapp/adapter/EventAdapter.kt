package com.example.hidayahapp.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hidayahapp.EventActivity
import com.example.hidayahapp.R
import com.example.hidayahapp.model.Event
import com.google.firebase.firestore.FirebaseFirestore

class EventAdapter(
    private val list: MutableList<Event>
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgEvent: ImageView = view.findViewById(R.id.imgEvent)
        val txtJudul: TextView = view.findViewById(R.id.txtJudul)
        val txtDeskripsi: TextView = view.findViewById(R.id.txtDeskripsi)
        val txtTanggal: TextView = view.findViewById(R.id.txtTanggal)
        val btnEdit: TextView = view.findViewById(R.id.btnEdit)
        val btnHapus: TextView = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val event = list[position]

        holder.txtJudul.text = event.judul
        holder.txtDeskripsi.text = event.deskripsi
        holder.txtTanggal.text = event.tanggal

        if (!event.gambarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(event.gambarUrl))
                .placeholder(R.drawable.bg_gray)
                .error(R.drawable.bg_gray)
                .into(holder.imgEvent)
        } else {
            holder.imgEvent.setImageResource(R.drawable.dakwah1)
        }

        holder.btnEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EventActivity::class.java).apply {
                putExtra("id", event.id)
                putExtra("judul", event.judul)
                putExtra("deskripsi", event.deskripsi)
                putExtra("tanggal", event.tanggal)
                putExtra("gambarUrl", event.gambarUrl)
            }
            context.startActivity(intent)
        }

        holder.btnHapus.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("event")
                .document(event.id)
                .delete()
                .addOnSuccessListener {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, list.size)
                    Toast.makeText(
                        holder.itemView.context,
                        "Event berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        holder.itemView.context,
                        "Gagal menghapus event",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
