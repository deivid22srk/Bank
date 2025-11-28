package com.bancoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bancoapp.data.BancoRepository
import com.bancoapp.data.Transaction
import com.bancoapp.data.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
    
    private val repository = BancoRepository()
    
    private val _transferState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val transferState: StateFlow<UiState<Unit>> = _transferState.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    fun observeTransactions(username: String) {
        viewModelScope.launch {
            repository.observeTransactions(username).collect { transactions ->
                _transactions.value = transactions
            }
        }
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
