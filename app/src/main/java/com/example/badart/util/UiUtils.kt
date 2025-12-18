package com.example.badart.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.R
import com.google.android.material.button.MaterialButton

object UiUtils {

    private fun getIconResId(title: String): Int {
        return when {
            title.contains("Error", ignoreCase = true) || 
            title.contains("Oops", ignoreCase = true) ||
            title.contains("Late", ignoreCase = true) -> R.drawable.ic_close
            
            title.contains("Won", ignoreCase = true) || 
            title.contains("Correct", ignoreCase = true) ||
            title.contains("Success", ignoreCase = true) ||
            title.contains("Unblocked", ignoreCase = true) ||
            title.contains("Unreported", ignoreCase = true) -> R.drawable.ic_check_circle
            
            title.contains("Block", ignoreCase = true) ||
            title.contains("Delete", ignoreCase = true) ||
            title.contains("Logout", ignoreCase = true) -> R.drawable.ic_close
            
            title.contains("Close", ignoreCase = true) || 
            title.contains("Wrong", ignoreCase = true) -> R.drawable.ic_close
            
            title.contains("Hint", ignoreCase = true) ||
            title.contains("Points", ignoreCase = true) -> R.drawable.ic_lightbulb_hint
            
            title.contains("Report", ignoreCase = true) -> R.drawable.ic_flag_report
            
            title.contains("React", ignoreCase = true) ||
            title.contains("added", ignoreCase = true) ||
            title.contains("Action", ignoreCase = true) ||
            title.contains("Color", ignoreCase = true) -> R.drawable.ic_palette
            
            else -> R.drawable.ic_lightbulb_hint
        }
    }

    fun showModal(context: Context, title: String, message: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_modal, null)
        dialog.setContentView(view)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val btnOk = view.findViewById<MaterialButton>(R.id.btnOk)
        
        tvTitle.text = title
        tvMessage.text = message
        ivIcon.setImageResource(getIconResId(title))
        
        btnOk.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    fun showConfirmation(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirmation, null)
        dialog.setContentView(view)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.btnConfirm)
        
        tvTitle.text = title
        tvMessage.text = message
        ivIcon.setImageResource(getIconResId(title))
        
        if (title.contains("Delete", ignoreCase = true) || 
            title.contains("Block", ignoreCase = true) ||
            title.contains("Logout", ignoreCase = true)) {
            btnConfirm.setBackgroundColor(Color.parseColor("#F44336"))
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }
        
        dialog.show()
    }

    fun showList(
        context: Context,
        title: String,
        options: Array<String>,
        fullWidth: Boolean = false,
        onSelected: (Int) -> Unit
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_list, null)
        dialog.setContentView(view)
        
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        if (fullWidth) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val rvItems = view.findViewById<RecyclerView>(R.id.rvItems)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        
        tvTitle.text = title
        ivIcon.setImageResource(getIconResId(title))
        
        rvItems.layoutManager = LinearLayoutManager(context)
        rvItems.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_dialog_list, parent, false)
                return object : RecyclerView.ViewHolder(itemView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tvItem = holder.itemView.findViewById<TextView>(R.id.tvItem)
                tvItem.text = options[position]
                holder.itemView.setOnClickListener {
                    dialog.dismiss()
                    onSelected(position)
                }
            }

            override fun getItemCount() = options.size
        }
        
        btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
    }

    fun showCustom(
        context: Context,
        title: String,
        customView: View,
        fullWidth: Boolean = false
    ): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_wrapper, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        if (fullWidth) {
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val container = view.findViewById<ViewGroup>(R.id.customContainer)
        
        tvTitle.text = title
        ivIcon.setImageResource(getIconResId(title))
        
        if (customView.parent != null) {
            (customView.parent as ViewGroup).removeView(customView)
        }
        container.addView(customView)
        
        dialog.show()
        return dialog
    }
}