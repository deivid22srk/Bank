package com.bancoseguro.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fromUsername: String,
    val toUsername: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val status: TransactionStatus = TransactionStatus.PENDING,
    val signature: String = ""
)

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}
