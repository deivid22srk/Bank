package com.bancoapp.data

import android.util.Log
import com.bancoapp.security.NativeCrypto
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BancoRepository {
    
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val transactionsRef = database.getReference("transactions")
    
    companion object {
        private const val TAG = "BancoRepository"
    }
    
    suspend fun registerUser(username: String, password: String): Result<User> {
        return try {
            val existingUser = usersRef.child(username).get().await()
            if (existingUser.exists()) {
                return Result.failure(Exception("Usuário já existe"))
            }
            
            val encryptedPassword = NativeCrypto.encryptString(password)
            
            val user = User(
                uid = username,
                username = username,
                balance = 1000.0
            )
            
            usersRef.child(username).setValue(user).await()
            usersRef.child(username).child("password").setValue(encryptedPassword).await()
            
            Log.i(TAG, "User registered: $username")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(username: String, password: String): Result<User> {
        return try {
            val snapshot = usersRef.child(username).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Usuário não encontrado"))
            }
            
            val storedPassword = snapshot.child("password").getValue(String::class.java) ?: ""
            val decryptedPassword = NativeCrypto.decryptString(storedPassword)
            
            if (decryptedPassword != password) {
                return Result.failure(Exception("Senha incorreta"))
            }
            
            val user = snapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("Erro ao carregar usuário"))
            
            Log.i(TAG, "User logged in: $username")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }
    
    fun observeUser(username: String): Flow<User?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "User observation cancelled", error.toException())
                close(error.toException())
            }
        }
        
        usersRef.child(username).addValueEventListener(listener)
        
        awaitClose {
            usersRef.child(username).removeEventListener(listener)
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
            
            val fromSnapshot = usersRef.child(fromUsername).get().await()
            val toSnapshot = usersRef.child(toUsername).get().await()
            
            if (!toSnapshot.exists()) {
                return Result.failure(Exception("Destinatário não encontrado"))
            }
            
            val fromUser = fromSnapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("Erro ao carregar saldo"))
            
            if (fromUser.balance < amount) {
                return Result.failure(Exception("Saldo insuficiente"))
            }
            
            val toUser = toSnapshot.getValue(User::class.java)
                ?: return Result.failure(Exception("Erro ao carregar destinatário"))
            
            val updates = hashMapOf<String, Any>(
                "users/$fromUsername/balance" to (fromUser.balance - amount),
                "users/$toUsername/balance" to (toUser.balance + amount)
            )
            
            database.reference.updateChildren(updates).await()
            
            val transaction = Transaction(
                id = System.currentTimeMillis().toString(),
                fromUser = fromUsername,
                toUser = toUsername,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                status = "completed"
            )
            
            transactionsRef.push().setValue(transaction).await()
            
            Log.i(TAG, "Transfer completed: $fromUsername -> $toUsername: $amount")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Transfer failed", e)
            Result.failure(e)
        }
    }
    
    fun observeTransactions(username: String): Flow<List<Transaction>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<Transaction>()
                for (child in snapshot.children) {
                    val transaction = child.getValue(Transaction::class.java)
                    if (transaction != null &&
                        (transaction.fromUser == username || transaction.toUser == username)) {
                        transactions.add(transaction)
                    }
                }
                transactions.sortByDescending { it.timestamp }
                trySend(transactions)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Transactions observation cancelled", error.toException())
                close(error.toException())
            }
        }
        
        transactionsRef.addValueEventListener(listener)
        
        awaitClose {
            transactionsRef.removeEventListener(listener)
        }
    }
}
