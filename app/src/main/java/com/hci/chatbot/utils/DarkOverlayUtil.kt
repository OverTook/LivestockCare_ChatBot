package com.hci.chatbot.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hci.chatbot.R

@SuppressLint("InflateParams")
fun Activity.showDarkOverlay() {
    // 이미 오버레이가 있는지 확인
    if (findViewById<View>(R.id.dark_overlay) != null) {
        return
    }

    val overlayView = LayoutInflater.from(this).inflate(R.layout.dark_overlay, null).apply {
        id = R.id.dark_overlay
    }
    addContentView(
        overlayView,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )

    overlayView.animate()
        .alpha(1f)
        .setDuration(200) // 0.2초
        .start()
}

// 어두운 오버레이를 제거하는 확장 함수
fun Activity.hideDarkOverlay() {
    val overlayView = findViewById<View>(R.id.dark_overlay)
    overlayView?.animate()?.alpha(0f)?.setDuration(200)?.withEndAction {
        (overlayView.parent as ViewGroup).removeView(overlayView)
    }?.start()
}