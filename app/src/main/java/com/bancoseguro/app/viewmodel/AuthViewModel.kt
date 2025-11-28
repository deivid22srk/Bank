package com.bancoseguro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bancoseguro.app.data.database.AppDatabase
import com.bancoseguro.app.data.models.User
import com.bancoseguro.app.data.repository.UserRepository
import com.bancoseguro.app.security.NativeCrypto
import com.bancoseguro.app.security.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    
    private var userRepository: UserRepository? = null

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            if (secureStorage.isLoggedIn()) {
                val username = secureStorage.getUsername()!!
                initDatabase()
                _authState.value = AuthState.Authenticated(username)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun initDatabase() {
        if (userRepository == null) {
            val encryptionKey = secureStorage.getOrCreateEncryptionKey()
            val db = AppDatabase.getDatabase(getApplication(), encryptionKey)
            userRepository = UserRepository(db.userDao())
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                if (username.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Nome de usuário e senha são obrigatórios")
                    return@launch
                }
                
                if (username.length < 3) {
                    _authState.value = AuthState.Error("Nome de usuário deve ter pelo menos 3 caracteres")
                    return@launch
                }
                
                if (password.length < 6) {
                    _authState.value = AuthState.Error("Senha deve ter pelo menos 6 caracteres")
                    return@launch
                }

                initDatabase()
                
                val userExists = userRepository?.userExists(username) ?: false
                if (userExists) {
                    _authState.value = AuthState.Error("Nome de usuário já existe")
                    return@launch
                }

                val salt = secureStorage.getOrCreateSalt()
                val passwordHash = NativeCrypto.hashPassword(password, salt)

                secureStorage.saveUsername(username)
                secureStorage.savePasswordHash(passwordHash)
                secureStorage.saveSalt(salt)

                val newUser = User(
                    username = username,
                    balance = 1000.0,
                    publicKey = UUID.randomUUID().toString(),
                    peerId = UUID.randomUUID().toString()
                )
                userRepository?.createUser(newUser)

                _authState.value = AuthState.Authenticated(username)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao criar conta: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                if (username.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Nome de usuário e senha são obrigatórios")
                    return@launch
                }

                val storedUsername = secureStorage.getUsername()
                val storedPasswordHash = secureStorage.getPasswordHash()
                val salt = secureStorage.getSalt()

                if (storedUsername == null || storedPasswordHash == null || salt == null) {
                    _authState.value = AuthState.Error("Conta não encontrada. Crie uma nova conta.")
                    return@launch
                }

                if (username != storedUsername) {
                    _authState.value = AuthState.Error("Nome de usuário incorreto")
                    return@launch
                }

                val inputPasswordHash = NativeCrypto.hashPassword(password, salt)

                if (!inputPasswordHash.contentEquals(storedPasswordHash)) {
                    _authState.value = AuthState.Error("Senha incorreta")
                    return@launch
                }

                initDatabase()
                _authState.value = AuthState.Authenticated(username)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Erro ao fazer login: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val username: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
