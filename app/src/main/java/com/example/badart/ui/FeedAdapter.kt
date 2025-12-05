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
                // MY BAD ART
                btnReport.visibility = View.GONE
                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE
                tvGuessHistory.visibility = View.VISIBLE

                // Show Warning if Hidden
                if (post.reportCount >= 3) {
                    tvReportWarning.visibility = View.VISIBLE
                } else {
                    tvReportWarning.visibility = View.GONE
                }

                if (post.isSolved) {
                    tvResult.text = "Status: SOLVED by ${post.winner}\nWord: ${post.wordToGuess}"
                } else {
                    tvResult.text = "Status: Unsolved\nWord: ${post.wordToGuess}"
                }

                if (post.guessHistory.isNotEmpty()) {
                    tvGuessHistory.text = "Recent Guesses:\n" + post.guessHistory.joinToString("\n")
                } else {
                    tvGuessHistory.text = "No guesses yet."
                }

            } else {
                // PUBLIC FEED MODE
                btnReport.visibility = View.VISIBLE
                tvGuessHistory.visibility = View.GONE
                tvReportWarning.visibility = View.GONE

                if (post.isSolved) {
                    layoutGuessing.visibility = View.GONE
                    tvResult.visibility = View.VISIBLE
                    tvResult.text = "Solved! The word was: ${post.wordToGuess}"
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