package com.moneyfy.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.moneyfy.ui.theme.MoneyfyTheme

@Composable
fun MainBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MoneyfyTheme.colors.background,
        contentColor = MoneyfyTheme.colors.textSecondary
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, maxLines = 1, softWrap = false) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MoneyfyTheme.colors.background,
                    selectedTextColor = MoneyfyTheme.colors.textPrimary,
                    indicatorColor = com.moneyfy.ui.theme.Primary,
                    unselectedIconColor = MoneyfyTheme.colors.textSecondary,
                    unselectedTextColor = MoneyfyTheme.colors.textSecondary
                )
            )
        }
    }
}
