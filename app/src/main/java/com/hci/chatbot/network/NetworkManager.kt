package com.hci.chatbot.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object  NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5125/"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
        .create()
    
    private var retrofit: Retrofit = Retrofit.Builder()
                                        .baseUrl(BASE_URL)
                                        .addConverterFactory(GsonConverterFactory.create(gson))
                                        .build()
    var apiService: PostInterface = retrofit.create(PostInterface::class.java)

    public fun initNetworkManager(firebaseToken: String, uuid: String) {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(TokenInterceptor(firebaseToken, uuid))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(PostInterface::class.java)
    }
}