package com.fitforge.app.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.fitforge.app.R

object ToastUtils {
    fun showCustomToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.layout_custom_toast, null)

        val text: TextView = layout.findViewById(R.id.toast_text)
        text.text = message

        with(Toast(context)) {
            duration = Toast.LENGTH_SHORT
            view = layout
            show()
        }
    }
}

fun Context.showToast(message: String) {
    ToastUtils.showCustomToast(this, message)
}