package com.example.badart.ui

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post

class FeedAdapter(
    private var posts: List<Post>,
    private val onGuess: (Post, String) -> Unit,
    private val onReport: (Post) -> Unit,
    private val onDelete: (Post) -> Unit,
    private val onReact: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private var isMyArtMode = false
    private var currentUserId: String = ""

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.apply {

            if (post.imageBitmap != null) {
                ivDrawing.setImageBitmap(post.imageBitmap)
            } else {
                ivDrawing.setImageDrawable(null)
            }

            val myReactionEmoji = post.userReactions[currentUserId]
            val reactionBuilder = StringBuilder()

            post.reactions.forEach { (emoji, count) ->
                if (count > 0) {
                    if (emoji == myReactionEmoji) {
                        reactionBuilder.append("[ $emoji $count ]  ")
                    } else {
                        reactionBuilder.append("$emoji $count  ")
                    }
                }
            }

            val finalString = reactionBuilder.toString()
            val spannable = SpannableString(finalString)

            if (myReactionEmoji != null) {
                val startIndex = finalString.indexOf("[")
                val endIndex = finalString.indexOf("]") + 1
                if(startIndex != -1 && endIndex != -1) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, 0)
                }
            }

            tvReactions.text = spannable

            if (myReactionEmoji != null) {
                btnReact.alpha = 0.5f
                btnReact.setOnClickListener {
                    Toast.makeText(holder.itemView.context, "You reacted: $myReactionEmoji", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnReact.alpha = 1.0f
                btnReact.setOnClickListener {
                    onReact(post)
                }
            }

            if (isMyArtMode) {
                tvArtist.visibility = View.GONE
                btnReport.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
                btnReact.visibility = View.GONE

                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE
                tvGuessHistory.visibility = View.VISIBLE

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

                btnDelete.setOnClickListener {
                    onDelete(post)
                }

            } else {
                tvArtist.visibility = View.VISIBLE
                tvArtist.text = post.artistName
                btnReport.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
                btnReact.visibility = View.VISIBLE

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

    fun updateList(newPosts: List<Post>, myArtMode: Boolean, myUserId: String) {
        posts = newPosts
        isMyArtMode = myArtMode
        currentUserId = myUserId
        notifyDataSetChanged()
    }
}