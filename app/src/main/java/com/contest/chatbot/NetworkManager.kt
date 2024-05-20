package com.contest.chatbot

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object  NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5125/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: PostInterface = retrofit.create(PostInterface::class.java)
}