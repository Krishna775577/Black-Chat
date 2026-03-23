package com.chitchat.app.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderLabel: String = "User",
    val text: String = "",
    val createdAt: Long = 0L
)
