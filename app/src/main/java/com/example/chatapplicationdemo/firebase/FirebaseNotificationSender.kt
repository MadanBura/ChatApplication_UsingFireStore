package com.example.chatapplicationdemo.firebase

import android.content.Context
import android.util.Log
import com.example.chatapplicationdemo.network.FirebaseAuthTokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FirebaseNotificationSender {
    private const val RETRY_COUNT = 3

    fun sendFCMNotification(context: Context, fcmToken: String, title: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch { // ✅ Ensure network call runs in IO thread
            val accessToken = FirebaseAuthTokenProvider.getAccessToken(context)

            if (!accessToken.isNullOrEmpty()) {
                val authorization = "Bearer $accessToken"

                val jsonObject = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", fcmToken)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", message)
                        })
                    })
                }

                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

                sendNotificationWithRetry(authorization, requestBody, 0)
            } else {
                Log.e("FCM", "Failed to get access token")
            }
        }
    }

    private fun sendNotificationWithRetry(authorization: String, requestBody: RequestBody, attempt: Int) {
        RetrofitInstance.googleApiService.sendNotification(authorization, requestBody)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("FCM", "Notification sent successfully")
                    } else {
                        Log.e("FCM", "Failed to send notification: ${response.code()}")
                        if (attempt < RETRY_COUNT) {
                            Log.d("FCM", "Retrying... Attempt ${attempt + 1}")
                            sendNotificationWithRetry(authorization, requestBody, attempt + 1)
                        } else {
                            Log.e("FCM", "Failed to send notification after $RETRY_COUNT attempts.")
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("FCM", "Error sending notification: ${t.message}")
                    if (attempt < RETRY_COUNT) {
                        Log.d("FCM", "Retrying... Attempt ${attempt + 1}")
                        sendNotificationWithRetry(authorization, requestBody, attempt + 1)
                    } else {
                        Log.e("FCM", "Failed to send notification after $RETRY_COUNT attempts.")
                    }
                }
            })
    }
}
