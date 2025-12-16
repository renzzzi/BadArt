package com.example.badart.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badart.R
import com.example.badart.databinding.FragmentFeedBinding
import com.example.badart.model.Post
import com.example.badart.util.SoundManager
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: FeedAdapter

    private var allPosts: List<Post> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)

        adapter = FeedAdapter(
            posts = emptyList(),
            onGuess = { post, guess ->
                val currentUser = viewModel.currentUser.value
                val myName = currentUser?.username ?: "Anonymous"

                if (guess.equals(post.wordToGuess, ignoreCase = true)) {
                    viewModel.solvePost(post, myName,
                        onSuccess = {
                            triggerKonfetti()
                            SoundManager.playCorrectGuess()
                            UiUtils.showModal(requireContext(), "You Won!", "Correct! The word was ${post.wordToGuess}. +10 Points!")
                        },
                        onFailure = {
                            SoundManager.playWrongGuess()
                            UiUtils.showModal(requireContext(), "Too Late!", "Someone else solved this just before you!")
                        }
                    )
                } else {
                    viewModel.recordWrongGuess(post, guess, myName)
                    SoundManager.playWrongGuess()
                    UiUtils.showModal(requireContext(), "So Close!", "That is not the correct word. Try again!")
                }
            },
            onReport = { post -> showReportDialog(post) },
            onDelete = { post -> showDeleteDialog(post) },
            onReact = { post -> showReactionDialog(post) },
            onHint = { post ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Buy Hint?")
                    .setMessage("Spend 5 points to briefly reveal a letter?")
                    .setPositiveButton("Buy") { _, _ ->
                        viewModel.deductScore(5,
                            onSuccess = {
                                SoundManager.playCorrectGuess()
                                adapter.triggerHint(post.id, post.wordToGuess)
                            },
                            onFailure = {
                                SoundManager.playErrorModal()
                                UiUtils.showModal(requireContext(), "Oops", "You don't have enough points!")
                            }
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onShare = { post -> sharePost(post) },
            onViewReactions = { post -> showReactionsDialog(post) },
            onGetUser = { userId, callback -> viewModel.getUser(userId, callback) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            allPosts = posts
            updateFeedBasedOnTab()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateFeedBasedOnTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabDraw.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }

        binding.btnEmptyAction.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_drawFragment)
        }

        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_profileFragment)
        }

        binding.btnLeaderboard.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_leaderboardFragment)
        }
    }

    private fun triggerKonfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xFF006E, 0x8338EC, 0x3A86FF, 0x06FFA5, 0xFFBE0B),
            position = Position.Relative(0.5, 0.3),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
        )
        binding.konfettiView.start(party)
    }

    private fun updateFeedBasedOnTab() {
        val currentUser = viewModel.currentUser.value
        val myName = currentUser?.username ?: ""
        val myId = currentUser?.userId ?: ""

        val filteredList: List<Post>
        val isMyArt: Boolean
        val tabIndex = binding.tabLayout.selectedTabPosition

        if (tabIndex == 0) {
            filteredList = allPosts.filter { it.artistName != myName && it.reportCount < 3 }
            isMyArt = false
        } else {
            filteredList = allPosts.filter { it.artistName == myName }
            isMyArt = true
        }

        if (filteredList.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE

            if (tabIndex == 0) {
                binding.tvEmptyState.text = "Nothing to see here, come back later."
                binding.btnEmptyAction.visibility = View.GONE
                binding.fabDraw.visibility = View.VISIBLE
            } else {
                binding.tvEmptyState.text = "Create your first masterpiece"
                binding.btnEmptyAction.visibility = View.VISIBLE
                binding.fabDraw.visibility = View.GONE
            }
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.fabDraw.visibility = View.VISIBLE
            adapter.updateList(filteredList, isMyArt, myId)
        }
    }

    private fun showReactionDialog(post: Post) {
        // Check if user is trying to react to their own post
        val currentUser = viewModel.currentUser.value
        if (currentUser != null && post.artistId == currentUser.userId) {
            SoundManager.playErrorModal()
            UiUtils.showModal(requireContext(), "Oops", "You cannot react to your own art!")
            return
        }
        
        // Check if user has already reacted
        if (currentUser != null && post.userReactions.containsKey(currentUser.userId)) {
            SoundManager.playErrorModal()
            UiUtils.showModal(requireContext(), "Already Reacted", "You have already reacted to this post!")
            return
        }
        
        val reactions = arrayOf(
            "🎨 Masterpiece", "💩 Trash", "🤦 Facepalm",
            "😂 Hilarious", "🤨 Confused", "🔥 Lit",
            "👻 Spooky", "🧠 Big Brain"
        )
        val emojis = arrayOf("🎨", "💩", "🤦", "😂", "🤨", "🔥", "👻", "🧠")

        AlertDialog.Builder(requireContext())
            .setTitle("React to this Bad Art")
            .setItems(reactions) { _, which ->
                viewModel.addReaction(post, emojis[which]) { errorMessage ->
                    SoundManager.playErrorModal()
                    UiUtils.showModal(requireContext(), "Oops", errorMessage)
                }
                // Only show success if no error callback was triggered
                if (currentUser != null && !post.userReactions.containsKey(currentUser.userId)) {
                    UiUtils.showModal(requireContext(), "Reacted", "You added: ${emojis[which]}")
                }
            }
            .show()
    }

    private fun showReportDialog(post: Post) {
        val options = arrayOf("Report Content", "Block User")
        AlertDialog.Builder(requireContext())
            .setTitle("Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.reportPost(post)
                        UiUtils.showModal(requireContext(), "Reported", "Thanks for keeping BadArt safe. This post has been reported.")
                    }
                    1 -> {
                        viewModel.blockUser(post.artistName)
                        UiUtils.showModal(requireContext(), "Blocked", "You will no longer see art from ${post.artistName}.")
                    }
                }
            }
            .show()
    }

    private fun showDeleteDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Drawing")
            .setMessage("Are you sure you want to delete this masterpiece?")
            .setPositiveButton("Delete") { _, _ ->
                SoundManager.playDelete()
                viewModel.deletePost(post)
                UiUtils.showModal(requireContext(), "Deleted", "Your artwork has been removed.")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sharePost(post: Post) {
        SoundManager.playShare()
        val shareBitmap = generateShareableBitmap(post) ?: return

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "BadArt_${post.id}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BadArt")
            }
        }

        val uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    shareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.close()

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "Can you guess this drawing? Download BadArt and play now!")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        clipData = ClipData.newRawUri(null, uri)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share your Bad Art"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                UiUtils.showModal(requireContext(), "Share Error", "Failed to prepare image for sharing.")
            }
        } else {
            UiUtils.showModal(requireContext(), "Storage Error", "Could not save image to storage.")
        }
    }

    private fun generateShareableBitmap(post: Post): Bitmap? {
        val original = post.imageBitmap ?: return null
        val width = 1080
        val height = 1350
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary_color)
        canvas.drawColor(primaryColor)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 100f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawText("BadArt", width / 2f, 200f, textPaint)

        val boxSize = 900
        val left = (width - boxSize) / 2
        val top = (height - boxSize) / 2
        val destRect = Rect(left, top, left + boxSize, top + boxSize)
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(destRect, bgPaint)
        canvas.drawBitmap(original, null, destRect, null)

        textPaint.textSize = 60f
        canvas.drawText("Draw poorly. Guess correctly.", width / 2f, top + boxSize + 150f, textPaint)
        textPaint.textSize = 40f
        textPaint.alpha = 200
        canvas.drawText("Artist: ${post.artistName}", width / 2f, top + boxSize + 250f, textPaint)

        return result
    }

    private fun showReactionsDialog(post: Post) {
        if (post.userReactions.isEmpty()) {
            UiUtils.showModal(requireContext(), "No Reactions", "No one has reacted to this post yet.")
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reactions, null)
        val tabLayout = dialogView.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayoutReactions)
        val recyclerView = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvReactors)
        val tvNoReactions = dialogView.findViewById<TextView>(R.id.tvNoReactions)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Get unique emojis that have reactions
        val emojisWithReactions = post.userReactions.values.distinct()
        
        // Create list of all reactors (userId -> emoji)
        val allReactors = post.userReactions.toList()

        // Add "All" tab
        tabLayout.addTab(tabLayout.newTab().setText("All (${allReactors.size})"))
        
        // Add tab for each emoji
        emojisWithReactions.forEach { emoji ->
            val count = allReactors.count { it.second == emoji }
            tabLayout.addTab(tabLayout.newTab().setText("$emoji $count"))
        }

        // Create the dialog first so we can pass dismiss callback to adapter
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Reactions")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        // Create adapter with dismiss callback
        val reactorAdapter = ReactorAdapter(allReactors.toMutableList()) { 
            dialog.dismiss() 
        }
        recyclerView.adapter = reactorAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val filtered = if (position == 0) {
                    allReactors
                } else {
                    val selectedEmoji = emojisWithReactions[position - 1]
                    allReactors.filter { it.second == selectedEmoji }
                }
                reactorAdapter.updateList(filtered)
                tvNoReactions.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        dialog.show()
    }

    // Inner adapter for reactions list
    inner class ReactorAdapter(
        private var reactors: List<Pair<String, String>>,
        private val onDismiss: () -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ReactorAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val ivAvatar: android.widget.ImageView = view.findViewById(R.id.ivAvatar)
            val tvUsername: TextView = view.findViewById(R.id.tvUsername)
            val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reactor, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (userId, emoji) = reactors[position]
            holder.tvEmoji.text = emoji
            
            // Reset to placeholder first
            holder.ivAvatar.setImageResource(R.drawable.ic_person_placeholder)
            holder.ivAvatar.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.medium_gray)
            holder.tvUsername.text = "Loading..."
            
            // Try to get user info from viewModel
            viewModel.getUser(userId) { user ->
                holder.tvUsername.text = user?.username ?: "Unknown User"
                
                // Display custom avatar if available
                if (user != null && user.avatarBase64.isNotEmpty()) {
                    try {
                        val decodedBytes = android.util.Base64.decode(user.avatarBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        holder.ivAvatar.setImageBitmap(bitmap)
                        holder.ivAvatar.imageTintList = null
                    } catch (e: Exception) {
                        // Keep placeholder on error
                    }
                }
            }

            holder.itemView.setOnClickListener {
                // Dismiss dialog first, then navigate to user profile
                onDismiss()
                val bundle = Bundle().apply { putString("userId", userId) }
                findNavController().navigate(R.id.action_feedFragment_to_profileFragment, bundle)
            }
        }

        override fun getItemCount() = reactors.size

        fun updateList(newReactors: List<Pair<String, String>>) {
            reactors = newReactors
            notifyDataSetChanged()
        }
    }
}