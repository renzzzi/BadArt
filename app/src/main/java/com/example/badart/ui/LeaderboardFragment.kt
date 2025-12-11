package com.example.badart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLeaderboard.adapter = adapter

        viewModel.leaderboard.observe(viewLifecycleOwner) { users ->
            adapter.updateList(users)
            updateTopThree(users)
        }

        binding.layoutFirst.setOnClickListener { navigateToTopUser(0) }
        binding.layoutSecond.setOnClickListener { navigateToTopUser(1) }
        binding.layoutThird.setOnClickListener { navigateToTopUser(2) }
    }

    private fun updateTopThree(users: List<com.example.badart.model.User>) {
        if (users.isNotEmpty()) {
            binding.tvFirstName.text = users[0].username
            binding.tvFirstScore.text = "${users[0].totalScore} pts"
        }
        if (users.size > 1) {
            binding.tvSecondName.text = users[1].username
            binding.tvSecondScore.text = "${users[1].totalScore} pts"
        }
        if (users.size > 2) {
            binding.tvThirdName.text = users[2].username
            binding.tvThirdScore.text = "${users[2].totalScore} pts"
        }
    }

    private fun navigateToTopUser(index: Int) {
        val users = viewModel.leaderboard.value ?: return
        if (index < users.size) {
            val user = users[index]
            val bundle = Bundle().apply { putString("userId", user.userId) }
            findNavController().navigate(R.id.action_leaderboardFragment_to_profileFragment, bundle)
        }
    }
}