package com.contest.chatbot

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object  NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5125/"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
        .create()
    
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson)) //날짜 처리를 위해서 gson 사용
        .build()

    val apiService: PostInterface = retrofit.create(PostInterface::class.java)
}