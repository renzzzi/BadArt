package com.example.badart.ui

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentProfileBinding
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.example.badart.views.DrawingView
import com.example.badart.views.Tool
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUsername.text = user.username
                binding.tvScore.text = user.totalScore.toString()
                binding.tvCorrectGuesses.text = user.correctGuesses.toString()
                binding.tvPostCount.text = user.postCount.toString()

                if (user.hasChangedAvatar) {
                    binding.tvAvatarHint.text = "Tap to Change Avatar (50 pts)"
                } else {
                    binding.tvAvatarHint.text = "Tap to Draw Avatar (Free)"
                }

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
            val user = viewModel.currentUser.value ?: return@setOnClickListener
            if (user.hasChangedAvatar) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Change Avatar")
                    .setMessage("Changing your avatar will cost 50 points. Continue?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (user.totalScore >= 50) {
                            showAvatarDialog()
                        } else {
                            UiUtils.showModal(requireContext(), "Low Balance", "You need 50 points to change your avatar.")
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                showAvatarDialog()
            }
        }

        binding.btnEditName.setOnClickListener {
            val user = viewModel.currentUser.value ?: return@setOnClickListener
            if (user.hasChangedUsername) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Change Username")
                    .setMessage("Changing your username will cost 50 points. Continue?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (user.totalScore >= 50) {
                            showNameDialog()
                        } else {
                            UiUtils.showModal(requireContext(), "Low Balance", "You need 50 points to change your username.")
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            } else {
                showNameDialog()
            }
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout") { _, _ ->
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        viewModel.logout()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? All your data will be lost.")
                .setPositiveButton("Delete") { _, _ ->
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        viewModel.deleteAccount()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showNameDialog() {
        val input = EditText(requireContext())
        input.hint = "New Username"
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(50, 0, 50, 0)
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(requireContext())
            .setTitle("New Username")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateUsername(newName,
                        onSuccess = { UiUtils.showModal(requireContext(), "Updated", "Your username has been changed.") },
                        onFailure = { msg -> UiUtils.showModal(requireContext(), "Error", msg) }
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAvatarDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_draw_avatar, null)
        val drawingView = dialogView.findViewById<DrawingView>(R.id.drawingViewAvatar)
        val layoutColors = dialogView.findViewById<LinearLayout>(R.id.layoutColorsAvatar)
        val btnUndo = dialogView.findViewById<ImageButton>(R.id.btnUndoAvatar)
        val btnRedo = dialogView.findViewById<ImageButton>(R.id.btnRedoAvatar)
        val btnBrush = dialogView.findViewById<MaterialButton>(R.id.btnBrushAvatar)
        val btnFill = dialogView.findViewById<MaterialButton>(R.id.btnFillAvatar)
        val btnEraser = dialogView.findViewById<MaterialButton>(R.id.btnEraserAvatar)
        val sliderSize = dialogView.findViewById<Slider>(R.id.sliderSizeAvatar)
        val btnClear = dialogView.findViewById<Button>(R.id.btnClearAvatar)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveAvatar)
        val layoutTools = dialogView.findViewById<LinearLayout>(R.id.tools_container_avatar)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val colors = listOf(
            R.color.paint_black, R.color.paint_red, R.color.paint_blue,
            R.color.paint_green, R.color.paint_yellow, R.color.paint_orange,
            R.color.paint_purple
        )

        fun selectTool(selectedButton: View, tool: Tool) {
            layoutTools.children.filterIsInstance<MaterialButton>().forEach { 
                it.strokeWidth = 0
            }
            (selectedButton as? MaterialButton)?.apply {
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_color))
                strokeWidth = 8
            }
            drawingView.setTool(tool)
        }

        for (colorRes in colors) {
            val colorBtn = MaterialButton(requireContext())
            val params = LinearLayout.LayoutParams(100, 100)
            params.setMargins(8, 0, 8, 0)
            colorBtn.layoutParams = params
            val colorValue = ContextCompat.getColor(requireContext(), colorRes)
            colorBtn.setBackgroundColor(colorValue)

            colorBtn.setOnClickListener { view ->
                layoutColors.children.forEach { child ->
                    (child as? MaterialButton)?.strokeWidth = 0
                }
                (view as? MaterialButton)?.apply {
                    strokeColor = ColorStateList.valueOf(Color.WHITE)
                    strokeWidth = 8
                }
                drawingView.setColor(colorValue)
                selectTool(btnBrush, Tool.BRUSH) // Switch back to brush on color selection
            }
            layoutColors.addView(colorBtn)
        }

        (layoutColors.getChildAt(0) as? MaterialButton)?.performClick()

        btnUndo.setOnClickListener { drawingView.undo() }
        btnRedo.setOnClickListener { drawingView.redo() }
        btnBrush.setOnClickListener { selectTool(it, Tool.BRUSH) }
        btnFill.setOnClickListener { selectTool(it, Tool.FILL) }
        btnEraser.setOnClickListener { selectTool(it, Tool.ERASER) }
        sliderSize.addOnChangeListener { _, value, _ -> drawingView.setBrushSize(value) }
        btnClear.setOnClickListener { 
            drawingView.clearCanvas()
            selectTool(btnBrush, Tool.BRUSH)
        }

        selectTool(btnBrush, Tool.BRUSH)

        btnSave.setOnClickListener {
            val bitmap = drawingView.getBitmap()
            if (bitmap != null) {
                viewModel.updateAvatar(bitmap,
                    onSuccess = {
                        UiUtils.showModal(requireContext(), "Updated", "Your new avatar is saved!")
                        dialog.dismiss()
                    },
                    onFailure = { msg ->
                        UiUtils.showModal(requireContext(), "Error", msg)
                    }
                )
            }
        }
        dialog.show()
    }
}