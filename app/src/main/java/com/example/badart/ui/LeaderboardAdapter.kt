package com.example.badart.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        val colorRes = when(rank) {
            1 -> R.color.gold
            2 -> R.color.silver
            3 -> R.color.bronze
            else -> android.R.color.darker_gray
        }
        holder.binding.tvRank.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))
    }

    override fun getItemCount() = users.size

    fun updateList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}