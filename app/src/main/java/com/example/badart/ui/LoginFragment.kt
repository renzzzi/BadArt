package com.example.badart.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentLoginBinding
import com.example.badart.viewmodel.SharedViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnLogin.isEnabled = isChecked
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
            }
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            if(username.isNotEmpty()) {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.text = "Loading..."
                viewModel.loginUser(username)
            }
        }
    }
}