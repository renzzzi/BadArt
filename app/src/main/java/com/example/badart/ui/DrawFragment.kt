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
    private val wordToDraw = "ZOMBIE" // In real app, pick random from list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDrawBinding.bind(view)

        binding.tvWordPrompt.text = "Draw: $wordToDraw"

        binding.btnClear.setOnClickListener {
            binding.drawingView.clearCanvas()
        }

        binding.btnSubmit.setOnClickListener {
            val bitmap = binding.drawingView.getBitmap()
            if (bitmap != null) {
                // OFFLINE MODE: We just pretend to upload
                Toast.makeText(requireContext(), "Uploaded to Feed!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Go back to feed
            }
        }
    }
}