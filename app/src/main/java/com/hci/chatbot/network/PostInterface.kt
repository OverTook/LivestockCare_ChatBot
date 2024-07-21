package com.hci.chatbot.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PostInterface {

    @GET("/get_hospital")
    fun getHospital(@Query("lat_from") latFrom: Double, @Query("lng_from") lngFrom: Double,
                         @Query("lat_to") latTo: Double, @Query("lng_to") lngTo: Double): Call<HospitalResponse>

    @GET("/get_disease_clustering")
    fun getClusteredData(@Query("lat_from") latFrom: Double, @Query("lng_from") lngFrom: Double,
                         @Query("lat_to") latTo: Double, @Query("lng_to") lngTo: Double, @Query("level") level: Int): Call<ClusteringResponse>

    @GET("/get_occur_disease_list")
    fun getDiseaseData(@Query("lat") lat: Double, @Query("lng") lng: Double, @Query("level") level: Int): Call<DiseaseListResponse>

    @GET("/valid_chat_count")
    fun getValidChatCount(): Call<ValidChatCountResponse>

    @GET("/login")
    fun loginAccount(
        @Query("platform") platform: String,
        @Query("token") token: String
    ): Call<AccountLoginResponse>

    @POST("/chat")
    fun chat(@Body requestData: ChatRequest) : Call<ChatResponse>

    @GET("/valid_reception")
    fun validReception(@Query("data") data: String) : Call<ValidReceptionResponse>
}
