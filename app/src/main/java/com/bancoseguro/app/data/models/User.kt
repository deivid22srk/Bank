package com.bancoseguro.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val username: String,
    val balance: Double = 1000.0,
    val createdAt: Long = System.currentTimeMillis(),
    val publicKey: String = "",
    val peerId: String = ""
)
