package com.example.badart.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post

class FeedAdapter(private val posts: List<Post>) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        holder.binding.apply {
            tvArtist.text = post.artistName
            ivDrawing.setImageResource(post.imageResId)

            if (post.isSolved) {
                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE
                tvResult.text = "Solved! Answer: ${post.wordToGuess}"
            } else {
                layoutGuessing.visibility = View.VISIBLE
                tvResult.visibility = View.GONE
            }

            btnGuess.setOnClickListener {
                val guess = etGuess.text.toString().trim()
                if (guess.equals(post.wordToGuess, ignoreCase = true)) {
                    post.isSolved = true
                    notifyItemChanged(position)
                    Toast.makeText(context, "Correct! +10 Points", Toast.LENGTH_SHORT).show()
                } else {
                    etGuess.error = "Wrong!"
                }
            }

            btnReport.setOnClickListener {
                Toast.makeText(context, "Post Reported.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = posts.size
}