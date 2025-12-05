package com.example.badart.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.badart.model.Post
import com.example.badart.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.io.ByteArrayOutputStream
import java.util.UUID

class SharedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    init {
        fetchPosts()
    }

    fun loginUser(username: String) {
        val userId = username.lowercase().replace(" ", "_")
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document: DocumentSnapshot ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)!!
                _currentUser.value = user
                fetchPosts()
            } else {
                val newUser = User(userId, username)
                userRef.set(newUser)
                _currentUser.value = newUser
            }
        }
    }

    fun addPost(word: String, bitmap: Bitmap) {
        val user = _currentUser.value ?: return

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            artistName = user.username,
            wordToGuess = word,
            imageBase64 = base64String,
            timestamp = System.currentTimeMillis()
        )

        db.collection("posts").document(newPost.id).set(newPost)
    }

    private fun fetchPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null || value == null) {
                    return@addSnapshotListener
                }

                val user = _currentUser.value
                val blockedList = user?.blockedUsers ?: emptyList()

                val postList = mutableListOf<Post>()

                for (doc in value.documents) {
                    val post = doc.toObject(Post::class.java)

                    if (post != null) {
                        if (post.reportCount >= 3) continue
                        if (blockedList.contains(post.artistName)) continue

                        if (post.imageBase64.isNotEmpty()) {
                            try {
                                val decodedBytes = Base64.decode(post.imageBase64, Base64.DEFAULT)
                                post.imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        postList.add(post)
                    }
                }
                _posts.value = postList
            }
    }

    fun solvePost(post: Post, winnerName: String) {
        // Now that Post.kt enforces "isSolved", this update will match correctly
        db.collection("posts").document(post.id)
            .update(
                "isSolved", true,
                "winner", winnerName
            )
            .addOnFailureListener { e ->
                Log.e("BadArt", "Error updating post: ${e.message}")
            }

        val user = _currentUser.value ?: return
        val newScore = user.totalScore + 10

        db.collection("users").document(user.userId)
            .update("totalScore", newScore)
            .addOnSuccessListener {
                _currentUser.value = user.copy(totalScore = newScore)
            }
    }

    fun recordWrongGuess(post: Post, guess: String, guesserName: String) {
        val historyEntry = "$guesserName: $guess"
        db.collection("posts").document(post.id)
            .update("guessHistory", FieldValue.arrayUnion(historyEntry))
    }

    fun reportPost(post: Post) {
        db.collection("posts").document(post.id)
            .update("reportCount", post.reportCount + 1)
    }

    fun blockUser(artistName: String) {
        val user = _currentUser.value ?: return

        db.collection("users").document(user.userId)
            .update("blockedUsers", FieldValue.arrayUnion(artistName))
            .addOnSuccessListener {
                user.blockedUsers.add(artistName)
                _currentUser.value = user
                fetchPosts()
            }
    }
}