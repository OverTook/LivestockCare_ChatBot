package com.contest.chatbot.history

import android.content.Context
import android.content.SharedPreferences
import com.contest.chatbot.HistoryData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatHistoryManager(private val context: Context) {
    companion object {
        private const val PREFS_NAME = "contest_chatbot"
        private const val HISTORY_KEY = "history_list"
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    public fun saveHistoryList(historyList: List<HistoryData>) {
        val jsonString = gson.toJson(historyList)
        sharedPreferences.edit().putString(HISTORY_KEY, jsonString).apply()
    }

    public fun loadHistoryList(): List<HistoryData>? {
        val jsonString = sharedPreferences.getString(HISTORY_KEY, null) ?: return null
        val type = object : TypeToken<List<HistoryData>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}