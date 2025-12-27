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
import com.example.hidayahapp.DakwahActivity
import com.example.hidayahapp.R
import com.example.hidayahapp.model.Dakwah
import com.google.firebase.firestore.FirebaseFirestore

class DakwahAdapter(
    private val dakwahList: MutableList<Dakwah>
) : RecyclerView.Adapter<DakwahAdapter.DakwahViewHolder>() {

    inner class DakwahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val ivUstadz: ImageView = itemView.findViewById(R.id.ivUstadz)
        val judul: TextView = itemView.findViewById(R.id.tvJudul)
        val deskripsi: TextView = itemView.findViewById(R.id.tvDeskripsi)
        val tvPlayVideo: TextView = itemView.findViewById(R.id.tvPlayVideo)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
        val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DakwahViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dakwah, parent, false)
        return DakwahViewHolder(itemView)
    }

    override fun getItemCount(): Int = dakwahList.size

    override fun onBindViewHolder(
        holder: DakwahViewHolder,
        position: Int
    ) {
        val data = dakwahList[position]

        holder.judul.text = data.judul
        holder.deskripsi.text = data.deskripsi

        Glide.with(holder.itemView.context)
            .load(data.thumbnailUrl)
            .placeholder(R.drawable.bg_gray)
            .into(holder.thumbnail)

        // Load foto ustadz jika ada
        if (!data.gambarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(data.gambarUrl)
                .placeholder(R.drawable.bg_gray)
                .circleCrop()
                .into(holder.ivUstadz)
        } else {
            holder.ivUstadz.setImageResource(R.drawable.bg_gray)
        }

        holder.tvPlayVideo.setOnClickListener {
            val videoId = ambilVideoId(data.videoUrl)
            if (videoId.isNotEmpty()) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                )
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Link video tidak valid.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btnDelete.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val dakwahId = data.id ?: ""

            if (dakwahId.isNotEmpty()) {
                db.collection("dakwah")
                    .document(dakwahId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            holder.itemView.context,
                            "Data berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        dakwahList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            holder.itemView.context,
                            "Gagal menghapus data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "ID data tidak ditemukan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btnEdit.setOnClickListener {
            val intent = Intent(
                holder.itemView.context,
                DakwahActivity::class.java
            ).apply {
                putExtra("id", data.id)
                putExtra("judul", data.judul)
                putExtra("deskripsi", data.deskripsi)
                putExtra("video", data.videoUrl)
                putExtra("fotoUstadzUrl", data.gambarUrl)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun ambilVideoId(url: String): String {
        return url.substringAfter("v=").substringBefore("&")
    }
}
