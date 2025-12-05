package com.example.badart.model

data class User(
    var userId: String = "",
    var username: String = "",
    var totalScore: Int = 0,
    var correctGuesses: Int = 0,
    var postCount: Int = 0,
    var avatarBase64: String = "",
    var blockedUsers: MutableList<String> = mutableListOf(),
    var reportedPosts: MutableList<String> = mutableListOf(),
    var hasChangedUsername: Boolean = false,
    var hasChangedAvatar: Boolean = false
)