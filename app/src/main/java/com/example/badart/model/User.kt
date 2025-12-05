package com.example.badart.model

data class User(
    val userId: String,
    val username: String,
    var totalScore: Int = 0,
    val blockedUsers: MutableList<String> = mutableListOf()
)