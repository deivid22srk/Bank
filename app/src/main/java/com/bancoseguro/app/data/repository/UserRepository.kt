package com.bancoseguro.app.data.repository

import com.bancoseguro.app.data.database.UserDao
import com.bancoseguro.app.data.models.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUserFlow(username: String): Flow<User?> = userDao.getUserFlow(username)
    
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUser(username: String): User? = userDao.getUser(username)

    suspend fun createUser(user: User) = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun updateBalance(username: String, balance: Double) = 
        userDao.updateBalance(username, balance)

    suspend fun userExists(username: String): Boolean = userDao.userExists(username)
}
