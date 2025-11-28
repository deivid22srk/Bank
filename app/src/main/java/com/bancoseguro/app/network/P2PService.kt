package com.bancoseguro.app.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bancoseguro.app.BancoSeguroApp
import com.bancoseguro.app.data.database.AppDatabase
import com.bancoseguro.app.data.models.Transaction
import com.bancoseguro.app.data.models.TransactionStatus
import com.bancoseguro.app.data.repository.TransactionRepository
import com.bancoseguro.app.data.repository.UserRepository
import com.bancoseguro.app.security.NativeCrypto
import com.bancoseguro.app.security.SecureStorage
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class P2PService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private val peers = ConcurrentHashMap<String, PeerInfo>()
    private val secureStorage by lazy { SecureStorage(this) }
    private val peerId = UUID.randomUUID().toString()
    
    private lateinit var userRepository: UserRepository
    private lateinit var transactionRepository: TransactionRepository
    
    private val defaultPeerAddresses = listOf(
        "10.0.2.2:8888",
        "192.168.1.100:8888",
        "192.168.0.100:8888"
    )

    override fun onCreate() {
        super.onCreate()
        
        val encryptionKey = secureStorage.getOrCreateEncryptionKey()
        val db = AppDatabase.getDatabase(this, encryptionKey)
        userRepository = UserRepository(db.userDao())
        transactionRepository = TransactionRepository(db.transactionDao(), db.userDao())
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            launch { startP2PServer() }
            launch { discoverPeers() }
            launch { startPingLoop() }
        }
    }

    private suspend fun startP2PServer() {
        try {
            serverSocket = ServerSocket(DEFAULT_PORT)
            
            while (serviceScope.isActive) {
                try {
                    val clientSocket = serverSocket?.accept() ?: break
                    serviceScope.launch {
                        handleClient(clientSocket)
                    }
                } catch (e: Exception) {
                    if (serviceScope.isActive) {
                        delay(1000)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleClient(socket: Socket) {
        try {
            socket.use {
                val inputStream = it.getInputStream()
                val outputStream = it.getOutputStream()
                
                val buffer = ByteArray(8192)
                val bytesRead = inputStream.read(buffer)
                
                if (bytesRead > 0) {
                    val encryptionKey = secureStorage.getOrCreateEncryptionKey()
                    val obfuscatedData = buffer.copyOf(bytesRead)
                    
                    val deobfuscated = NativeCrypto.deobfuscateTraffic(obfuscatedData)
                    val decryptedData = NativeCrypto.decryptData(deobfuscated, encryptionKey)
                    
                    val json = String(decryptedData, Charsets.UTF_8)
                    val message = P2PMessage.fromJson(json)
                    
                    message?.let { msg ->
                        handleMessage(msg)
                        
                        val response = P2PMessage.Pong(senderId = peerId)
                        val responseJson = P2PMessage.toJson(response)
                        val encryptedResponse = NativeCrypto.encryptData(
                            responseJson.toByteArray(Charsets.UTF_8),
                            encryptionKey
                        )
                        val obfuscatedResponse = NativeCrypto.obfuscateTraffic(encryptedResponse)
                        outputStream.write(obfuscatedResponse)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun handleMessage(message: P2PMessage) {
        when (message) {
            is P2PMessage.UserSync -> {
                val user = userRepository.getUser(message.username)
                if (user == null) {
                }
            }
            is P2PMessage.TransactionBroadcast -> {
                val existingTx = transactionRepository.getTransaction(message.transactionId)
                if (existingTx == null) {
                    val transaction = Transaction(
                        id = message.transactionId,
                        fromUsername = message.fromUsername,
                        toUsername = message.toUsername,
                        amount = message.amount,
                        timestamp = message.timestamp,
                        status = TransactionStatus.COMPLETED,
                        signature = message.signature
                    )
                }
            }
            is P2PMessage.PeerDiscovery -> {
                val peerInfo = PeerInfo(
                    peerId = message.senderId,
                    username = message.username,
                    address = "",
                    port = message.port
                )
                peers[message.senderId] = peerInfo
            }
            is P2PMessage.PeerResponse -> {
                message.peers.forEach { peer ->
                    peers[peer.peerId] = peer
                }
            }
            is P2PMessage.Ping -> {
            }
            is P2PMessage.Pong -> {
            }
        }
    }

    private suspend fun discoverPeers() {
        while (serviceScope.isActive) {
            val username = secureStorage.getUsername() ?: "anonymous"
            val discoveryMessage = P2PMessage.PeerDiscovery(
                senderId = peerId,
                username = username,
                port = DEFAULT_PORT
            )
            
            defaultPeerAddresses.forEach { address ->
                serviceScope.launch {
                    try {
                        sendMessageToAddress(address, discoveryMessage)
                    } catch (e: Exception) {
                    }
                }
            }
            
            delay(30000)
        }
    }

    private suspend fun startPingLoop() {
        while (serviceScope.isActive) {
            delay(15000)
            
            val peersToRemove = mutableListOf<String>()
            val currentTime = System.currentTimeMillis()
            
            peers.values.forEach { peer ->
                if (currentTime - peer.lastSeen > 60000) {
                    peersToRemove.add(peer.peerId)
                } else {
                    serviceScope.launch {
                        try {
                            val pingMessage = P2PMessage.Ping(senderId = peerId)
                            sendMessageToPeer(peer, pingMessage)
                        } catch (e: Exception) {
                        }
                    }
                }
            }
            
            peersToRemove.forEach { peerId ->
                peers.remove(peerId)
            }
        }
    }

    private suspend fun sendMessageToAddress(address: String, message: P2PMessage) {
        withContext(Dispatchers.IO) {
            val parts = address.split(":")
            if (parts.size != 2) return@withContext
            
            val host = parts[0]
            val port = parts[1].toIntOrNull() ?: return@withContext
            
            try {
                Socket(host, port).use { socket ->
                    socket.soTimeout = 5000
                    
                    val encryptionKey = secureStorage.getOrCreateEncryptionKey()
                    val json = P2PMessage.toJson(message)
                    val encrypted = NativeCrypto.encryptData(
                        json.toByteArray(Charsets.UTF_8),
                        encryptionKey
                    )
                    val obfuscated = NativeCrypto.obfuscateTraffic(encrypted)
                    
                    socket.getOutputStream().write(obfuscated)
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun sendMessageToPeer(peer: PeerInfo, message: P2PMessage) {
        sendMessageToAddress("${peer.address}:${peer.port}", message)
    }

    suspend fun broadcastTransaction(transaction: Transaction) {
        val message = P2PMessage.TransactionBroadcast(
            senderId = peerId,
            transactionId = transaction.id,
            fromUsername = transaction.fromUsername,
            toUsername = transaction.toUsername,
            amount = transaction.amount,
            signature = transaction.signature
        )
        
        peers.values.forEach { peer ->
            serviceScope.launch {
                try {
                    sendMessageToPeer(peer, message)
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Banco Seguro")
            .setContentText("Rede P2P ativa")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "P2P Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        serverSocket?.close()
    }

    companion object {
        private const val CHANNEL_ID = "p2p_service_channel"
        private const val NOTIFICATION_ID = 1
        private const val DEFAULT_PORT = 8888
    }
}
