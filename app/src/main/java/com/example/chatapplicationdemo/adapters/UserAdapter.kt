package com.example.chatapplicationdemo.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.chatapplicationdemo.databinding.ItemContainerUserBinding
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.listeners.UserListeners
import com.example.chatapplicationdemo.models.User

class UserAdapter(
    private val userList : List<User>,
    private val userListeners: UserListeners
) : RecyclerView.Adapter<UserAdapter.UserViewHolderClass>(){




    private fun getUserImage(encodeImage : String) : Bitmap{
        val byte = Base64.decode(encodeImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byte, 0, byte.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolderClass {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_container_user, parent, false)
        return UserViewHolderClass(view)
    }

    override fun onBindViewHolder(holder: UserViewHolderClass, position: Int) {
        val user = userList[position]
        holder.userprofile.setImageBitmap(user.image?.let { getUserImage(it) })
        holder.userName.text = user.name
        holder.useremail.text = user.email

        holder.userLayout.setOnClickListener {
            userListeners.onUserClicked(user)
        }

    }

    override fun getItemCount() = userList.size

    inner class UserViewHolderClass(view : View) : RecyclerView.ViewHolder(view){
        val userprofile = view.findViewById<ImageView>(R.id.userPro)
        val useremail = view.findViewById<TextView>(R.id.userTextEmail)
        val userName = view.findViewById<TextView>(R.id.textName)

        val userLayout = view.findViewById<ConstraintLayout>(R.id.userLayout)

    }


}