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
import com.example.hidayahapp.ArtikelActivity
import com.example.hidayahapp.R
import com.example.hidayahapp.model.Artikel
import com.google.firebase.firestore.FirebaseFirestore

class ArtikelAdapter(
    private val list: MutableList<Artikel>
) : RecyclerView.Adapter<ArtikelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgArtikel: ImageView = view.findViewById(R.id.imgArtikel)
        val txtJudul: TextView = view.findViewById(R.id.txtJudul)
        val txtDeskripsi: TextView = view.findViewById(R.id.txtDeskripsi)
        val btnEdit: TextView = view.findViewById(R.id.btnEdit)
        val btnHapus: TextView = view.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artikel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val artikel = list[position]
        val context = holder.itemView.context
        val gambarUriString = artikel.gambarUrl

        holder.txtJudul.text = artikel.judul
        holder.txtDeskripsi.text = artikel.deskripsi

        // Load gambar lokal menggunakan Glide
        if (!gambarUriString.isNullOrEmpty()) {
            Glide.with(context)
                .load(Uri.parse(gambarUriString))
                .placeholder(R.drawable.bg_gray)
                .into(holder.imgArtikel)
        } else {
            holder.imgArtikel.setImageResource(R.drawable.bg_gray)
        }

        // Tombol edit
        holder.btnEdit.setOnClickListener {
            val intent = Intent(context, ArtikelActivity::class.java).apply {
                putExtra("id", artikel.id)
            }
            context.startActivity(intent)
        }

        // Tombol hapus
        holder.btnHapus.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val currentPos = holder.adapterPosition

            if (currentPos != RecyclerView.NO_POSITION) {
                db.collection("artikel")
                    .document(artikel.id)
                    .delete()
                    .addOnSuccessListener {
                        list.removeAt(currentPos)
                        notifyItemRemoved(currentPos)
                        notifyItemRangeChanged(currentPos, list.size)
                        Toast.makeText(
                            context,
                            "Artikel berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Gagal menghapus artikel",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
