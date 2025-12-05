package com.example.badart.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badart.R
import com.example.badart.databinding.FragmentFeedBinding
import com.example.badart.model.Post
import com.example.badart.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: FeedAdapter

    // Store full list here, filter when sending to adapter
    private var allPosts: List<Post> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)

        adapter = FeedAdapter(
            posts = emptyList(),
            onGuess = { post, guess ->
                if (guess.equals(post.wordToGuess, ignoreCase = true)) {
                    viewModel.solvePost(post)
                    Toast.makeText(context, "Correct! +10 Points", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Wrong!", Toast.LENGTH_SHORT).show()
                }
            },
            onReport = { post ->
                showReportDialog(post)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            allPosts = posts
            updateFeedBasedOnTab()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateFeedBasedOnTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabDraw.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_profileFragment)
        }
    }

    private fun updateFeedBasedOnTab() {
        val currentUser = viewModel.currentUser.value
        val myName = currentUser?.username ?: ""

        if (binding.tabLayout.selectedTabPosition == 0) {
            // TAB 0: My BadFeed (Everyone else's art)
            val filtered = allPosts.filter { it.artistName != myName }
            adapter.updateList(filtered, myArtMode = false)
        } else {
            // TAB 1: My BadArt (Only my art)
            val filtered = allPosts.filter { it.artistName == myName }
            adapter.updateList(filtered, myArtMode = true)
        }
    }

    private fun showReportDialog(post: Post) {
        val options = arrayOf("Report Content", "Block User")
        AlertDialog.Builder(requireContext())
            .setTitle("Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.reportPost(post)
                        Toast.makeText(context, "Post Reported", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        viewModel.blockUser(post.artistName)
                        Toast.makeText(context, "User Blocked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}