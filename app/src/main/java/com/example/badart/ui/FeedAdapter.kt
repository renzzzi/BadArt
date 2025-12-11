package com.example.badart.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.R
import com.example.badart.databinding.ItemPostBinding
import com.example.badart.model.Post
import com.google.android.material.chip.Chip
import com.example.badart.util.SoundManager

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

    private val revealedIndices = mutableMapOf<String, MutableSet<Int>>()

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

            chipGroupReactions.removeAllViews()
            val myReactionEmoji = post.userReactions[currentUserId]

            post.reactions.forEach { (emoji, count) ->
                if (count > 0) {
                    val chip = Chip(holder.itemView.context).apply {
                        text = "$emoji $count"
                        chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.light_gray)
                        chipStrokeWidth = 0f
                        isClickable = false
                    }

                    if (emoji == myReactionEmoji) {
                        val baseColor = ContextCompat.getColor(holder.itemView.context, R.color.primary_light)
                        val translucentColor = Color.argb(77, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
                        chip.chipBackgroundColor = ColorStateList.valueOf(translucentColor)
                        chip.chipStrokeColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.primary_dark)
                        chip.chipStrokeWidth = 4f
                    }

                    chipGroupReactions.addView(chip)
                }
            }

            btnReact.alpha = if (myReactionEmoji != null) 0.5f else 1.0f
            btnReact.setOnClickListener {
                onReact(post)
            }
            btnShare.setOnClickListener { onShare(post) }

            if (isMyArtMode) {
                headerSection.visibility = View.VISIBLE
                ivArtistAvatar.visibility = View.GONE
                tvArtist.visibility = View.GONE

                btnDelete.visibility = View.VISIBLE
                btnReport.visibility = View.GONE
                layoutGuessing.visibility = View.GONE
                cardResult.visibility = View.VISIBLE
                cardGuessHistory.visibility = View.VISIBLE
                tvHintDisplay.visibility = View.GONE

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
                headerSection.visibility = View.VISIBLE
                ivArtistAvatar.visibility = View.VISIBLE
                tvArtist.visibility = View.VISIBLE
                tvArtist.text = post.artistName

                val navigateToProfile = View.OnClickListener {
                    if (post.artistId.isNotEmpty()) {
                        val bundle = Bundle().apply { putString("userId", post.artistId) }
                        it.findNavController().navigate(R.id.action_feedFragment_to_profileFragment, bundle)
                    }
                }

                ivArtistAvatar.isClickable = true
                ivArtistAvatar.isFocusable = true
                ivArtistAvatar.setOnClickListener(navigateToProfile)

                tvArtist.isClickable = true
                tvArtist.isFocusable = true
                tvArtist.setOnClickListener(navigateToProfile)

                btnReport.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
                cardGuessHistory.visibility = View.GONE
                cardReportWarning.visibility = View.GONE

                if (post.isSolved) {
                    layoutGuessing.visibility = View.GONE
                    cardResult.visibility = View.VISIBLE
                    tvResult.text = "Solved! The word was: ${post.wordToGuess}"
                    tvResult.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.solved_green))
                    tvHintDisplay.visibility = View.GONE
                } else {
                    layoutGuessing.visibility = View.VISIBLE
                    cardResult.visibility = View.GONE

                    tvHintDisplay.visibility = View.VISIBLE
                    updateHintDisplay(post, tvHintDisplay)

                    btnHint.setOnClickListener {
                        onHint(post)
                    }

                    btnGuess.setOnClickListener {
                        val guess = etGuess.text.toString().trim()
                        if (guess.isNotEmpty()) {
                            onGuess(post, guess)
                            etGuess.text?.clear()
                        }
                    }
                }
                btnReport.setOnClickListener {
                    onReport(post)
                }
            }
        }
    }

    private fun updateHintDisplay(post: Post, textView: TextView) {
        val wordLen = post.wordToGuess.length
        val sb = StringBuilder()
        val revealed = revealedIndices[post.id] ?: mutableSetOf()

        for (i in 0 until wordLen) {
            val charAtI = post.wordToGuess[i]

            if (Character.isWhitespace(charAtI)) {
                sb.append("  ")
            } else if (revealed.contains(i)) {
                sb.append(charAtI).append(" ")
            } else {
                sb.append("_ ")
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

    fun triggerHint(postId: String, wordToGuess: String) {
        val revealed = revealedIndices.getOrPut(postId) { mutableSetOf() }

        val unrevealedIndices = mutableListOf<Int>()
        for (i in wordToGuess.indices) {
            if (!revealed.contains(i) && !Character.isWhitespace(wordToGuess[i])) {
                unrevealedIndices.add(i)
            }
        }

        if (unrevealedIndices.isNotEmpty()) {
            val randomIndex = unrevealedIndices.random()
            revealed.add(randomIndex)
            notifyDataSetChanged()
        }
    }
}