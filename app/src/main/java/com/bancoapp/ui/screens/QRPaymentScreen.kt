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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bancoapp.data.UiState
import com.bancoapp.ui.theme.*
import com.bancoapp.viewmodel.AuthViewModel
import com.bancoapp.viewmodel.BankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRPaymentScreen(
    recipientUsername: String,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    bankViewModel: BankViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var amount by remember { mutableStateOf("") }
    
    val transferState by bankViewModel.transferState.collectAsState()
    
    LaunchedEffect(transferState) {
        if (transferState is UiState.Success) {
            kotlinx.coroutines.delay(2000)
            bankViewModel.resetTransferState()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagar com QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PastelGreen.copy(alpha = 0.3f)
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
                            PastelGreen.copy(alpha = 0.1f),
                            PastelBlue.copy(alpha = 0.1f)
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
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = DarkPastelGreen
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "QR Code Detectado!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = PastelGreen.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Pagar para",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextLight
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = DarkPastelGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = recipientUsername,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Seu Saldo: ${formatCurrency(currentUser?.balance ?: 0.0)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { 
                                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                    amount = it
                                }
                            },
                            label = { Text("Valor (◐)") },
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkPastelGreen,
                                unfocusedBorderColor = PastelGreen
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AnimatedVisibility(visible = transferState is UiState.Error) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = (transferState as? UiState.Error)?.message ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
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
                                        text = "Pagamento realizado! ✓",
                                        color = TextDark,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = transferState !is UiState.Loading,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Cancelar")
                            }
                            
                            Button(
                                onClick = {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    if (amountValue > 0 && recipientUsername.isNotBlank()) {
                                        bankViewModel.transfer(
                                            currentUser?.username ?: "",
                                            recipientUsername,
                                            amountValue
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                enabled = amount.isNotBlank() && 
                                        amount.toDoubleOrNull() != null &&
                                        amount.toDouble() > 0 &&
                                        transferState !is UiState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkPastelGreen
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (transferState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Pagar",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
