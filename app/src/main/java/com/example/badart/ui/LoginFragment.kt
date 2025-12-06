package com.example.badart.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.badart.R
import com.example.badart.databinding.FragmentLoginBinding
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: SharedViewModel by activityViewModels()

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.firebaseLoginWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("LoginFragment", "Google Sign In Failed", e)
                UiUtils.showModal(requireContext(), "Login Error", "Google Sign In Failed. Code: ${e.statusCode}")
            }
        } else {
            Log.w("LoginFragment", "Google Sign In cancelled or failed, result code: ${result.resultCode}")
            UiUtils.showModal(requireContext(), "Login Info", "Google Sign In was cancelled or failed.")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        Log.d("LoginFragment", "View created")

        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnGoogleLogin.isEnabled = isChecked
            binding.btnGoogleLogin.alpha = if (isChecked) 1.0f else 0.5f
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Log.d("LoginFragment", "User logged in, navigating to feed.")
                findNavController().navigate(R.id.action_loginFragment_to_feedFragment)
            }
        }

        viewModel.loginError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e("LoginFragment", "Login error received: $error")
                UiUtils.showModal(requireContext(), "Login Failed", error)
            }
        }

        binding.btnGoogleLogin.setOnClickListener {
            Log.d("LoginFragment", "Google login button clicked")
            initiateGoogleLogin()
        }
    }

    private fun initiateGoogleLogin() {
        Log.d("LoginFragment", "Initiating Google login flow.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
}