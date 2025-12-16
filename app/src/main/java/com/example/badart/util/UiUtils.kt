package com.example.badart.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.example.badart.R
import com.google.android.material.button.MaterialButton

object UiUtils {
    fun showModal(context: Context, title: String, message: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_modal, null)
        dialog.setContentView(view)
        
        // Set transparent background for rounded corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)
        
        tvTitle.text = title
        tvMessage.text = message
        
        // Set icon based on title content
        when {
            title.contains("Error", ignoreCase = true) || 
            title.contains("Oops", ignoreCase = true) ||
            title.contains("Late", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_close)
            }
            title.contains("Won", ignoreCase = true) || 
            title.contains("Correct", ignoreCase = true) ||
            title.contains("Success", ignoreCase = true) ||
            title.contains("Unblocked", ignoreCase = true) ||
            title.contains("Unreported", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_check_circle)
            }
            title.contains("Block", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_block)
            }
            title.contains("Close", ignoreCase = true) || 
            title.contains("Wrong", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_close)
            }
            title.contains("Hint", ignoreCase = true) ||
            title.contains("Points", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_lightbulb_hint)
            }
            title.contains("Report", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_flag_report)
            }
            title.contains("React", ignoreCase = true) ||
            title.contains("added", ignoreCase = true) -> {
                ivIcon.setImageResource(R.drawable.ic_palette)
            }
            else -> {
                ivIcon.setImageResource(R.drawable.ic_lightbulb_hint)
            }
        }
        
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
}