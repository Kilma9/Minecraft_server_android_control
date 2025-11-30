package com.kilma.minecraft.data

data class SavedServer(
    val id: Long = 0,
    val name: String,
    val ip: String,
    val port: Int,
    val password: String
)
