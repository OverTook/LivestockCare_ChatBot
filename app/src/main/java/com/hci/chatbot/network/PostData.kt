package com.hci.chatbot.network

import com.google.gson.annotations.SerializedName
import java.util.Date


data class HospitalResponse (
    val success: Boolean,
    val msg: String,
    val hospitals: List<Hospital>
)

data class Hospital (
    val address: String,
    @SerializedName("hospital_name")
    val hospitalName: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("phone_number")
    val phone: String
)

// =========================================================================== //


data class ClusteringResponse (
    val success: Boolean,
    val msg: String,
    val data: List<ClusteringMarkers>
)

data class ClusteringMarkers (
    @SerializedName("addr_code")
    val addressCode: String,
    @SerializedName("addr_name")
    val addressName: String,
    @SerializedName("alpha")
    val alpha: Float,
    @SerializedName("filter")
    val filter: String,
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

// =========================================================================== //

data class ValidChatCountResponse (
    val result: String,
    val msg: String,

    @SerializedName("max_count")
    val maxCount: Int,
    @SerializedName("curr_count")
    val curCount: Int
)

// =========================================================================== //

data class AccountLoginResponse (
    val result: String,
    val msg: String,

    val token: String
)

data class AccountLinkResponse (
    val result: String,
    val msg: String
)

// =========================================================================== //

data class ValidReceptionResponse (
    val result: String,
    val msg: String
)

