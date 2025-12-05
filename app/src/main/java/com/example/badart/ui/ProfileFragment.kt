package com.example.badart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.badart.R
import com.example.badart.databinding.FragmentProfileBinding
import com.example.badart.viewmodel.SharedViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.tvUsername.text = user.username
            binding.tvScore.text = user.totalScore.toString()
        }
    }
}