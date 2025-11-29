package com.example.badart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badart.R
import com.example.badart.databinding.FragmentFeedBinding
import com.example.badart.model.Post

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var binding: FragmentFeedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)

        // FIX IS HERE: We use standard Android system icons instead of plain numbers
        val mockPosts = listOf(
            Post(
                id = "1",
                artistName = "User1",
                wordToGuess = "Apple",
                imageResId = android.R.drawable.ic_menu_gallery // Valid System ID
            ),
            Post(
                id = "2",
                artistName = "User2",
                wordToGuess = "Car",
                imageResId = android.R.drawable.ic_menu_camera // Valid System ID
            )
        )

        val adapter = FeedAdapter(mockPosts)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.fabDraw.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }
    }
}