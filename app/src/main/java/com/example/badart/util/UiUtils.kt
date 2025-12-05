package com.example.badart.util

import android.content.Context
import android.app.AlertDialog

object UiUtils {
    fun showModal(context: Context, title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}