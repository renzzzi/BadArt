package com.example.badart.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.button.MaterialButton
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
import com.example.badart.views.Tool

class DrawFragment : Fragment(R.layout.fragment_draw) {

    private lateinit var binding: FragmentDrawBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private var wordToDraw = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDrawBinding.bind(view)

        wordToDraw = GameConstants.getRandomWord()
        binding.tvWordPrompt.text = "Draw: $wordToDraw"


        setupColorRibbon()
        setupToolButtons()

        binding.btnUndo.setOnClickListener { binding.drawingView.undo() }
        binding.btnRedo.setOnClickListener { binding.drawingView.redo() }

        binding.sliderBrushSize.addOnChangeListener { _, value, _ ->
            binding.drawingView.setBrushSize(value)
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

    private fun setupToolButtons() {
        binding.btnBrush.setOnClickListener { selectTool(it, Tool.BRUSH) }
        binding.btnEraser.setOnClickListener { selectTool(it, Tool.ERASER) }
        binding.btnFill.setOnClickListener { selectTool(it, Tool.FILL) }
        binding.btnClear.setOnClickListener { 
            binding.drawingView.clearCanvas()
            selectTool(binding.btnBrush, Tool.BRUSH) // Default to brush after clear
        }
        selectTool(binding.btnBrush, Tool.BRUSH) // Select brush by default
    }

    private fun selectTool(selectedButton: View, tool: Tool) {
        // Reset all tool buttons
        binding.toolsContainer.children.filterIsInstance<MaterialButton>().forEach { 
            it.strokeWidth = 0
        }

        // Highlight the selected tool button
        (selectedButton as? MaterialButton)?.apply {
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_color))
            strokeWidth = 8
        }

        binding.drawingView.setTool(tool)
    }

    private fun setupColorRibbon() {
        val colors = listOf(
            R.color.paint_black, R.color.paint_red, R.color.paint_blue,
            R.color.paint_green, R.color.paint_yellow, R.color.paint_orange,
            R.color.paint_purple
        )

        for (colorRes in colors) {
            val colorBtn = MaterialButton(requireContext())
            val params = LinearLayout.LayoutParams(100, 100)
            params.setMargins(8, 0, 8, 0)
            colorBtn.layoutParams = params
            val colorValue = ContextCompat.getColor(requireContext(), colorRes)
            colorBtn.setBackgroundColor(colorValue)

            colorBtn.setOnClickListener { view ->
                binding.layoutColors.children.forEach { child ->
                    (child as? MaterialButton)?.strokeWidth = 0
                }
                (view as? MaterialButton)?.apply {
                    strokeColor = ColorStateList.valueOf(Color.WHITE)
                    strokeWidth = 8
                }
                binding.drawingView.setColor(colorValue)
                selectTool(binding.btnBrush, Tool.BRUSH) // Switch back to brush on color selection
            }
            binding.layoutColors.addView(colorBtn)
        }

        // Select black color by default
        (binding.layoutColors.getChildAt(0) as? MaterialButton)?.performClick()
    }
}