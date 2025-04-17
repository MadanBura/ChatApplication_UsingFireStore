package com.example.chatapplicationdemo.listeners

import com.example.chatapplicationdemo.models.User

interface RecentConversationListener {

    fun onConversationClick(user : User)

}