package com.bancoseguro.app.data.repository

import com.bancoseguro.app.data.database.TransactionDao
import com.bancoseguro.app.data.database.UserDao
import com.bancoseguro.app.data.models.Transaction
import com.bancoseguro.app.data.models.TransactionStatus
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val userDao: UserDao
) {
    fun getTransactionsForUser(username: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsForUser(username)

    fun getRecentTransactions(limit: Int = 50): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val fromUser = userDao.getUser(transaction.fromUsername)
                ?: return Result.failure(Exception("Usuário remetente não encontrado"))

            if (!userDao.userExists(transaction.toUsername)) {
                return Result.failure(Exception("Usuário destinatário não encontrado"))
            }

            if (fromUser.balance < transaction.amount) {
                return Result.failure(Exception("Saldo insuficiente"))
            }

            if (transaction.amount <= 0) {
                return Result.failure(Exception("Valor inválido"))
            }

            val newFromBalance = fromUser.balance - transaction.amount
            userDao.updateBalance(transaction.fromUsername, newFromBalance)

            val toUser = userDao.getUser(transaction.toUsername)!!
            val newToBalance = toUser.balance + transaction.amount
            userDao.updateBalance(transaction.toUsername, newToBalance)

            val completedTransaction = transaction.copy(status = TransactionStatus.COMPLETED)
            transactionDao.insertTransaction(completedTransaction)

            Result.success(completedTransaction)
        } catch (e: Exception) {
            val failedTransaction = transaction.copy(status = TransactionStatus.FAILED)
            transactionDao.insertTransaction(failedTransaction)
            Result.failure(e)
        }
    }

    suspend fun getTransaction(id: String): Transaction? = transactionDao.getTransaction(id)
}
