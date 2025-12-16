package com.example.badart.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.R
import com.example.badart.databinding.ItemLeaderboardBinding
import com.example.badart.model.User

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    private var users: List<User> = emptyList()

    inner class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val rank = position + 1

        holder.binding.tvRank.text = "#$rank"
        holder.binding.tvUsername.text = user.username
        holder.binding.tvScore.text = "${user.totalScore} pts"
        holder.binding.tvCorrectGuesses.text = "${user.correctGuesses} solved"
        holder.binding.tvPostCount.text = "${user.postCount} posts"

        // Display custom avatar if available
        if (user.avatarBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.binding.ivAvatar.setImageBitmap(bitmap)
                holder.binding.ivAvatar.imageTintList = null
            } catch (e: Exception) {
                // Fall back to placeholder if decoding fails
                holder.binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
                holder.binding.ivAvatar.imageTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.medium_gray)
            }
        } else {
            // Show placeholder for users without custom avatar
            holder.binding.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            holder.binding.ivAvatar.imageTintList = ContextCompat.getColorStateList(holder.itemView.context, R.color.medium_gray)
        }

        val colorRes = when(rank) {
            1 -> R.color.gold
            2 -> R.color.silver
            3 -> R.color.bronze
            else -> android.R.color.darker_gray
        }
        holder.binding.tvRank.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply { putString("userId", user.userId) }
            it.findNavController().navigate(R.id.action_leaderboardFragment_to_profileFragment, bundle)
        }
    }

    override fun getItemCount() = users.size

    fun updateList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}