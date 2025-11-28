package com.bancoseguro.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bancoseguro.app.ui.screens.AuthScreen
import com.bancoseguro.app.ui.screens.HomeScreen
import com.bancoseguro.app.viewmodel.AuthState
import com.bancoseguro.app.viewmodel.AuthViewModel

@Composable
fun BancoSeguroApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> "home"
            else -> "auth"
        }
    ) {
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthenticated = { username ->
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val username = when (val state = authState) {
                is AuthState.Authenticated -> state.username
                else -> ""
            }
            
            HomeScreen(
                username = username,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
