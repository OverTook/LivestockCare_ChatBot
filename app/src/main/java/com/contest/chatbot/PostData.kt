package com.contest.chatbot

import com.google.gson.annotations.SerializedName

data class ClusteringResponse (
    @SerializedName("data")
    val Data: List<ClusteringData>
)

data class ClusteringData (
    @SerializedName("addr_code")
    val addressCode: String,
    @SerializedName("addr_name")
    val addressName: String,
    @SerializedName("alpha")
    val alpha: String,
    @SerializedName("filter")
    val filter: String,
    @SerializedName("geometry")
    val geometry: List<List<List<List<Double>>>>,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lng")
    val lng: String,
    @SerializedName("total_occur_count")
    val totalOccurCount: String
)

data class HistoryData (
    @SerializedName("content")
    val content: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("timestamp")
    val timestamp: String
)
data class ChatResponse (
    @SerializedName("history")
    val history: List<HistoryData>,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("result")
    val result: String,
    @SerializedName("timestamp")
    val timestamp: String
)

data class ChatRequest (
    @SerializedName("msg") val msg: String,
    @SerializedName("history") val history: List<HistoryData>
)