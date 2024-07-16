package com.hci.chatbot.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "user_profile"
        private const val KEY_IMAGE_URL = "profile_url"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
        private const val KEY_LAST_CHAT_TIME = "last_chat_time"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveProfileInfo(imageURL: String, nickname: String, email: String) {
        editor.putString(KEY_IMAGE_URL, imageURL)
        editor.putString(KEY_NICKNAME, nickname)
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    fun saveLastChatTime(time: Long) {
        editor.putLong(KEY_LAST_CHAT_TIME, time)
        editor.apply()
    }

    fun getLastChatTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_CHAT_TIME, System.currentTimeMillis())
    }

    fun getImageURL(): String? {
        return sharedPreferences.getString(KEY_IMAGE_URL, null)
    }

    fun getNickname(): String? {
        return sharedPreferences.getString(KEY_NICKNAME, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun clearProfileInfo() {
        editor.clear()
        editor.apply()
    }
}