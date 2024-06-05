package com.contest.chatbot

import com.google.gson.annotations.SerializedName
import java.util.Date


data class CenterAddrResponse (
    @SerializedName("addr")
    val addr: String,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("result")
    val result: String
)


// =========================================================================== //


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
    val alpha: Float,
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


// =========================================================================== //


data class DiseaseListItemData (
    @SerializedName("addr_code")
    val addressCode: String,
    @SerializedName("addr_name")
    val addressName: String,
    @SerializedName("dgnss_engn")
    val dgnssEngn: String,
    @SerializedName("disease_code")
    val diseaseCode: String,
    @SerializedName("disease_name")
    val diseaseName: String,
    @SerializedName("end_date")
    val endDate: Date?,
    @SerializedName("farm_name")
    val farmName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("livestock_type")
    val livestockType: String,
    @SerializedName("occur_count")
    val occurCount: Int,
    @SerializedName("occur_date")
    val occurDate: Date
)

data class DiseaseListData (
    @SerializedName("disease_list")
    val diseaseList: List<DiseaseListItemData>,
    @SerializedName("addr")
    val address: String,
    @SerializedName("filter")
    val filter: String,
    @SerializedName("total_occur_count")
    val totalOccurCount: Int
)

data class DiseaseListResponse (
    @SerializedName("data")
    val data: DiseaseListData,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("result")
    val result: String
)


// =========================================================================== //


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
