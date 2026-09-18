package com.moneyfy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    data object Transactions : Screen("transactions", "Transactions", Icons.Default.SwapHoriz)
    data object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalance)
    data object Contacts : Screen("contacts", "Contacts", Icons.Default.People)
    data object Categories : Screen("categories", "Categories", Icons.Default.Category)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object AddTransaction : Screen("add_transaction", "Add", Icons.Default.SwapHoriz)
    data object EditTransaction : Screen("edit_transaction/{transactionId}", "Edit", Icons.Default.SwapHoriz)
    data object About : Screen("about", "About", Icons.Default.Info)
    data object HelpSupport : Screen("help_support", "Help & Support", Icons.Default.Help)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Accounts,
    Screen.Contacts,
    Screen.Categories,
    Screen.Settings
)
