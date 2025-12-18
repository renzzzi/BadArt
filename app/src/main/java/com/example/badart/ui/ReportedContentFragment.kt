package com.example.badart.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.badart.R
import com.example.badart.model.Post
import com.example.badart.util.UiUtils
import com.example.badart.viewmodel.SharedViewModel
import com.google.android.material.button.MaterialButton

class ReportedContentFragment : Fragment(R.layout.fragment_reported_content) {

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rvReportedContent = view.findViewById<RecyclerView>(R.id.rvReportedContent)
        val layoutEmpty = view.findViewById<View>(R.id.layoutEmpty)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        rvReportedContent.layoutManager = LinearLayoutManager(requireContext())

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val reportedPostIds = user.reportedPosts.toList()
                if (reportedPostIds.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvReportedContent.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvReportedContent.visibility = View.VISIBLE
                    
                    // Fetch reported posts
                    viewModel.getPostsByIds(reportedPostIds) { posts ->
                        rvReportedContent.adapter = ReportedContentAdapter(posts.toMutableList(), reportedPostIds.toMutableList())
                    }
                }
            }
        }
    }

    inner class ReportedContentAdapter(
        private val posts: MutableList<Post>,
        private val reportedIds: MutableList<String>
    ) : RecyclerView.Adapter<ReportedContentAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivArtistAvatar: ImageView = view.findViewById(R.id.ivArtistAvatar)
            val tvArtistName: TextView = view.findViewById(R.id.tvArtistName)
            val ivDrawing: ImageView = view.findViewById(R.id.ivDrawing)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val btnUnreport: MaterialButton = view.findViewById(R.id.btnUnreport)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reported_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = posts[position]
            
            holder.tvArtistName.text = post.artistName
            
            // Show solved status instead of the word
            if (post.isSolved) {
                holder.tvStatus.text = "Solved"
                holder.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.solved_green))
            } else {
                holder.tvStatus.text = "Unsolved"
                holder.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            }

            // Display drawing
            if (post.imageBase64.isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(post.imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.ivDrawing.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    holder.ivDrawing.setImageResource(R.drawable.ic_palette)
                }
            }

            // Reset artist avatar
            holder.ivArtistAvatar.setImageResource(R.drawable.ic_person_placeholder)
            holder.ivArtistAvatar.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.medium_gray)

            // Fetch artist avatar
            if (post.artistId.isNotEmpty()) {
                viewModel.getUser(post.artistId) { user ->
                    if (user != null && user.avatarBase64.isNotEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            holder.ivArtistAvatar.setImageBitmap(bitmap)
                            holder.ivArtistAvatar.imageTintList = null
                        } catch (e: Exception) {
                            // Keep placeholder
                        }
                    }
                }
            }

            holder.btnUnreport.setOnClickListener {
                UiUtils.showConfirmation(requireContext(), "Unreport Content", "Are you sure you want to remove your report from this post?") {
                    viewModel.unreportPost(post.id) {
                        UiUtils.showModal(requireContext(), "Unreported", "Your report has been removed.")
                        val pos = holder.adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            posts.removeAt(pos)
                            reportedIds.remove(post.id)
                            notifyItemRemoved(pos)
                            
                            if (posts.isEmpty()) {
                                view?.findViewById<View>(R.id.layoutEmpty)?.visibility = View.VISIBLE
                                view?.findViewById<RecyclerView>(R.id.rvReportedContent)?.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

        override fun getItemCount() = posts.size
    }
}
