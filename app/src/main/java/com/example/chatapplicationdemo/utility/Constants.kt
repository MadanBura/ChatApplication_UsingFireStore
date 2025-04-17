package com.example.chatapplicationdemo.utility

object Constants {

    //for user table
    const val KEY_COLLECTION_USER = "users"
    const val KEY_NAME = "name"
    const val KEY_EMAIL = "email"
    const val KEY_PASSWORD = "password"
    const val KEY_PREFERENCE_NAME = "chatAppPreference"
    const val KEY_IS_SIGNED_IN = "isSignedIn"
    const val KEY_USER_ID = "userId"
    const val KEY_IMAGE = "image"
    const val KEY_FCM_TOKEN = "fcmToken"
    const val KEY_USER = "user"

    //for chat table
    const val KEY_COLLECTION_CHAT = "chat"
    const val KEY_SENDER_ID = "senderId"
    const val KEY_RECEIVER_ID = "receiverId"
    const val KEY_MESSAGE = "message"
    const val KEY_TIMESTAMP = "timestamp"

    //for recentCOnversation
    const val KEY_COLLECTION_RECENT_CONVERSATIONS = "conversations"
    const val KEY_SENDER_NAME = "senderName"
    const val KEY_RECEIVER_NAME = "receiverName"
    const val KEY_SENDER_IMAGE = "senderImage"
    const val KEY_RECEIVER_IMAGE = "receiverImage"
    const val KEY_LAST_MESSAGE = "lastMessage"


    //User Availaility
    const val KEY_AVAILABILITY = "availability"
    const val REMOTE_MSG_AUTHORIZATION = "Authorization"
    const val REMOTE_MSG_CONTENT_TYPE= "Content-Type"
    const val REMOTE_MSG_DATA = "data"
    const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"



    private val remotemsgHeaders : MutableMap<String,String> = mutableMapOf()
    public fun getRemoteMsgHeaders() : MutableMap<String, String>{
        if(remotemsgHeaders.isEmpty()){
            remotemsgHeaders[REMOTE_MSG_AUTHORIZATION] = "key="
            remotemsgHeaders[REMOTE_MSG_CONTENT_TYPE] = "application/json"
        }
        return remotemsgHeaders
    }




}