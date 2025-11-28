package com.bancoapp.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val username: String = "",
    val balance: Double = 1000.0,
    val createdAt: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class Transaction(
    val id: String = "",
    val fromUser: String = "",
    val toUser: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "completed"
)

data class TransferRequest(
    val recipient: String,
    val amount: Double
)

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
