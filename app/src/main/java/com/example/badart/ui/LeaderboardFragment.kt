package com.example.badart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badart.R
import com.example.badart.databinding.FragmentLeaderboardBinding
import com.example.badart.viewmodel.SharedViewModel

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {

    private lateinit var binding: FragmentLeaderboardBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private val adapter = LeaderboardAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeaderboardBinding.bind(view)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            adapter.updateList(users)
        }
    }
}