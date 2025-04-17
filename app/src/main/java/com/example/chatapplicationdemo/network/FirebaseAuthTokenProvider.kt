package com.example.chatapplicationdemo.network

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FirebaseAuthTokenProvider {
    suspend fun getAccessToken(context: Context): String? {
        return withContext(Dispatchers.IO) { // ✅ Ensure it's always in IO thread
            try {
                val fileName = "service-account.json"
                val assetFiles = context.assets.list("") ?: emptyArray()
                if (!assetFiles.contains(fileName)) {
                    Log.e("FCM", "Error: $fileName not found in assets!")
                    return@withContext null
                }

                Log.d("FCM", "Opening $fileName for reading...")

                context.assets.open(fileName).use { inputStream ->
                    val credentials = GoogleCredentials
                        .fromStream(inputStream)
                        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))

                    try {
                        credentials.refreshIfExpired() // ✅ Network call, must be inside IO dispatcher
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to refresh access token: ${e.message}", e)
                        return@withContext null
                    }

                    val token = credentials.accessToken?.tokenValue

                    if (token.isNullOrEmpty()) {
                        Log.e("FCM", "Access token is null or empty! Check Firebase IAM permissions.")
                        return@withContext null
                    }

                    Log.d("FCM", "Successfully retrieved access token: $token")
                    return@withContext token
                }
            } catch (e: Exception) {
                Log.e("FCM", "Failed to get access token: ${e.message}", e)
                return@withContext null
            }
        }
    }
}
