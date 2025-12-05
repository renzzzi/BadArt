package com.example.badart.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentDrawBinding

class DrawFragment : Fragment(R.layout.fragment_draw) {

    private lateinit var binding: FragmentDrawBinding
    private val wordToDraw = "ZOMBIE"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDrawBinding.bind(view)

        binding.tvWordPrompt.text = getString(R.string.draw_prompt, wordToDraw)

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
        }

        binding.btnSubmit.setOnClickListener {
            val bitmap = binding.drawingView.getBitmap()
            if (bitmap != null) {
                Toast.makeText(requireContext(), getString(R.string.uploaded_feed), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}