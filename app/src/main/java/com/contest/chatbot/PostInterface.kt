package com.contest.chatbot

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PostInterface {
    @GET("/get_disease_clustering")
    fun getData(@Query("lat") lat: Double, @Query("lng") lng: Double, @Query("level") level: Int): Call<ClusteringResponse>

}