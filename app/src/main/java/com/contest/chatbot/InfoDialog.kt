package com.contest.chatbot

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        val termsButton = findViewById<Button>(R.id.termsBtn)
        termsButton.paintFlags = termsButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        termsButton.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.naver.com"))
            this.context.startActivity(intent)
        }


    }
}
