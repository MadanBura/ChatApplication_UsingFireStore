package com.example.chatapplicationdemo

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

class ChatApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
        }
}