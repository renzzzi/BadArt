package com.example.badart.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post

class FeedAdapter(
    private var posts: List<Post>,
    private val onGuess: (Post, String) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.apply {
            tvArtist.text = post.artistName
            if(post.imageBitmap != null) {
                ivDrawing.setImageBitmap(post.imageBitmap)
            } else {
                ivDrawing.setImageDrawable(null)
            }

            if (post.isSolved) {
                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE
                tvResult.text = "Solved! Answer: ${post.wordToGuess}"
            } else {
                layoutGuessing.visibility = View.VISIBLE
                tvResult.visibility = View.GONE

                btnGuess.setOnClickListener {
                    val guess = etGuess.text.toString().trim()
                    onGuess(post, guess)
                    etGuess.text.clear()
                }
            }
        }
    }

    override fun getItemCount() = posts.size

    fun updateList(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}