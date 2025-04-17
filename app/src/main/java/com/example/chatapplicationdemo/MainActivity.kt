package com.example.chatapplicationdemo

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatapplicationdemo.activities.BaseActivity
import com.example.chatapplicationdemo.activities.ChatActivity
import com.example.chatapplicationdemo.activities.LoginActivity
import com.example.chatapplicationdemo.activities.UsersActivity
import com.example.chatapplicationdemo.adapters.RecentConversationAdapter
import com.example.chatapplicationdemo.databinding.ActivityMainBinding
import com.example.chatapplicationdemo.listeners.RecentConversationListener
import com.example.chatapplicationdemo.models.ChatMessage
import com.example.chatapplicationdemo.models.User
import com.example.chatapplicationdemo.utility.Constants
import com.example.chatapplicationdemo.utility.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : BaseActivity(), RecentConversationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var recentConversation: MutableList<ChatMessage>
    private lateinit var conversationAdapter: RecentConversationAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        init()
        setListeners()
        loadUserDetails()
        getToken()
        listenConversation()
    }

    private fun init() {
        database = FirebaseFirestore.getInstance() // Ensuring Firestore is initialized
        recentConversation = mutableListOf()
        conversationAdapter = RecentConversationAdapter(recentConversation, this)
        binding.recentConversations.adapter = conversationAdapter
    }

    private fun listenConversation() {
        val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)

        if (currentUserId.isNullOrEmpty()) {
            Log.e("Firestore", "User ID is null. Cannot listen to conversations.")
            return
        }

        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATIONS)
            .whereIn(Constants.KEY_SENDER_ID, listOf(currentUserId))
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_RECENT_CONVERSATIONS)
            .whereIn(Constants.KEY_RECEIVER_ID, listOf(currentUserId))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            Log.e("Firestore", "Error fetching conversations: ${error.message}")
            return@EventListener
        }

        if (value != null) {
            for (documentChange in value.documentChanges) {
                val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                val lastMessage =
                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE) ?: ""
                val timestamp = documentChange.document.getDate(Constants.KEY_TIMESTAMP) ?: Date()
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)

                if (senderId == null || receiverId == null) continue

                Log.d(
                    "FirestoreUpdate",
                    "Change Type: ${documentChange.type}, Message: $lastMessage"
                )

                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val existingChat = recentConversation.find {
                            (it.senderId == senderId && it.receiverId == receiverId) ||
                                    (it.senderId == receiverId && it.receiverId == senderId)
                        }

                        if (existingChat != null) {
                            existingChat.message = lastMessage
                            existingChat.dateObj = timestamp
                        } else {
                            val chatMessage = ChatMessage(
                                senderId = senderId,
                                receiverId = receiverId,
                                message = lastMessage,
                                dateObj = timestamp
                            )

                            if (currentUserId == senderId) {
                                chatMessage.conversationImage =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                                chatMessage.conversationName =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
                                chatMessage.conversationId =
                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            } else {
                                chatMessage.conversationImage =
                                    documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
                                chatMessage.conversationName =
                                    documentChange.document.getString(Constants.KEY_SENDER_NAME)
                                chatMessage.conversationId =
                                    documentChange.document.getString(Constants.KEY_SENDER_ID)
                            }

                            recentConversation.add(chatMessage)
                        }
                    }

                    DocumentChange.Type.MODIFIED -> {
                        for (chat in recentConversation) {
                            if ((chat.senderId == senderId && chat.receiverId == receiverId) ||
                                (chat.senderId == receiverId && chat.receiverId == senderId)
                            ) {
                                chat.message = lastMessage
                                chat.dateObj = timestamp
                                break
                            }
                        }
                    }

                    else -> {}
                }
            }

            recentConversation.sortByDescending { it.dateObj }
            Log.d("SortedList", recentConversation.toString())

            runOnUiThread {
                conversationAdapter.notifyDataSetChanged()
                binding.recentConversations.smoothScrollToPosition(0)
                binding.recentConversations.visibility = View.VISIBLE
                binding.ProgressBarMain.visibility = View.GONE
            }
        }
    }

    private fun setListeners() {
        binding.btnLogout.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun loadUserDetails() {
        binding.userName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun signOut() {
        showToast("Signing Out...")
        val documentReference = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USER).document(it)
        }

        val userTokenMap = mutableMapOf<String, Any>()
        userTokenMap[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference?.update(userTokenMap)
            ?.addOnSuccessListener {
                preferenceManager.clearUser()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
            }
            ?.addOnFailureListener { showToast("Unable to sign out") }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token)
        val documentRef = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USER).document(it)
        }

        //   val documentRef = database.collection(Constants.KEY_COLLECTION_USER)
        //   .document(preferenceManager.getString(Constants.KEY_USER_ID))

        documentRef?.update(Constants.KEY_FCM_TOKEN, token)
            ?.addOnSuccessListener { showToast("Token updated Successfully") }
            ?.addOnFailureListener { showToast("Unable to update token") }
    }

    override fun onConversationClick(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}
