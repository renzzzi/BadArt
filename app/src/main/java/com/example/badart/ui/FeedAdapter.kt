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

    private val activeHints = mutableMapOf<String, MutableMap<Int, Long>>()
    private val animationRunnables = mutableMapOf<String, Runnable>()

    inner class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.binding.tvHintDisplay.removeCallbacks(animationRunnables[post.id])

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
                btnReact.setOnClickListener { onReact(post) }
            }

            btnShare.setOnClickListener { onShare(post) }

            if (isMyArtMode) {
                tvArtist.visibility = View.GONE
                btnReport.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
                btnReact.visibility = View.GONE
                layoutGuessing.visibility = View.GONE
                tvResult.visibility = View.VISIBLE
                tvGuessHistory.visibility = View.VISIBLE

                if (post.reportCount >= 3) tvReportWarning.visibility = View.VISIBLE
                else tvReportWarning.visibility = View.GONE

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
                btnDelete.setOnClickListener { onDelete(post) }

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

                    tvHintDisplay.visibility = View.VISIBLE
                    startHintAnimation(post, tvHintDisplay)

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

    private fun startHintAnimation(post: Post, textView: TextView) {
        val primaryColor = ContextCompat.getColor(textView.context, R.color.primary_color)

        val runnable = object : Runnable {
            override fun run() {
                val wordLen = post.wordToGuess.length
                val sb = StringBuilder()
                val currentTime = System.currentTimeMillis()
                val postHints = activeHints[post.id] ?: mutableMapOf()

                for (i in 0 until wordLen) {
                    val startTime = postHints[i] ?: 0L
                    if (currentTime < startTime + 10000) {
                        sb.append(post.wordToGuess[i])
                    } else {
                        sb.append("_")
                    }
                    if (i < wordLen - 1) sb.append(" ")
                }

                val fullText = sb.toString()
                val spannable = SpannableString(fullText)
                var hasActiveAnimation = false

                for (i in 0 until wordLen) {
                    val startTime = postHints[i] ?: 0L

                    val spanIndex = i * 2

                    if (currentTime < startTime + 10000) {
                        hasActiveAnimation = true
                        val elapsed = currentTime - startTime
                        val fraction = elapsed / 10000f

                        val alpha = ((1.0f - fraction) * 255).toInt().coerceIn(0, 255)
                        val colorWithAlpha = Color.argb(alpha, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))

                        spannable.setSpan(
                            ForegroundColorSpan(colorWithAlpha),
                            spanIndex,
                            spanIndex + 1,
                            0
                        )
                    }
                }

                textView.text = spannable

                if (hasActiveAnimation) {
                    textView.postDelayed(this, 33)
                }
            }
        }

        animationRunnables[post.id] = runnable
        runnable.run()
    }

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.tvHintDisplay.removeCallbacks(null)
    }

    override fun getItemCount() = posts.size

    fun updateList(newPosts: List<Post>, myArtMode: Boolean, myUserId: String) {
        posts = newPosts
        isMyArtMode = myArtMode
        currentUserId = myUserId
        notifyDataSetChanged()
    }

    fun triggerHint(postId: String, wordToGuess: String) {
        val postHints = activeHints.getOrPut(postId) { mutableMapOf() }
        val currentTime = System.currentTimeMillis()
        val validIndices = mutableListOf<Int>()

        for (i in wordToGuess.indices) {
            val start = postHints[i] ?: 0L
            if (currentTime >= start + 10000) {
                validIndices.add(i)
            }
        }

        if (validIndices.isNotEmpty()) {
            val randomIndex = validIndices.random()
            postHints[randomIndex] = currentTime
            notifyDataSetChanged()
        }
    }
}
