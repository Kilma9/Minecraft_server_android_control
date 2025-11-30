package com.kilma.minecraft.data

data class ChatMessage(
    val timestamp: Long,
    val player: String,
    val message: String,
    val isServerMessage: Boolean = false
)
