package com.example.chatapplicationdemo.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.listeners.RecentConversationListener
import com.example.chatapplicationdemo.models.ChatMessage
import com.example.chatapplicationdemo.models.User

class RecentConversationAdapter(
    private val chatList: List<ChatMessage>,
    private val onConversationClick : RecentConversationListener
) : RecyclerView.Adapter<RecentConversationAdapter.ConversationHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationHolderClass {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_container_recent_conversation, parent, false)
        return ConversationHolderClass(view)
    }

    override fun onBindViewHolder(holder: ConversationHolderClass, position: Int) {
        val chatMessages = chatList[position]

        holder.recentUserName.text = chatMessages.conversationName ?: "Unknown User"
        holder.recentUserMessage.text = chatMessages.message ?: "No message"
        val profileImage = chatMessages.conversationImage?.let { getConversionImage(it) }
        holder.recentUserProPic.setImageBitmap(profileImage)

        holder.recentLay.setOnClickListener {

            val user = User().apply {
                this.id = chatMessages.conversationId
                this.name = chatMessages.conversationName
                this.image = chatMessages.conversationImage
            }
            onConversationClick.onConversationClick(user)
        }

    }

    override fun getItemCount() = chatList.size

    inner class ConversationHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recentUserProPic: ImageView = itemView.findViewById(R.id.userProRecent)
        val recentUserName: TextView = itemView.findViewById(R.id.textNameRecent)
        val recentUserMessage: TextView = itemView.findViewById(R.id.textRecentMessage)

        val recentLay : ConstraintLayout = itemView.findViewById(R.id.recentConversationsLayout)

    }

    // Decode the base64 image string to Bitmap
    private fun getConversionImage(encodedImage: String): Bitmap {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
          return  BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
