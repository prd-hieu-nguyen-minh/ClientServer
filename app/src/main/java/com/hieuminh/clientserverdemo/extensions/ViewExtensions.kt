package com.hieuminh.clientserverdemo.extensions

import android.annotation.SuppressLint
import android.widget.TextView

object ViewExtensions {
    @SuppressLint("SetTextI18n")
    fun TextView.addLog(msg: String?) {
        text = "${msg}\n\n${text}".trim()
    }
}
