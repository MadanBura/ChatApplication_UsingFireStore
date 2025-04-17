package com.example.chatapplicationdemo.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.chatapplicationdemo.adapters.ChatAdapter
import com.example.chatapplicationdemo.databinding.ActivityChatBinding
import com.example.chatapplicationdemo.firebase.FirebaseNotificationSender
import com.example.chatapplicationdemo.listeners.UserListeners
import com.example.chatapplicationdemo.models.ChatMessage
import com.example.chatapplicationdemo.models.User
import com.example.chatapplicationdemo.utility.Constants
import com.example.chatapplicationdemo.utility.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatActivity : BaseActivity(), UserListeners {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receivedUser: User
    private lateinit var preferenceManager: PreferenceManager
    private val chatMessageList: MutableList<ChatMessage> = mutableListOf()
    private lateinit var database: FirebaseFirestore
    private lateinit var chatAdapter: ChatAdapter

    private var conversationId: String? = null
    private var isReceiverAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedUser = intent.getSerializableExtra(Constants.KEY_USER) as User

        init()
        loadReceiverDetails()
        setListeners()
        listenMessages()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun listenAvailabilityOfReceiver() {

        val receiverUserId = receivedUser.id

        if (receiverUserId != null) {
            database.collection(Constants.KEY_COLLECTION_USER).document(receiverUserId)
                .addSnapshotListener(this@ChatActivity) { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (value != null) {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
//                            val availability : Int =
//                                Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY))
//                                    ?.toInt() ?: 0
                            val availability: Int =
                                value.getLong(Constants.KEY_AVAILABILITY)?.toInt() ?: 0

                            isReceiverAvailable = availability == 1
                        }
                        receivedUser.token = value.getString(Constants.KEY_FCM_TOKEN)
                        if (receivedUser.image == null) {
                            receivedUser.image = value.getString(Constants.KEY_IMAGE)
                            chatAdapter.notifyItemRangeChanged(0, chatMessageList.size)
                        }
                    }

                    if (isReceiverAvailable) {
                        binding.userAvaialability.visibility = View.VISIBLE
                    } else {
                        binding.userAvaialability.visibility = View.GONE
                    }
                }
        }


    }


    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        database = FirebaseFirestore.getInstance()

        val bitmap = receivedUser.image?.let { getBitMapFromEncodedString(it) }

        if (bitmap != null) {
            chatAdapter = ChatAdapter(
                chatMessageList,
                bitmap,
                preferenceManager.getString(Constants.KEY_USER_ID) ?: ""
            )
        }
        binding.chatRecyclerView.adapter = chatAdapter
    }

    private fun sendMessage() {
        val messageData = mutableMapOf<String, Any>()
        preferenceManager.getString(Constants.KEY_USER_ID)
            ?.let { messageData[Constants.KEY_SENDER_ID] = it }
        receivedUser.id?.let { messageData[Constants.KEY_RECEIVER_ID] = it }
        messageData[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        messageData[Constants.KEY_TIMESTAMP] = Date()

        database.collection(Constants.KEY_COLLECTION_CHAT).add(messageData)
            .addOnSuccessListener {
                Log.d("ChatActivity", "Message sent successfully")
            }
            .addOnFailureListener {
                Log.e("ChatActivity", "Failed to send message", it)
            }

        if (conversationId != null) {
            updateConversation(binding.inputMessage.text.toString())
        } else {
            val conversation = mutableMapOf<String, Any>()
            preferenceManager.getString(Constants.KEY_USER_ID)
                ?.let { conversation[Constants.KEY_SENDER_ID] = it }
            preferenceManager.getString(Constants.KEY_NAME)
                ?.let { conversation[Constants.KEY_SENDER_NAME] = it }
            preferenceManager.getString(Constants.KEY_IMAGE)
                ?.let { conversation[Constants.KEY_SENDER_IMAGE] = it }

            receivedUser.name?.let { conversation[Constants.KEY_RECEIVER_NAME] = it }
            receivedUser.id?.let { conversation[Constants.KEY_RECEIVER_ID] = it }
            receivedUser.image?.let { conversation[Constants.KEY_RECEIVER_IMAGE] = it }

            conversation[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addRecentConversation(conversation)
        }

        if (!isReceiverAvailable) {
                    val title = "New Message from ${preferenceManager.getString(Constants.KEY_NAME)}"
                    val message = binding.inputMessage.text.toString()

            FirebaseNotificationSender.sendFCMNotification(applicationContext, receivedUser.token ?: "", title, message)


        }
        binding.inputMessage.text.clear()
    }


    private fun loadReceiverDetails() {
        binding.personName.text = receivedUser.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }


    //This is a Firestore event listener that triggers whenever there are changes in the Firestore chat collection.
    //It listens for added, modified, or removed documents (chat messages).
    private val eventListener = EventListener<QuerySnapshot> { value, error ->

        //If there is an error while fetching messages, it logs the error and returns early to prevent crashes.
        if (error != null) {
            Log.e("Firestore", "Error fetching messages", error)
            return@EventListener
        }


        //If there is new data (value) from Firestore, the current size of chatMessageList
        // is stored in count to track how many new messages were added.
        if (value != null) {
            val count = chatMessageList.size

            //It loops through each document change and checks if the type is DocumentChange.
            // Type.ADDED, meaning a new message was sent.
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {

                    /*
                    senderId: The ID of the user who sent the message.
                    receiverId: The ID of the user who received the message.
                    message: The message content.
                    dateObj: The timestamp of when the message was sent.

                    */

                    val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId =
                        documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    val message = documentChange.document.getString(Constants.KEY_MESSAGE)
                    val dateObj = documentChange.document.getDate(Constants.KEY_TIMESTAMP)

                    Log.d(
                        "Firestore",
                        "New message from: $senderId to: $receiverId -> $message"
                    )

                    if (dateObj != null) {
                        val dateTime = getReadableDateTimeFormatter(dateObj)
                        chatMessageList.add(
                            ChatMessage(senderId, receiverId, message, dateTime, dateObj)
                        )
                    }
                }
            }

            chatMessageList.sortBy { it.dateObj }

            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(count, chatMessageList.size - count)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessageList.size - 1)
            }

            binding.chatRecyclerView.visibility =
                if (chatMessageList.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        }
        binding.progressBarChat.visibility = View.GONE
        if (conversationId != null) {
            checkForRecentConversation()
        }
    }


    private fun addRecentConversation(conversation: MutableMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener {
                conversationId = it.id
            }
            .addOnFailureListener {
                Log.e("ChatActivity", "Failed to add conversation", it)
                Toast.makeText(this, "Failed to add conversation", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateConversation(message: String) {
        val documentReference = conversationId?.let {
            database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATIONS).document(it)
        }
        documentReference?.update(
            Constants.KEY_LAST_MESSAGE,
            message,
            Constants.KEY_TIMESTAMP,
            Date()
        )

    }


    private fun listenMessages() {
        val userId = preferenceManager.getString(Constants.KEY_USER_ID)

        //This query listens for messages sent by the current user (userId) to the receiver (receivedUser.id).
        //Why?
        //So we can display the messages the logged-in user sends in the chat.
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, userId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedUser.id)
            .addSnapshotListener(eventListener)


        //This query listens for messages sent by the receiver (receivedUser.id) to the logged-in user (userId).
        //Why?
        //So we can display the messages received from the other person in real time.
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receivedUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, userId)
            .addSnapshotListener(eventListener)
    }

    private fun getBitMapFromEncodedString(encodedImage: String): Bitmap? {
        return try {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("ChatActivity", "Error decoding image", e)
            null
        }
    }

    private fun getReadableDateTimeFormatter(date: Date): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }


    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationonCompleteListener)
    }

    private fun checkForRecentConversation() {
        if (chatMessageList.isNotEmpty()) {
            val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
            val receivedUserId = receivedUser.id

            if (currentUserId != null && receivedUserId != null) {
                checkForConversationRemotely(currentUserId, receivedUserId)
                checkForConversationRemotely(receivedUserId, currentUserId)
            }
        }
    }

    private val conversationonCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
            val documentSnapshot = task.result.documents[0]
            conversationId = documentSnapshot.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }

    override fun onUserClicked(user: User) {

    }
}
