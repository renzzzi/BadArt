package com.example.badart.ui

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentProfileBinding
import com.example.badart.viewmodel.SharedViewModel
import com.example.badart.views.DrawingView
import com.google.android.material.slider.Slider

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUsername.text = user.username
                binding.tvScore.text = user.totalScore.toString()
                binding.tvCorrectGuesses.text = user.correctGuesses.toString()
                binding.tvPostCount.text = user.postCount.toString()

                if (user.avatarBase64.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.ivAvatarDisplay.setImageBitmap(bitmap)
                        binding.ivAvatarDisplay.visibility = View.VISIBLE
                        binding.ivAvatar.visibility = View.GONE
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            val myId = viewModel.currentUser.value?.userId
            val myRank = users.indexOfFirst { it.userId == myId }
            if (myRank != -1) {
                binding.tvRank.text = "#${myRank + 1}"
            } else {
                binding.tvRank.text = "-"
            }
        }

        binding.cardAvatar.setOnClickListener {
            showAvatarDialog()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? All your data will be lost.")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteAccount()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showAvatarDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_draw_avatar, null)

        val drawingView = dialogView.findViewById<DrawingView>(R.id.drawingViewAvatar)
        val layoutColors = dialogView.findViewById<LinearLayout>(R.id.layoutColorsAvatar)

        val btnUndo = dialogView.findViewById<ImageButton>(R.id.btnUndoAvatar)
        val btnRedo = dialogView.findViewById<ImageButton>(R.id.btnRedoAvatar)

        val btnBrush = dialogView.findViewById<Button>(R.id.btnBrushAvatar)
        val btnFill = dialogView.findViewById<Button>(R.id.btnFillAvatar)
        val btnEraser = dialogView.findViewById<Button>(R.id.btnEraserAvatar)

        val sliderSize = dialogView.findViewById<Slider>(R.id.sliderSizeAvatar)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClearAvatar)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveAvatar)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val colors = listOf(
            R.color.paint_black, R.color.paint_red, R.color.paint_blue,
            R.color.paint_green, R.color.paint_yellow, R.color.paint_orange,
            R.color.paint_purple
        )

        for (colorRes in colors) {
            val colorBtn = Button(requireContext())
            val params = LinearLayout.LayoutParams(100, 100)
            params.setMargins(8, 0, 8, 0)
            colorBtn.layoutParams = params
            val colorValue = ContextCompat.getColor(requireContext(), colorRes)
            colorBtn.setBackgroundColor(colorValue)
            colorBtn.setOnClickListener {
                drawingView.setColor(colorValue)
            }
            layoutColors.addView(colorBtn)
        }

        btnUndo.setOnClickListener { drawingView.undo() }
        btnRedo.setOnClickListener { drawingView.redo() }

        btnBrush.setOnClickListener { drawingView.setEraser(false) }
        btnFill.setOnClickListener { drawingView.setFillMode(true) }
        btnEraser.setOnClickListener { drawingView.setEraser(true) }

        sliderSize.addOnChangeListener { _, value, _ ->
            drawingView.setBrushSize(value)
        }

        btnClear.setOnClickListener {
            drawingView.clearCanvas()
        }

        btnSave.setOnClickListener {
            val bitmap = drawingView.getBitmap()
            if (bitmap != null) {
                viewModel.updateAvatar(bitmap)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}