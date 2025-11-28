package com.bancoseguro.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bancoseguro.app.data.models.Transaction
import com.bancoseguro.app.ui.theme.ErrorRed
import com.bancoseguro.app.ui.theme.PrimaryGreen
import com.bancoseguro.app.ui.theme.SecondaryBlue
import com.bancoseguro.app.ui.theme.SuccessGreen
import com.bancoseguro.app.viewmodel.BankUiState
import com.bancoseguro.app.viewmodel.BankViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    onLogout: () -> Unit,
    bankViewModel: BankViewModel = viewModel()
) {
    val uiState by bankViewModel.uiState.collectAsState()
    var showTransferDialog by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        if (username.isNotEmpty()) {
            bankViewModel.loadUserData(username)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Banco Seguro", color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sair",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        floatingActionButton = {
            if (uiState is BankUiState.Success) {
                FloatingActionButton(
                    onClick = { showTransferDialog = true },
                    containerColor = SecondaryBlue
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Transferir",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BankUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is BankUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                color = ErrorRed,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                is BankUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        BalanceCard(
                            username = state.user.username,
                            balance = state.user.balance
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Histórico de Transações",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (state.transactions.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nenhuma transação ainda",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.transactions) { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        currentUsername = username
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showTransferDialog) {
            TransferDialog(
                currentUsername = username,
                onDismiss = { showTransferDialog = false },
                onConfirm = { toUsername, amount ->
                    bankViewModel.transfer(username, toUsername, amount)
                    showTransferDialog = false
                },
                bankViewModel = bankViewModel
            )
        }
    }
}

@Composable
fun BalanceCard(username: String, balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryGreen, SecondaryBlue)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Olá, $username",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Saldo Disponível",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(8.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = formatCurrency(balance),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currentUsername: String) {
    val isReceived = transaction.toUsername == currentUsername
    val otherUsername = if (isReceived) transaction.fromUsername else transaction.toUsername
    val amountColor = if (isReceived) SuccessGreen else ErrorRed
    val amountPrefix = if (isReceived) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isReceived) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReceived) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = amountColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isReceived) "De: $otherUsername" else "Para: $otherUsername",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatDate(transaction.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = "$amountPrefix ${formatCurrency(transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit,
    bankViewModel: BankViewModel
) {
    var toUsername by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val transferState by bankViewModel.transferState.collectAsState()

    LaunchedEffect(transferState) {
        if (transferState is com.bancoseguro.app.viewmodel.TransferState.Success) {
            bankViewModel.resetTransferState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nova Transferência")
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = toUsername,
                    onValueChange = { toUsername = it },
                    label = { Text("Destinatário") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Valor") },
                    leadingIcon = {
                        Text("R$", modifier = Modifier.padding(start = 12.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (transferState is com.bancoseguro.app.viewmodel.TransferState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (transferState as com.bancoseguro.app.viewmodel.TransferState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && toUsername.isNotBlank()) {
                        onConfirm(toUsername, amountDouble)
                    }
                },
                enabled = amount.isNotBlank() && toUsername.isNotBlank()
            ) {
                Text("Transferir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatCurrency(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return formatter.format(value)
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    return formatter.format(Date(timestamp))
}
