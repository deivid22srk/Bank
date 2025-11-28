package com.bancoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bancoapp.data.UiState
import com.bancoapp.ui.theme.*
import com.bancoapp.viewmodel.AuthViewModel
import com.bancoapp.viewmodel.BankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    bankViewModel: BankViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    val transferState by bankViewModel.transferState.collectAsState()
    
    LaunchedEffect(transferState) {
        if (transferState is UiState.Success) {
            recipient = ""
            amount = ""
            kotlinx.coroutines.delay(2000)
            bankViewModel.resetTransferState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transferir") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelBlue.copy(alpha = 0.3f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PastelBlue.copy(alpha = 0.1f),
                            PastelGreen.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceLight
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = DarkPastelBlue
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Saldo: ${formatCurrency(currentUser?.balance ?: 0.0)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextDark
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        OutlinedTextField(
                            value = recipient,
                            onValueChange = { recipient = it },
                            label = { Text("Nome do destinatário") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkPastelBlue,
                                unfocusedBorderColor = PastelBlue
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { 
                                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                    amount = it
                                }
                            },
                            label = { Text("Valor (R$)") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkPastelBlue,
                                unfocusedBorderColor = PastelBlue
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AnimatedVisibility(visible = transferState is UiState.Error) {
                            Text(
                                text = (transferState as? UiState.Error)?.message ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        AnimatedVisibility(visible = transferState is UiState.Success) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = PastelGreen
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = DarkPastelGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Transferência realizada!",
                                        color = TextDark,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                val amountValue = amount.toDoubleOrNull() ?: 0.0
                                if (amountValue > 0 && recipient.isNotBlank()) {
                                    bankViewModel.transfer(
                                        currentUser?.username ?: "",
                                        recipient,
                                        amountValue
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = recipient.isNotBlank() && 
                                    amount.isNotBlank() && 
                                    transferState !is UiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkPastelBlue
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (transferState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = TextDark
                                )
                            } else {
                                Text(
                                    text = "Confirmar Transferência",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
