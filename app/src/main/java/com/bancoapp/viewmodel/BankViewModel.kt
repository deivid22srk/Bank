package com.bancoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bancoapp.data.BancoRepository
import com.bancoapp.data.Transaction
import com.bancoapp.data.UiState
import com.bancoapp.data.User
import com.bancoapp.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BankViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = BancoRepository()
    private val notificationHelper = NotificationHelper(application)
    
    private var lastBalance: Double? = null
    private var currentUsername: String? = null
    
    private val _transferState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val transferState: StateFlow<UiState<Unit>> = _transferState.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    fun observeTransactions(username: String) {
        viewModelScope.launch {
            repository.observeTransactions(username).collect { transactions ->
                val oldTransactions = _transactions.value
                _transactions.value = transactions
                
                if (oldTransactions.isNotEmpty() && transactions.isNotEmpty()) {
                    val newTransaction = transactions.firstOrNull { new ->
                        oldTransactions.none { old -> old.id == new.id }
                    }
                    
                    if (newTransaction != null && newTransaction.toUser == username) {
                        notificationHelper.showPaymentReceivedNotification(
                            newTransaction.amount,
                            newTransaction.fromUser
                        )
                    }
                }
            }
        }
    }
    
    fun observeUserBalance(user: User) {
        currentUsername = user.username
        lastBalance = user.balance
    }
    
    fun checkBalanceChange(newUser: User) {
        val oldBalance = lastBalance
        val newBalance = newUser.balance
        
        if (oldBalance != null && newBalance > oldBalance) {
            val difference = newBalance - oldBalance
            val latestTransaction = _transactions.value.firstOrNull { 
                it.toUser == currentUsername 
            }
            
            if (latestTransaction != null) {
                notificationHelper.showPaymentReceivedNotification(
                    difference,
                    latestTransaction.fromUser
                )
            }
        }
        
        lastBalance = newBalance
    }
    
    fun transfer(fromUsername: String, toUsername: String, amount: Double) {
        viewModelScope.launch {
            _transferState.value = UiState.Loading
            
            val result = repository.transferMoney(fromUsername, toUsername, amount)
            
            result.onSuccess {
                _transferState.value = UiState.Success(Unit)
            }.onFailure { error ->
                _transferState.value = UiState.Error(error.message ?: "Erro na transferência")
            }
        }
    }
    
    fun resetTransferState() {
        _transferState.value = UiState.Idle
    }
}
