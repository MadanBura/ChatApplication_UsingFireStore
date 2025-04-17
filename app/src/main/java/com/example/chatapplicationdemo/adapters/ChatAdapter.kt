package com.example.chatapplicationdemo.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.models.ChatMessage

class ChatAdapter(
    private val chatList: List<ChatMessage>,
    private var receiverProfilePic : Bitmap,
    private val senderId: String

) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }




    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_sentmessage, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_container_messagereceived, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatList[position]

        if (holder is SentMessageViewHolder) {
            holder.sentMessage.text = message.message
            holder.dateTime.text = message.dateTime
        } else if (holder is ReceivedMessageViewHolder) {
            holder.receivedMessage.text = message.message
            holder.dateTime.text = message.dateTime
            holder.profilePic.setImageBitmap(receiverProfilePic)

        }
    }

    override fun getItemCount(): Int = chatList.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.textMessageSent)
        val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.textMessage)
        val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val profilePic : ImageView = itemView.findViewById(R.id.chatuserPro)
    }


//    public fun setReceiverProfileImage(bitmap: Bitmap){
//        receiverProfilePic = bitmap
//    }

}
