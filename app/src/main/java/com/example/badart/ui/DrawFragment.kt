package com.example.badart.ui

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentDrawBinding
import com.example.badart.util.GameConstants
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.example.badart.views.ColorValueView
import com.example.badart.views.ColorWheelView
import com.example.badart.views.Tool
import com.google.android.material.button.MaterialButton

class DrawFragment : Fragment(R.layout.fragment_draw) {

    private lateinit var binding: FragmentDrawBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private var wordToDraw = ""

    private val recentColors = mutableListOf<Int>()
    private val maxRecentColors = 8

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDrawBinding.bind(view)

        wordToDraw = GameConstants.getRandomWord()
        binding.tvWordPrompt.text = "Draw: $wordToDraw"

        initDefaultColors()
        setupColorRibbon()
        setupToolButtons()

        binding.btnUndo.setOnClickListener { binding.drawingView.undo() }
        binding.btnRedo.setOnClickListener { binding.drawingView.redo() }

        binding.sliderBrushSize.addOnChangeListener { _, value, _ ->
            binding.drawingView.setBrushSize(value)
        }

        binding.btnColorWheel.setOnClickListener {
            showColorPicker()
        }

        binding.btnSubmit.setOnClickListener {
            val bitmap = binding.drawingView.getBitmap()
            if (bitmap != null) {
                viewModel.addPost(wordToDraw, bitmap)
                UiUtils.showModal(requireContext(), "Success", "Your masterpiece has been uploaded to the feed!")
                findNavController().popBackStack()
            }
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

    private fun setupToolButtons() {
        binding.btnBrush.setOnClickListener { selectTool(it, Tool.BRUSH) }
        binding.btnEraser.setOnClickListener { selectTool(it, Tool.ERASER) }
        binding.btnFill.setOnClickListener { selectTool(it, Tool.FILL) }
        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
            selectTool(binding.btnBrush, Tool.BRUSH)
        }
        selectTool(binding.btnBrush, Tool.BRUSH)
    }

    private fun selectTool(selectedButton: View, tool: Tool) {
        binding.toolsContainer.children.filterIsInstance<MaterialButton>().forEach {
            it.strokeWidth = 0
        }

        (selectedButton as? MaterialButton)?.apply {
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_color))
            strokeWidth = 8
        }

        binding.drawingView.setTool(tool)
    }

    private fun setupColorRibbon() {
        binding.layoutColors.removeAllViews()

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
                highlightColorBtn(view)
                binding.drawingView.setColor(colorValue)
                selectTool(binding.btnBrush, Tool.BRUSH)
            }
            binding.layoutColors.addView(colorBtn)
        }

        binding.scrollViewColors.post {
            binding.scrollViewColors.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun highlightColorBtn(view: View) {
        binding.layoutColors.children.forEach { child ->
            val btn = child as? MaterialButton
            btn?.strokeWidth = 0
            (btn?.background as? GradientDrawable)?.setStroke(4, Color.LTGRAY)
        }

        val btn = view as? MaterialButton
        val bg = btn?.background as? GradientDrawable

        bg?.setStroke(6, Color.BLACK)

        btn?.strokeColor = ColorStateList.valueOf(Color.WHITE)
        btn?.strokeWidth = 6
    }

    private fun showColorPicker() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_color_picker, null)
        val colorWheel = dialogView.findViewById<ColorWheelView>(R.id.colorWheel)
        val valueSlider = dialogView.findViewById<ColorValueView>(R.id.colorValueSlider)
        val preview = dialogView.findViewById<View>(R.id.viewPreviewColor)
        val btnSelect = dialogView.findViewById<Button>(R.id.btnSelectColor)

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

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnSelect.setOnClickListener {
            val finalColor = Color.HSVToColor(floatArrayOf(currentHue, currentSat, currentValue))
            addNewColor(finalColor)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addNewColor(color: Int) {
        if (recentColors.contains(color)) {
            recentColors.remove(color)
        }
        recentColors.add(0, color)

        if (recentColors.size > maxRecentColors) {
            recentColors.removeAt(recentColors.size - 1)
        }

        setupColorRibbon()

        val count = binding.layoutColors.childCount
        if (count > 0) {
            val newBtn = binding.layoutColors.getChildAt(count - 1)
            newBtn.performClick()
        }
    }
}