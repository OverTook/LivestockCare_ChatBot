package com.hci.chatbot.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings

private var ssaid: String? = null

@SuppressLint("HardwareIds")
fun Activity.getSSAID(): String? {
    if(ssaid == null) {
        ssaid = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
    return ssaid
}