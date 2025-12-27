package com.example.hidayahapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hidayahapp.CatatanActivity
import com.example.hidayahapp.R
import com.example.hidayahapp.model.Catatan
import com.google.firebase.database.FirebaseDatabase

class CatatanAdapter(
    private val context: Context,
    private val catatanList: MutableList<Catatan>,
    private val uid: String
) : RecyclerView.Adapter<CatatanAdapter.CatatanViewHolder>() {

    inner class CatatanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val judul: TextView = view.findViewById(R.id.tvJudul)
        val isi: TextView = view.findViewById(R.id.tvIsi)
        val btnHapus: Button = view.findViewById(R.id.btnDelete)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val layoutGambar: LinearLayout =
            view.findViewById(R.id.layoutGambar) // container gambar
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatatanViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_catatan, parent, false)
        return CatatanViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CatatanViewHolder,
        position: Int
    ) {
        val catatan = catatanList[position]

        holder.judul.text = catatan.judul
        holder.isi.text = catatan.isi

        // Bersihkan layout gambar agar tidak duplikat saat recycle
        holder.layoutGambar.removeAllViews()

        // Tampilkan semua gambar di bawah isi
        catatan.gambarList?.forEach { url ->
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(context)
                .load(url)
                .into(imageView)

            holder.layoutGambar.addView(imageView)
        }

        holder.btnHapus.setOnClickListener {
            hapusCatatan(catatan.key, position)
        }

        holder.btnEdit.setOnClickListener {
            editCatatan(catatan)
        }
    }

    override fun getItemCount(): Int = catatanList.size

    @SuppressLint("NotifyDataSetChanged")
    private fun hapusCatatan(
        key: String?,
        position: Int
    ) {
        if (key != null) {
            val database = FirebaseDatabase.getInstance()
            val ref = database
                .getReference("catatan")
                .child(uid)
                .child(key)

            ref.removeValue()
                .addOnSuccessListener {
                    catatanList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, catatanList.size)
                    Toast.makeText(
                        context,
                        "Catatan dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Gagal menghapus catatan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun editCatatan(catatan: Catatan) {
        val intent = Intent(context, CatatanActivity::class.java).apply {
            putExtra("key", catatan.key)
        }
        context.startActivity(intent)
    }
}
