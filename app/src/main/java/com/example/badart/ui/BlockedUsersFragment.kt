package com.example.badart.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.badart.R
import com.example.badart.model.User
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.google.android.material.button.MaterialButton

class BlockedUsersFragment : Fragment(R.layout.fragment_blocked_users) {

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rvBlockedUsers = view.findViewById<RecyclerView>(R.id.rvBlockedUsers)
        val layoutEmpty = view.findViewById<View>(R.id.layoutEmpty)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        rvBlockedUsers.layoutManager = LinearLayoutManager(requireContext())

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val blockedUsers = user.blockedUsers.toList()
                if (blockedUsers.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvBlockedUsers.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvBlockedUsers.visibility = View.VISIBLE
                    rvBlockedUsers.adapter = BlockedUsersAdapter(blockedUsers.toMutableList())
                }
            }
        }
    }

    inner class BlockedUsersAdapter(
        private val blockedUsers: MutableList<String>
    ) : RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
            val tvUsername: TextView = view.findViewById(R.id.tvUsername)
            val btnUnblock: MaterialButton = view.findViewById(R.id.btnUnblock)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blocked_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val username = blockedUsers[position]
            holder.tvUsername.text = username

            // Reset avatar
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            holder.ivAvatar.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.medium_gray)

            // Try to fetch user avatar
            viewModel.getUserByName(username) { user ->
                if (user != null && user.avatarBase64.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        holder.ivAvatar.setImageBitmap(bitmap)
                        holder.ivAvatar.imageTintList = null
                    } catch (e: Exception) {
                        // Keep placeholder
                    }
                }
            }

            holder.btnUnblock.setOnClickListener {
                UiUtils.showConfirmation(requireContext(), "Unblock User", "Are you sure you want to unblock \"$username\"?") {
                    viewModel.unblockUser(username) {
                        UiUtils.showModal(requireContext(), "Unblocked", "\"$username\" has been unblocked.")
                        val pos = holder.adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            blockedUsers.removeAt(pos)
                            notifyItemRemoved(pos)
                            
                            if (blockedUsers.isEmpty()) {
                                view?.findViewById<View>(R.id.layoutEmpty)?.visibility = View.VISIBLE
                                view?.findViewById<RecyclerView>(R.id.rvBlockedUsers)?.visibility = View.GONE
                            }
                        }
                    }
                }
            }

            holder.itemView.setOnClickListener {
                // Navigate to user profile
                viewModel.getUserByName(username) { user ->
                    if (user != null) {
                        val bundle = Bundle().apply { putString("userId", user.userId) }
                        findNavController().navigate(R.id.action_blockedUsersFragment_to_profileFragment, bundle)
                    }
                }
            }
        }

        override fun getItemCount() = blockedUsers.size
    }
}
