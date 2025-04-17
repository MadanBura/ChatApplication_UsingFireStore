package com.example.chatapplicationdemo.models

import java.util.Date

data class ChatMessage(
    var senderId : String? = null,
    var receiverId : String? = null,
    var message : String? = null,
    var dateTime : String? = null,
    var dateObj : Date = Date(),

    var conversationId : String? = null,
    var conversationName : String? = null,
    var conversationImage: String? = null
)