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

    private var allPosts: List<Post> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)

        adapter = FeedAdapter(
            posts = emptyList(),
            onGuess = { post, guess ->
                val currentUser = viewModel.currentUser.value
                val myName = currentUser?.username ?: "Anonymous"

                if (guess.equals(post.wordToGuess, ignoreCase = true)) {
                    viewModel.solvePost(post, myName)
                    Toast.makeText(context, "Correct! +10 Points", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.recordWrongGuess(post, guess, myName)
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

        binding.btnEmptyAction.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_profileFragment)
        }
    }

    private fun updateFeedBasedOnTab() {
        val currentUser = viewModel.currentUser.value
        val myName = currentUser?.username ?: ""

        val filteredList: List<Post>
        val isMyArt: Boolean
        val tabIndex = binding.tabLayout.selectedTabPosition

        if (tabIndex == 0) {
            // TAB 0: My BadFeed
            // HIDE myself AND HIDE reported posts
            filteredList = allPosts.filter { it.artistName != myName && it.reportCount < 3 }
            isMyArt = false
        } else {
            // TAB 1: My BadArt
            // SHOW myself (including reported)
            filteredList = allPosts.filter { it.artistName == myName }
            isMyArt = true
        }

        if (filteredList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            if (tabIndex == 0) {
                // Empty Feed
                binding.tvEmptyState.text = "Nothing to see here, come back later."
                binding.btnEmptyAction.visibility = View.GONE
                binding.fabDraw.visibility = View.VISIBLE
            } else {
                // Empty My Art
                binding.tvEmptyState.text = "Create your first masterpiece"
                binding.btnEmptyAction.visibility = View.VISIBLE
                binding.fabDraw.visibility = View.GONE
            }
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.fabDraw.visibility = View.VISIBLE
            adapter.updateList(filteredList, isMyArt)
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