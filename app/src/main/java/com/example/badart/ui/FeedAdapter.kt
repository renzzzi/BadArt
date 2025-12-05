package com.example.badart.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post

class FeedAdapter(
    private var posts: List<Post>,
    private val onGuess: (Post, String) -> Unit,
    private val onReport: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private var isMyArtMode = false

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.apply {
            tvArtist.text = post.artistName

            if (post.imageBitmap != null) {
                ivDrawing.setImageBitmap(post.imageBitmap)
            } else {
                ivDrawing.setImageDrawable(null)
            }

            if (isMyArtMode) {
                // MY BAD ART MODE: Show status, Hide Report, Hide Guessing
                btnReport.visibility = View.GONE
                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE

                if (post.isSolved) {
                    tvResult.text = "Status: SOLVED! Word: ${post.wordToGuess}"
                } else {
                    tvResult.text = "Status: Still guessing... Word: ${post.wordToGuess}"
                }

            } else {
                // PUBLIC FEED MODE
                btnReport.visibility = View.VISIBLE

                if (post.isSolved) {
                    layoutGuessing.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = "Solved! Answer: ${post.wordToGuess}"
                } else {
                    layoutGuessing.visibility = View.VISIBLE
                    tvResult.visibility = View.GONE

                    btnGuess.setOnClickListener {
                        val guess = etGuess.text.toString().trim()
                        if (guess.isNotEmpty()) {
                            onGuess(post, guess)
                            etGuess.text.clear()
                        }
                    }
                }

                btnReport.setOnClickListener {
                    onReport(post)
                }
            }
        }
    }

    override fun getItemCount() = posts.size

    fun updateList(newPosts: List<Post>, myArtMode: Boolean) {
        posts = newPosts
        isMyArtMode = myArtMode
        notifyDataSetChanged()
    }
}