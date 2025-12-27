package com.example.hidayahapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hidayahapp.R
import com.example.hidayahapp.model.User

class UserAdapter(
    private val userList: List<User>,
    private val onLogoutClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val txtUserEmail: TextView = itemView.findViewById(R.id.txtUserEmail)
        val btnLogout: View = itemView.findViewById(R.id.btnLogout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_profile, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val user = userList[position]

        holder.txtUserEmail.text = user.email

        if (!user.photoUri.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(user.photoUri))
                .placeholder(R.drawable.bg_gray)
                .into(holder.imgProfile)
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_gray)
        }

        holder.btnLogout.setOnClickListener {
            onLogoutClick(user)
        }
    }

    override fun getItemCount(): Int = userList.size
}
