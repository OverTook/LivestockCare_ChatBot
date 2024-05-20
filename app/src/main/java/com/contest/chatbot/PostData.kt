package com.contest.chatbot

import com.google.gson.annotations.SerializedName

data class ClusteringResponse (
    @SerializedName("data")
    val Data: List<ClusteringDataItem>
)

data class ClusteringDataItem (
    @SerializedName("addr_code")
    val AddressCode: String,
    @SerializedName("addr_name")
    val AddressName: String,
    @SerializedName("alpha")
    val Alpha: String,
    @SerializedName("filter")
    val Filter: String,
    @SerializedName("geometry")
    val Geometry: List<List<List<List<Double>>>>,
    @SerializedName("lat")
    val Lat: String,
    @SerializedName("lng")
    val Lng: String,
    @SerializedName("total_occur_count")
    val TotalOccurCount: String
)
