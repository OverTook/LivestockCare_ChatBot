package com.hci.chatbot.utils

import android.view.View
import android.view.ViewGroup.LayoutParams


fun View.hide(){
    this.visibility = View.GONE
    val param = this.layoutParams
    param.height = 0
    this.layoutParams = param
}

fun View.show(){
    this.visibility = View.VISIBLE
    val param = this.layoutParams
    param.height = LayoutParams.WRAP_CONTENT
    this.layoutParams = param
}