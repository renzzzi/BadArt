package com.example.badart.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentDrawBinding
import com.example.badart.util.GameConstants
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel

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

        binding.btnBrush.setOnClickListener { binding.drawingView.setEraser(false) }
        binding.btnEraser.setOnClickListener { binding.drawingView.setEraser(true) }
        binding.btnFill.setOnClickListener { binding.drawingView.setFillMode(true) }
        binding.btnClear.setOnClickListener { binding.drawingView.clearCanvas() }

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

    private fun setupColorRibbon() {
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
                binding.drawingView.setColor(colorValue)
            }
            binding.layoutColors.addView(colorBtn)
        }
    }
}