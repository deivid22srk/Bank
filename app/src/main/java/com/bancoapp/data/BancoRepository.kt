package com.bancoapp.data

import android.util.Log
import com.bancoapp.BancoApplication
import com.bancoapp.security.NativeCrypto
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BancoRepository {
    
    private val supabase = BancoApplication.supabase
    
    companion object {
        private const val TAG = "BancoRepository"
    }
    
    suspend fun registerUser(username: String, password: String): Result<User> {
        return try {
            val existingUsers = supabase.from("users")
                .select(columns = Columns.list("username")) {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<User>()
            
            if (existingUsers.isNotEmpty()) {
                return Result.failure(Exception("Usuário já existe"))
            }
            
            val encryptedPassword = NativeCrypto.encryptString(password)
            
            val newUser = UserWithPassword(
                username = username,
                passwordHash = encryptedPassword,
                balance = 1000.0
            )
            
            val createdUser = supabase.from("users")
                .insert(newUser) {
                    select()
                }
                .decodeSingle<User>()
            
            Log.i(TAG, "User registered: $username")
            Result.success(createdUser)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.failure(Exception("Erro ao registrar: ${e.message}"))
        }
    }
    
    suspend fun loginUser(username: String, password: String): Result<User> {
        return try {
            val users = supabase.from("users")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<Map<String, Any>>()
            
            if (users.isEmpty()) {
                return Result.failure(Exception("Usuário não encontrado"))
            }
            
            val userData = users.first()
            val storedPassword = userData["password_hash"] as? String ?: ""
            val decryptedPassword = NativeCrypto.decryptString(storedPassword)
            
            if (decryptedPassword != password) {
                return Result.failure(Exception("Senha incorreta"))
            }
            
            val user = User(
                id = userData["id"] as? String,
                username = userData["username"] as? String ?: "",
                balance = (userData["balance"] as? Number)?.toDouble() ?: 0.0,
                createdAt = userData["created_at"] as? String,
                updatedAt = userData["updated_at"] as? String
            )
            
            Log.i(TAG, "User logged in: $username")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(Exception("Erro ao fazer login: ${e.message}"))
        }
    }
    
    fun observeUser(username: String): Flow<User?> = flow {
        val channel = supabase.realtime.channel("users_$username")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "users"
            filter = "username=eq.$username"
        }
        
        channel.subscribe()
        
        changeFlow.collect { action ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update -> {
                    try {
                        val users = supabase.from("users")
                            .select {
                                filter {
                                    eq("username", username)
                                }
                            }
                            .decodeList<User>()
                        
                        emit(users.firstOrNull())
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching updated user", e)
                    }
                }
                is PostgresAction.Delete -> emit(null)
                else -> {}
            }
        }
    }.catch { e ->
        Log.e(TAG, "User observation error", e)
        fetchUserPeriodically(username).collect { emit(it) }
    }
    
    private fun fetchUserPeriodically(username: String): Flow<User?> = flow {
        while (true) {
            try {
                val users = supabase.from("users")
                    .select {
                        filter {
                            eq("username", username)
                        }
                    }
                    .decodeList<User>()
                
                emit(users.firstOrNull())
                kotlinx.coroutines.delay(2000)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user", e)
                kotlinx.coroutines.delay(5000)
            }
        }
    }
    
    suspend fun transferMoney(
        fromUsername: String,
        toUsername: String,
        amount: Double
    ): Result<Unit> {
        return try {
            if (amount <= 0) {
                return Result.failure(Exception("Valor inválido"))
            }
            
            val request = TransferRequest(
                senderUsername = fromUsername,
                receiverUsername = toUsername,
                transferAmount = amount
            )
            
            val response = supabase.postgrest.rpc(
                function = "process_transfer",
                parameters = request
            )
            
            val json = Json.parseToJsonElement(response.data).jsonObject
            val success = json["success"]?.jsonPrimitive?.content?.toBoolean() ?: false
            
            if (success) {
                Log.i(TAG, "Transfer completed: $fromUsername -> $toUsername: $amount")
                Result.success(Unit)
            } else {
                val error = json["error"]?.jsonPrimitive?.content ?: "Erro desconhecido"
                Log.e(TAG, "Transfer failed: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Transfer failed", e)
            Result.failure(Exception("Erro ao transferir: ${e.message}"))
        }
    }
    
    fun observeTransactions(username: String): Flow<List<Transaction>> = flow {
        val channel = supabase.realtime.channel("transactions_$username")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "transactions"
        }
        
        channel.subscribe()
        
        emit(fetchTransactions(username))
        
        changeFlow.collect { action ->
            when (action) {
                is PostgresAction.Insert, is PostgresAction.Update, is PostgresAction.Delete -> {
                    try {
                        emit(fetchTransactions(username))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching transactions", e)
                    }
                }
                else -> {}
            }
        }
    }.catch { e ->
        Log.e(TAG, "Transactions observation error", e)
        fetchTransactionsPeriodically(username).collect { emit(it) }
    }
    
    private fun fetchTransactionsPeriodically(username: String): Flow<List<Transaction>> = flow {
        while (true) {
            try {
                emit(fetchTransactions(username))
                kotlinx.coroutines.delay(3000)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching transactions", e)
                kotlinx.coroutines.delay(5000)
            }
        }
    }
    
    private suspend fun fetchTransactions(username: String): List<Transaction> {
        val allTransactions = supabase.from("transactions")
            .select {
                order("timestamp", ascending = false)
            }
            .decodeList<Transaction>()
        
        return allTransactions.filter { transaction ->
            transaction.fromUser == username || transaction.toUser == username
        }
    }
}
