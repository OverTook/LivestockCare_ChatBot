package com.hci.chatbot

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton


class InfoDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_popup)

        // Dialog를 전체 화면으로 설정
        window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val dialogButton = findViewById<ImageButton>(R.id.closeBtn)

        dialogButton.setOnClickListener {
            dismiss()
        }
    }
}
