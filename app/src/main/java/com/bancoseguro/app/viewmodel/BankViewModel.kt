package com.bancoseguro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bancoseguro.app.data.database.AppDatabase
import com.bancoseguro.app.data.models.Transaction
import com.bancoseguro.app.data.models.User
import com.bancoseguro.app.data.repository.TransactionRepository
import com.bancoseguro.app.data.repository.UserRepository
import com.bancoseguro.app.security.SecureStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BankViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage(application)
    
    private val encryptionKey = secureStorage.getOrCreateEncryptionKey()
    private val db = AppDatabase.getDatabase(application, encryptionKey)
    private val userRepository = UserRepository(db.userDao())
    private val transactionRepository = TransactionRepository(db.transactionDao(), db.userDao())

    private val _uiState = MutableStateFlow<BankUiState>(BankUiState.Loading)
    val uiState: StateFlow<BankUiState> = _uiState

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState

    fun loadUserData(username: String) {
        viewModelScope.launch {
            try {
                combine(
                    userRepository.getUserFlow(username),
                    transactionRepository.getTransactionsForUser(username)
                ) { user, transactions ->
                    if (user != null) {
                        BankUiState.Success(user, transactions)
                    } else {
                        BankUiState.Error("Usuário não encontrado")
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = BankUiState.Error("Erro ao carregar dados: ${e.message}")
            }
        }
    }

    fun transfer(fromUsername: String, toUsername: String, amount: Double, description: String = "") {
        viewModelScope.launch {
            try {
                _transferState.value = TransferState.Loading

                if (toUsername.isBlank()) {
                    _transferState.value = TransferState.Error("Nome de usuário do destinatário é obrigatório")
                    return@launch
                }

                if (amount <= 0) {
                    _transferState.value = TransferState.Error("Valor deve ser maior que zero")
                    return@launch
                }

                if (fromUsername == toUsername) {
                    _transferState.value = TransferState.Error("Não é possível transferir para si mesmo")
                    return@launch
                }

                val transaction = Transaction(
                    fromUsername = fromUsername,
                    toUsername = toUsername,
                    amount = amount,
                    description = description
                )

                val result = transactionRepository.createTransaction(transaction)

                if (result.isSuccess) {
                    _transferState.value = TransferState.Success("Transferência realizada com sucesso!")
                } else {
                    _transferState.value = TransferState.Error(
                        result.exceptionOrNull()?.message ?: "Erro desconhecido"
                    )
                }
            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Erro ao realizar transferência: ${e.message}")
            }
        }
    }

    fun resetTransferState() {
        _transferState.value = TransferState.Idle
    }
}

sealed class BankUiState {
    object Loading : BankUiState()
    data class Success(val user: User, val transactions: List<Transaction>) : BankUiState()
    data class Error(val message: String) : BankUiState()
}

sealed class TransferState {
    object Idle : TransferState()
    object Loading : TransferState()
    data class Success(val message: String) : TransferState()
    data class Error(val message: String) : TransferState()
}
