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

        // MOCK DATA (Offline Mode)
        // In Phase 2, you replace this with Firebase Firestore listener
        val mockPosts = listOf(
            Post("1", "User1", "Apple", null),
            Post("2", "User2", "Car", null)
        )

        val adapter = FeedAdapter(mockPosts)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // FAB to go to Drawing Screen
        binding.fabDraw.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }
    }
}