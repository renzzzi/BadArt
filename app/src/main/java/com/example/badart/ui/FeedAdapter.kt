package com.example.badart.ui

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.R
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post

class FeedAdapter(
    private var posts: List<Post>,
    private val onGuess: (Post, String) -> Unit,
    private val onReport: (Post) -> Unit,
    private val onDelete: (Post) -> Unit,
    private val onReact: (Post) -> Unit,
    private val onHint: (Post) -> Unit,
    private val onShare: (Post) -> Unit
) : RecyclerView.Adapter<FeedAdapter.PostViewHolder>() {

    private var isMyArtMode = false
    private var currentUserId: String = ""

    // Map to store which indices are revealed for which post
    private val revealedIndices = mutableMapOf<String, MutableSet<Int>>()

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.binding.apply {

            // 1. IMAGE HANDLING
            if (post.imageBitmap != null) {
                ivDrawing.setImageBitmap(post.imageBitmap)
            } else {
                ivDrawing.setImageDrawable(null) // Or placeholder
            }

            // 2. REACTIONS
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

            // Bold the user's reaction
            val finalString = reactionBuilder.toString()
            val spannable = SpannableString(finalString)
            if (myReactionEmoji != null) {
                val startIndex = finalString.indexOf("[")
                val endIndex = finalString.indexOf("]") + 1
                if(startIndex != -1 && endIndex != -1) {
                    spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, 0)
                }
                btnReact.alpha = 0.5f // Visual feedback
            } else {
                btnReact.alpha = 1.0f
            }
            tvReactions.text = spannable
            btnReact.setOnClickListener { onReact(post) }
            btnShare.setOnClickListener { onShare(post) }

            // 3. ARTIST MODE (MY ART)
            if (isMyArtMode) {
                // Header
                headerSection.visibility = View.GONE // Hide artist header in My Art

                // Visibility Toggles
                btnDelete.visibility = View.VISIBLE
                btnReport.visibility = View.GONE
                layoutGuessing.visibility = View.GONE
                cardResult.visibility = View.VISIBLE
                cardGuessHistory.visibility = View.VISIBLE
                tvHintDisplay.visibility = View.GONE // Hide underscores for own art

                if (post.reportCount >= 3) cardReportWarning.visibility = View.VISIBLE
                else cardReportWarning.visibility = View.GONE

                if (post.isSolved) {
                    tvResult.text = "Status: SOLVED by ${post.winner}\nWord: ${post.wordToGuess}"
                    tvResult.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.solved_green))
                } else {
                    tvResult.text = "Status: Unsolved\nWord: ${post.wordToGuess}"
                    tvResult.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_secondary))
                }

                if (post.guessHistory.isNotEmpty()) {
                    tvGuessHistory.text = "Recent Guesses:\n" + post.guessHistory.joinToString("\n")
                } else {
                    tvGuessHistory.text = "No guesses yet."
                }

                btnDelete.setOnClickListener { onDelete(post) }

            } else {
                // 4. FEED MODE (GUESSING)
                headerSection.visibility = View.VISIBLE
                tvArtist.text = post.artistName
                btnReport.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
                cardGuessHistory.visibility = View.GONE
                cardReportWarning.visibility = View.GONE

                // LOGIC: Show Result vs Guessing Input
                if (post.isSolved) {
                    layoutGuessing.visibility = View.GONE
                    cardResult.visibility = View.VISIBLE
                    tvResult.text = "Solved! The word was: ${post.wordToGuess}"
                    tvResult.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.solved_green))
                    tvHintDisplay.visibility = View.GONE
                } else {
                    layoutGuessing.visibility = View.VISIBLE
                    cardResult.visibility = View.GONE

                    // SHOW UNDERSCORES
                    tvHintDisplay.visibility = View.VISIBLE
                    updateHintDisplay(post, tvHintDisplay)

                    btnHint.setOnClickListener { onHint(post) }

                    btnGuess.setOnClickListener {
                        val guess = etGuess.text.toString().trim()
                        if (guess.isNotEmpty()) {
                            onGuess(post, guess)
                            etGuess.text?.clear()
                        }
                    }
                }
                btnReport.setOnClickListener { onReport(post) }
            }
        }
    }

    // SIMPLIFIED HINT DISPLAY (No fading animation, just reveal)
    private fun updateHintDisplay(post: Post, textView: TextView) {
        val wordLen = post.wordToGuess.length
        val sb = StringBuilder()
        val revealed = revealedIndices[post.id] ?: mutableSetOf()

        for (i in 0 until wordLen) {
            val charAtI = post.wordToGuess[i]

            if (Character.isWhitespace(charAtI)) {
                sb.append("  ") // Double space for word separation
            } else if (revealed.contains(i)) {
                sb.append(charAtI).append(" ") // Show letter + space
            } else {
                sb.append("_ ") // Show underscore + space
            }
        }

        textView.text = sb.toString()
    }

    override fun getItemCount() = posts.size

    fun updateList(newPosts: List<Post>, myArtMode: Boolean, myUserId: String) {
        posts = newPosts
        isMyArtMode = myArtMode
        currentUserId = myUserId
        notifyDataSetChanged()
    }

    // Call this when user clicks "Hint" and has enough points
    fun triggerHint(postId: String, wordToGuess: String) {
        val revealed = revealedIndices.getOrPut(postId) { mutableSetOf() }

        // Find indices that are NOT yet revealed
        val unrevealedIndices = mutableListOf<Int>()
        for (i in wordToGuess.indices) {
            if (!revealed.contains(i) && !Character.isWhitespace(wordToGuess[i])) {
                unrevealedIndices.add(i)
            }
        }

        if (unrevealedIndices.isNotEmpty()) {
            val randomIndex = unrevealedIndices.random()
            revealed.add(randomIndex)
            notifyDataSetChanged() // Refresh UI to show new letter
        }
    }
}