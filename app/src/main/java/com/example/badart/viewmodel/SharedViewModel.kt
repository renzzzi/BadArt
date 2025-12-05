package com.example.badart.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.badart.model.Post
import com.example.badart.model.User
import java.util.UUID

class SharedViewModel : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _posts = MutableLiveData<MutableList<Post>>(mutableListOf())
    val posts: LiveData<MutableList<Post>> = _posts

    fun loginUser(username: String) {
        _currentUser.value = User(
            userId = UUID.randomUUID().toString(),
            username = username
        )
    }

    fun addPost(word: String, bitmap: Bitmap) {
        val user = _currentUser.value ?: return
        val newPost = Post(
            id = UUID.randomUUID().toString(),
            artistName = user.username,
            wordToGuess = word,
            imageBitmap = bitmap
        )
        val currentList = _posts.value ?: mutableListOf()
        currentList.add(0, newPost)
        _posts.value = currentList
    }

    fun solvePost(post: Post) {
        val currentList = _posts.value ?: return
        val index = currentList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            currentList[index] = post.copy(isSolved = true)
            _posts.value = currentList

            val user = _currentUser.value
            user?.let {
                it.totalScore += 10
                _currentUser.value = it
            }
        }
    }
}