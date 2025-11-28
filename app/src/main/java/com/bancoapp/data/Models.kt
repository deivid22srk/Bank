package com.bancoapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String = "",
    val balance: Double = 1000.0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class UserWithPassword(
    val username: String,
    @SerialName("password_hash")
    val passwordHash: String,
    val balance: Double = 1000.0
)

@Serializable
data class UserPasswordOnly(
    @SerialName("password_hash")
    val passwordHash: String
)

@Serializable
data class Transaction(
    val id: String? = null,
    @SerialName("from_user")
    val fromUser: String = "",
    @SerialName("to_user")
    val toUser: String = "",
    val amount: Double = 0.0,
    val timestamp: String? = null,
    val status: String = "completed"
)

@Serializable
data class TransferRequest(
    @SerialName("sender_username")
    val senderUsername: String,
    @SerialName("receiver_username")
    val receiverUsername: String,
    @SerialName("transfer_amount")
    val transferAmount: Double
)

@Serializable
data class TransferResponse(
    val success: Boolean,
    val error: String? = null,
    @SerialName("transaction_id")
    val transactionId: String? = null,
    val message: String? = null
)

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
