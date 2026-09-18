package com.moneyfy.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moneyfy.ui.navigation.MainBottomNavigation
import com.moneyfy.ui.navigation.Screen
import com.moneyfy.ui.theme.MoneyfyTheme
import com.moneyfy.ui.viewmodel.TransactionViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val transactionViewModel: TransactionViewModel = viewModel()
    var isDarkTheme by remember { mutableStateOf(true) }

    MoneyfyTheme(darkTheme = isDarkTheme) {
        Scaffold(
            bottomBar = { MainBottomNavigation(navController) },
            containerColor = MoneyfyTheme.colors.background
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        transactionViewModel = transactionViewModel,
                        onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                    )
                }
                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        transactionViewModel = transactionViewModel,
                        onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                    )
                }
                composable(Screen.Accounts.route) {
                    AccountsScreen()
                }
                composable(Screen.Contacts.route) {
                    ContactsScreen()
                }
                composable(Screen.Categories.route) {
                    CategoriesScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToHelpSupport = { navController.navigate(Screen.HelpSupport.route) }
                    )
                }
                composable(Screen.AddTransaction.route) {
                    AddTransactionScreen(
                        transactionViewModel = transactionViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.About.route) {
                    AboutScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.HelpSupport.route) {
                    HelpSupportScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
