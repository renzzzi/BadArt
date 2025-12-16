package com.example.badart.ui

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentProfileBinding
import com.example.badart.model.User
import com.example.badart.util.SoundManager
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.example.badart.views.ColorValueView
import com.example.badart.views.ColorWheelView
import com.example.badart.views.DrawingView
import com.example.badart.views.Tool
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SharedViewModel by activityViewModels()

    private val recentColors = mutableListOf<Int>()
    private val maxRecentColors = 8

    private var viewedUserId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        initDefaultColors()

        viewedUserId = arguments?.getString("userId")

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        if (viewedUserId != null && viewedUserId != viewModel.currentUser.value?.userId) {
            setupReadOnlyMode()
            viewModel.getUser(viewedUserId!!) { user ->
                if (user != null) {
                    populateUserData(user)
                    setupBlockButton() // Setup after username is populated
                } else {
                    UiUtils.showModal(requireContext(), "Error", "User not found")
                    findNavController().popBackStack()
                }
            }
        } else {
            setupEditMode()
            viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    populateUserData(user)
                } else {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }

        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            val targetId = viewedUserId ?: viewModel.currentUser.value?.userId
            val myRank = users.indexOfFirst { it.userId == targetId }
            if (myRank != -1) {
                binding.tvRank.text = "#${myRank + 1}"
            } else {
                binding.tvRank.text = "-"
            }
        }
    }

    private fun setupReadOnlyMode() {
        binding.btnEditName.visibility = View.GONE
        binding.cardAvatar.isClickable = false
        binding.tvAvatarHint.visibility = View.INVISIBLE
        binding.btnLogout.visibility = View.GONE
        binding.btnDeleteAccount.visibility = View.GONE
        binding.btnManageBlockedUsers.visibility = View.GONE
        binding.btnManageReportedContent.visibility = View.GONE
        
        // Show block/unblock button for other users
        binding.btnBlockUser.visibility = View.VISIBLE
    }
    
    private fun setupBlockButton() {
        val viewedUsername = binding.tvUsername.text.toString()
        val currentUser = viewModel.currentUser.value ?: return
        val isBlocked = currentUser.blockedUsers.contains(viewedUsername)
        
        if (isBlocked) {
            binding.btnBlockUser.text = "Unblock User"
            binding.btnBlockUser.setTextColor(ContextCompat.getColor(requireContext(), R.color.solved_green))
            binding.btnBlockUser.setStrokeColorResource(R.color.solved_green)
            binding.btnBlockUser.setIconTintResource(R.color.solved_green)
        } else {
            binding.btnBlockUser.text = "Block User"
            binding.btnBlockUser.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
            binding.btnBlockUser.setStrokeColorResource(R.color.error_red)
            binding.btnBlockUser.setIconTintResource(R.color.error_red)
        }
        
        binding.btnBlockUser.setOnClickListener {
            val username = binding.tvUsername.text.toString()
            val user = viewModel.currentUser.value ?: return@setOnClickListener
            
            if (user.blockedUsers.contains(username)) {
                // Confirmation for unblock
                AlertDialog.Builder(requireContext())
                    .setTitle("Unblock User")
                    .setMessage("Are you sure you want to unblock \"$username\"? You will see their posts again.")
                    .setPositiveButton("Unblock") { _, _ ->
                        viewModel.unblockUser(username) {
                            UiUtils.showModal(requireContext(), "Unblocked", "\"$username\" has been unblocked.")
                            setupBlockButton()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                // Confirmation for block
                AlertDialog.Builder(requireContext())
                    .setTitle("Block User")
                    .setMessage("Are you sure you want to block \"$username\"? You will no longer see their posts.")
                    .setPositiveButton("Block") { _, _ ->
                        viewModel.blockUser(username) {
                            UiUtils.showModal(requireContext(), "Blocked", "You will no longer see posts from \"$username\".")
                            setupBlockButton()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun setupEditMode() {
        binding.btnEditName.visibility = View.VISIBLE
        binding.cardAvatar.isClickable = true
        binding.tvAvatarHint.visibility = View.VISIBLE
        binding.btnLogout.visibility = View.VISIBLE
        binding.btnDeleteAccount.visibility = View.VISIBLE
        binding.btnManageBlockedUsers.visibility = View.VISIBLE
        binding.btnManageReportedContent.visibility = View.VISIBLE

        setupClickListeners()
    }

    private fun populateUserData(user: User) {
        binding.tvUsername.text = user.username
        binding.tvScore.text = user.totalScore.toString()
        binding.tvCorrectGuesses.text = user.correctGuesses.toString()
        binding.tvPostCount.text = user.postCount.toString()

        if (user.hasChangedAvatar && binding.tvAvatarHint.visibility == View.VISIBLE) {
            binding.tvAvatarHint.text = "Tap to Change Avatar (50 pts)"
        } else if (binding.tvAvatarHint.visibility == View.VISIBLE) {
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
    }

    private fun setupClickListeners() {
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
                            SoundManager.playErrorModal()
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
                            SoundManager.playErrorModal()
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

        binding.btnManageBlockedUsers.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_blockedUsersFragment)
        }

        binding.btnManageReportedContent.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_reportedContentFragment)
        }
    }

    private fun initDefaultColors() {
        if (recentColors.isEmpty()) {
            val defaults = listOf(
                R.color.paint_black, R.color.paint_red, R.color.paint_blue,
                R.color.paint_green, R.color.paint_yellow
            )
            defaults.forEach {
                recentColors.add(ContextCompat.getColor(requireContext(), it))
            }
        }
    }

    private fun showNameDialog() {
        val input = EditText(requireContext())
        input.hint = "New Username (max 15 chars)"
        
        // Filter to allow only letters, numbers, and underscore, max 15 characters
        val alphanumericFilter = android.text.InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val c = source[i]
                if (!c.isLetterOrDigit() && c != '_') {
                    return@InputFilter ""
                }
            }
            null
        }
        val maxLengthFilter = android.text.InputFilter.LengthFilter(15)
        input.filters = arrayOf(alphanumericFilter, maxLengthFilter)
        
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
                        onSuccess = {
                            SoundManager.playSuccessModal()
                            UiUtils.showModal(requireContext(), "Updated", "Your username has been changed.")
                        },
                        onFailure = { msg ->
                            SoundManager.playErrorModal()
                            UiUtils.showModal(requireContext(), "Error", msg)
                        }
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
        val scrollViewColors = dialogView.findViewById<HorizontalScrollView>(R.id.scrollViewColorsAvatar)
        val btnColorWheel = dialogView.findViewById<MaterialButton>(R.id.btnColorWheelAvatar)

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

        fun refreshColors() {
            layoutColors.removeAllViews()

            for (colorValue in recentColors.asReversed()) {
                val colorBtn = MaterialButton(requireContext())
                val colorParams = LinearLayout.LayoutParams(100, 100)
                colorParams.setMargins(8, 0, 8, 0)
                colorBtn.layoutParams = colorParams

                colorBtn.backgroundTintList = null
                colorBtn.stateListAnimator = null

                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(colorValue)
                shape.setStroke(4, Color.LTGRAY)
                colorBtn.background = shape

                colorBtn.setOnClickListener { view ->
                    layoutColors.children.forEach { child ->
                        val b = child as? MaterialButton
                        b?.strokeWidth = 0
                        (b?.background as? GradientDrawable)?.setStroke(4, Color.LTGRAY)
                    }
                    val btn = view as? MaterialButton
                    val bg = btn?.background as? GradientDrawable

                    bg?.setStroke(6, Color.BLACK)
                    btn?.strokeColor = ColorStateList.valueOf(Color.WHITE)
                    btn?.strokeWidth = 6

                    drawingView.setColor(colorValue)
                }
                layoutColors.addView(colorBtn)
            }

            scrollViewColors.post {
                scrollViewColors.fullScroll(View.FOCUS_RIGHT)
            }
        }

        btnColorWheel.setOnClickListener {
            val pickerView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_picker, null)
            val colorWheel = pickerView.findViewById<ColorWheelView>(R.id.colorWheel)
            val valueSlider = pickerView.findViewById<ColorValueView>(R.id.colorValueSlider)
            val preview = pickerView.findViewById<View>(R.id.viewPreviewColor)
            val btnSelect = pickerView.findViewById<Button>(R.id.btnSelectColor)

            var currentHue = 0f
            var currentSat = 1f
            var currentValue = 1f

            fun updatePreview() {
                val color = Color.HSVToColor(floatArrayOf(currentHue, currentSat, currentValue))
                preview.backgroundTintList = ColorStateList.valueOf(color)
            }

            colorWheel.setOnColorSelectedListener { color ->
                val hsv = FloatArray(3)
                Color.colorToHSV(color, hsv)
                currentHue = hsv[0]
                currentSat = hsv[1]

                valueSlider.setHueSat(currentHue, currentSat)
                updatePreview()
            }

            valueSlider.setOnValueChangedListener { value ->
                currentValue = value
                updatePreview()
            }

            val pickerDialog = AlertDialog.Builder(requireContext())
                .setView(pickerView)
                .create()

            btnSelect.setOnClickListener {
                val finalColor = Color.HSVToColor(floatArrayOf(currentHue, currentSat, currentValue))

                if (recentColors.contains(finalColor)) recentColors.remove(finalColor)
                recentColors.add(0, finalColor)
                if (recentColors.size > maxRecentColors) recentColors.removeAt(recentColors.size - 1)

                refreshColors()

                val count = layoutColors.childCount
                if (count > 0) {
                    val newBtn = layoutColors.getChildAt(count - 1)
                    newBtn.performClick()
                }
                pickerDialog.dismiss()
            }
            pickerDialog.show()
        }

        refreshColors()

        val count = layoutColors.childCount
        if (count > 0) {
            val firstColorBtn = layoutColors.getChildAt(count - 1)
            firstColorBtn?.performClick()
        }

        btnUndo.setOnClickListener {
            SoundManager.playUndo()
            drawingView.undo()
        }
        btnRedo.setOnClickListener {
            SoundManager.playRedo()
            drawingView.redo()
        }
        btnBrush.setOnClickListener {
            SoundManager.playBrush()
            selectTool(it, Tool.BRUSH)
        }
        btnFill.setOnClickListener {
            SoundManager.playFill()
            selectTool(it, Tool.FILL)
        }
        btnEraser.setOnClickListener {
            SoundManager.playEraser()
            selectTool(it, Tool.ERASER)
        }
        sliderSize.addOnChangeListener { _, value, _ -> drawingView.setBrushSize(value) }
        btnClear.setOnClickListener {
            SoundManager.playDelete()
            drawingView.clearCanvas()
            selectTool(btnBrush, Tool.BRUSH)
        }

        selectTool(btnBrush, Tool.BRUSH)

        btnSave.setOnClickListener {
            val bitmap = drawingView.getBitmap()
            if (bitmap != null) {
                viewModel.updateAvatar(bitmap,
                    onSuccess = {
                        SoundManager.playSuccessModal()
                        UiUtils.showModal(requireContext(), "Updated", "Your new avatar is saved!")
                        dialog.dismiss()
                    },
                    onFailure = { msg ->
                        SoundManager.playErrorModal()
                        UiUtils.showModal(requireContext(), "Error", msg)
                    }
                )
            }
        }
        dialog.show()
    }
}