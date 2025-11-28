package com.bancoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bancoapp.data.BancoRepository
import com.bancoapp.data.SessionManager
import com.bancoapp.data.UiState
import com.bancoapp.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = BancoRepository()
    private val sessionManager = SessionManager(application)
    
    private val _authState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            
            val result = repository.loginUser(username, password)
            
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = UiState.Success(user)
                sessionManager.saveSession(username)
                observeUser(username)
            }.onFailure { error ->
                _authState.value = UiState.Error(error.message ?: "Erro ao fazer login")
            }
        }
    }
    
    fun register(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            
            val result = repository.registerUser(username, password)
            
            result.onSuccess { user ->
                _currentUser.value = user
                _authState.value = UiState.Success(user)
                sessionManager.saveSession(username)
                observeUser(username)
            }.onFailure { error ->
                _authState.value = UiState.Error(error.message ?: "Erro ao registrar")
            }
        }
    }
    
    private fun observeUser(username: String) {
        viewModelScope.launch {
            repository.observeUser(username).collect { user ->
                if (user != null) {
                    _currentUser.value = user
                }
            }
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _authState.value = UiState.Idle
        sessionManager.clearSession()
    }
    
    fun restoreSession() {
        val username = sessionManager.getUsername()
        if (username != null) {
            viewModelScope.launch {
                val users = repository.getUserByUsername(username)
                users.onSuccess { user ->
                    _currentUser.value = user
                    observeUser(username)
                }
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = UiState.Idle
    }
}
