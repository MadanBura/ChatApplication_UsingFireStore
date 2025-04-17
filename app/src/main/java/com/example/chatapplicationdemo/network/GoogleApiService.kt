package com.example.chatapplicationdemo.network

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST

interface GoogleApiService {

    @POST("/v1/projects/neochatapp-19440/messages:send")
    fun sendNotification(
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ): Call<Void>
}