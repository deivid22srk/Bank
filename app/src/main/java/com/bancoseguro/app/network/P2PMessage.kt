package com.bancoseguro.app.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

sealed class P2PMessage {
    abstract val type: String
    abstract val timestamp: Long
    abstract val senderId: String

    data class UserSync(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "USER_SYNC",
        val username: String,
        val balance: Double,
        val publicKey: String
    ) : P2PMessage()

    data class TransactionBroadcast(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "TRANSACTION",
        val transactionId: String,
        val fromUsername: String,
        val toUsername: String,
        val amount: Double,
        val signature: String
    ) : P2PMessage()

    data class PeerDiscovery(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "PEER_DISCOVERY",
        val username: String,
        val port: Int
    ) : P2PMessage()

    data class PeerResponse(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "PEER_RESPONSE",
        val username: String,
        val peers: List<PeerInfo>
    ) : P2PMessage()

    data class Ping(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "PING"
    ) : P2PMessage()

    data class Pong(
        override val senderId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val type: String = "PONG"
    ) : P2PMessage()

    companion object {
        private val gson = Gson()

        fun toJson(message: P2PMessage): String {
            return gson.toJson(message)
        }

        fun fromJson(json: String): P2PMessage? {
            return try {
                val baseMessage = gson.fromJson(json, BaseMessage::class.java)
                when (baseMessage.type) {
                    "USER_SYNC" -> gson.fromJson(json, UserSync::class.java)
                    "TRANSACTION" -> gson.fromJson(json, TransactionBroadcast::class.java)
                    "PEER_DISCOVERY" -> gson.fromJson(json, PeerDiscovery::class.java)
                    "PEER_RESPONSE" -> gson.fromJson(json, PeerResponse::class.java)
                    "PING" -> gson.fromJson(json, Ping::class.java)
                    "PONG" -> gson.fromJson(json, Pong::class.java)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private data class BaseMessage(
        val type: String,
        val timestamp: Long,
        val senderId: String
    )
}

data class PeerInfo(
    val peerId: String,
    val username: String,
    val address: String,
    val port: Int,
    val lastSeen: Long = System.currentTimeMillis()
)
