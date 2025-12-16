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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.io.ByteArrayOutputStream
import java.util.UUID

class SharedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _leaderboard = MutableLiveData<List<User>>()
    val leaderboard: LiveData<List<User>> = _leaderboard

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        db.firestoreSettings = settings

        checkCurrentAuth()
    }

    private fun checkCurrentAuth() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            loadUserFromFirestore(firebaseUser.uid)
        }
    }

    fun firebaseLoginWithGoogle(idToken: String) {
        _loginError.value = null
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user!!
                val userRef = db.collection("users").document(firebaseUser.uid)

                userRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            loadUserFromFirestore(firebaseUser.uid)
                        } else {
                            val newUser = User(
                                userId = firebaseUser.uid,
                                username = firebaseUser.displayName ?: "Artist ${firebaseUser.uid.take(4)}",
                            )
                            userRef.set(newUser)
                                .addOnSuccessListener {
                                    _currentUser.value = newUser
                                    fetchPosts()
                                    fetchLeaderboard()
                                }
                                .addOnFailureListener { e ->
                                    _loginError.value = "Failed to create user profile: ${e.localizedMessage}"
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        _loginError.value = "Failed to load user profile: ${e.localizedMessage}"
                    }
            }
            .addOnFailureListener { e ->
                _loginError.value = "Firebase authentication failed: ${e.message}"
            }
    }

    private fun loadUserFromFirestore(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _currentUser.value = user
                    fetchPosts()
                    fetchLeaderboard()
                } else {
                    val newUser = User(userId = uid, username = "Returned User")
                    db.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            _currentUser.value = newUser
                            fetchPosts()
                            fetchLeaderboard()
                        }
                        .addOnFailureListener { e ->
                            _loginError.value = "Failed to create user profile: ${e.localizedMessage}"
                        }
                }
            }
            .addOnFailureListener { e ->
                _loginError.value = "Failed to load user profile: ${e.localizedMessage}"
            }
    }

    fun getUser(userId: String, onResult: (User?) -> Unit) {
        if (userId == _currentUser.value?.userId) {
            onResult(_currentUser.value)
            return
        }
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(User::class.java))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getUserByName(username: String, onResult: (User?) -> Unit) {
        db.collection("users").whereEqualTo("username", username).limit(1).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onResult(documents.documents[0].toObject(User::class.java))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun fetchLeaderboard() {
        db.collection("users")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value == null) return@addSnapshotListener
                val users = value.toObjects(User::class.java)
                _leaderboard.value = users
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
            artistId = user.userId,
            wordToGuess = word,
            imageBase64 = base64String,
            timestamp = System.currentTimeMillis()
        )

        db.collection("posts").document(newPost.id).set(newPost)

        db.collection("users").document(user.userId)
            .update("postCount", FieldValue.increment(1))
            .addOnSuccessListener {
                _currentUser.value = user.copy(postCount = user.postCount + 1)
            }
    }

    private fun fetchPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value == null) return@addSnapshotListener

                val user = _currentUser.value
                val blockedList = user?.blockedUsers ?: emptyList()
                val reportedList = user?.reportedPosts ?: emptyList()

                val postList = mutableListOf<Post>()

                for (doc in value.documents) {
                    val post = doc.toObject(Post::class.java)

                    if (post != null) {
                        if (blockedList.contains(post.artistName)) continue
                        if (reportedList.contains(post.id)) continue

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

    fun solvePost(post: Post, winnerName: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val postRef = db.collection("posts").document(post.id)
        val userRef = db.collection("users").document(currentUser.value!!.userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentPost = snapshot.toObject(Post::class.java)

            if (currentPost?.isSolved == true) {
                throw FirebaseFirestoreException("Already Solved", FirebaseFirestoreException.Code.ABORTED)
            }

            transaction.update(postRef, "isSolved", true)
            transaction.update(postRef, "winner", winnerName)

            val userSnap = transaction.get(userRef)
            val currentScore = userSnap.getLong("totalScore") ?: 0
            val currentGuesses = userSnap.getLong("correctGuesses") ?: 0

            transaction.update(userRef, "totalScore", currentScore + 10)
            transaction.update(userRef, "correctGuesses", currentGuesses + 1)
        }.addOnSuccessListener {
            val user = _currentUser.value
            if (user != null) {
                _currentUser.value = user.copy(
                    totalScore = user.totalScore + 10,
                    correctGuesses = user.correctGuesses + 1
                )
            }
            onSuccess()
        }.addOnFailureListener {
            onFailure()
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

        val user = _currentUser.value ?: return

        db.collection("users").document(user.userId)
            .update("reportedPosts", FieldValue.arrayUnion(post.id))
            .addOnSuccessListener {
                user.reportedPosts.add(post.id)
                _currentUser.value = user
                fetchPosts()
            }
    }

    fun deletePost(post: Post) {
        val user = _currentUser.value ?: return

        db.collection("posts").document(post.id).delete()
            .addOnSuccessListener {
                db.collection("users").document(user.userId)
                    .update("postCount", FieldValue.increment(-1))
                    .addOnSuccessListener {
                        val newCount = if (user.postCount > 0) user.postCount - 1 else 0
                        _currentUser.value = user.copy(postCount = newCount)
                    }
            }
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

    fun addReaction(post: Post, emoji: String, onFailure: ((String) -> Unit)? = null) {
        val user = _currentUser.value ?: return
        
        // Prevent reacting to your own post
        if (post.artistId == user.userId) {
            onFailure?.invoke("You cannot react to your own art")
            return
        }
        
        // Prevent multiple reactions from same user
        if (post.userReactions.containsKey(user.userId)) {
            onFailure?.invoke("You have already reacted to this post")
            return
        }

        val postRef = db.collection("posts").document(post.id)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val currentPost = snapshot.toObject(Post::class.java) ?: return@runTransaction

            if (currentPost.userReactions.containsKey(user.userId)) {
                return@runTransaction
            }

            val currentCount = currentPost.reactions[emoji] ?: 0
            transaction.update(postRef, "reactions.$emoji", currentCount + 1)
            transaction.update(postRef, "userReactions.${user.userId}", emoji)
        }
    }

    fun deductScore(points: Int, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val user = _currentUser.value ?: return
        if (user.totalScore < points) {
            onFailure()
            return
        }

        val newScore = user.totalScore - points
        db.collection("users").document(user.userId)
            .update("totalScore", newScore)
            .addOnSuccessListener {
                _currentUser.value = user.copy(totalScore = newScore)
                onSuccess()
            }
    }

    fun updateAvatar(bitmap: Bitmap, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = _currentUser.value ?: return
        val cost = if (user.hasChangedAvatar) 50 else 0

        if (user.totalScore < cost) {
            onFailure("Not enough points! Cost: $cost")
            return
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val newScore = user.totalScore - cost

        db.collection("users").document(user.userId)
            .update(
                "avatarBase64", base64String,
                "hasChangedAvatar", true,
                "totalScore", newScore
            )
            .addOnSuccessListener {
                _currentUser.value = user.copy(
                    avatarBase64 = base64String,
                    hasChangedAvatar = true,
                    totalScore = newScore
                )
                onSuccess()
            }
    }

    fun updateUsername(newName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = _currentUser.value ?: return
        val cost = if (user.hasChangedUsername) 50 else 0

        if (newName.length < 3) {
            onFailure("Too short")
            return
        }

        if (newName.length > 15) {
            onFailure("Username must be 15 characters or less")
            return
        }

        val usernameRegex = Regex("^[a-zA-Z0-9_]+$")
        if (!usernameRegex.matches(newName)) {
            onFailure("Only letters, numbers, and underscores allowed")
            return
        }

        if (user.totalScore < cost) {
            onFailure("Not enough points! Cost: $cost")
            return
        }

        // Check if username is already taken
        db.collection("users").whereEqualTo("username", newName).limit(1).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    onFailure("Username is already taken")
                    return@addOnSuccessListener
                }

                // Username is available, proceed with update
                val oldName = user.username
                val newScore = user.totalScore - cost

                db.collection("posts")
                    .whereEqualTo("artistName", oldName)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val batch = db.batch()

                        val userRef = db.collection("users").document(user.userId)
                        batch.update(userRef, "username", newName)
                        batch.update(userRef, "hasChangedUsername", true)
                        batch.update(userRef, "totalScore", newScore)

                        for (doc in snapshot.documents) {
                            batch.update(doc.reference, "artistName", newName)
                        }

                        batch.commit()
                            .addOnSuccessListener {
                                _currentUser.value = user.copy(
                                    username = newName,
                                    hasChangedUsername = true,
                                    totalScore = newScore
                                )
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.localizedMessage ?: "Update failed")
                            }
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.localizedMessage ?: "Network error")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.localizedMessage ?: "Failed to check username availability")
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun deleteAccount() {
        val user = _currentUser.value ?: return
        db.collection("users").document(user.userId).delete()
            .addOnSuccessListener {
                val firebaseUser = auth.currentUser
                firebaseUser?.delete()?.addOnCompleteListener {
                    logout()
                }
            }
    }
}