package com.moneyfy.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfy.LoginActivity
import com.moneyfy.firebase.AuthRepository
import com.moneyfy.firebase.DefaultDataSeeder
import com.moneyfy.ui.theme.*
import com.moneyfy.ui.viewmodel.AccountViewModel
import com.moneyfy.ui.viewmodel.CategoryViewModel
import com.moneyfy.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    accountViewModel: AccountViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    isDarkTheme: Boolean = true,
    onThemeToggle: (Boolean) -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToHelpSupport: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository(context) }
    val currentUser = authRepo.currentUser

    // Live data counts
    val accounts by accountViewModel.allAccounts.collectAsState(initial = emptyList())
    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    val transactions by transactionViewModel.allTransactionsWithDetails.collectAsState(initial = emptyList())

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Settings preferences states
    var selectedCurrency by remember { mutableStateOf("₹ INR (Indian Rupee)") }
    var dailyReminders by remember { mutableStateOf(com.moneyfy.util.BiometricAuthManager.isReminderEnabled(context)) }
    var reminderTime by remember { mutableStateOf(com.moneyfy.util.BiometricAuthManager.getReminderTime(context)) }

    // Helper function call for exporting CSV to Download/Moneyfy


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoneyfyTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = MoneyfyTheme.colors.textPrimary
            )
            Text(
                text = "Account preferences & app configuration",
                style = MaterialTheme.typography.bodyMedium,
                color = MoneyfyTheme.colors.textSecondary
            )
        }

        // User Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User Profile",
                        tint = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.displayName?.ifBlank { null }
                            ?: currentUser?.email?.substringBefore("@") ?: "Moneyfy User",
                        style = MaterialTheme.typography.titleLarge,
                        color = MoneyfyTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.email ?: "local@moneyfy.app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyfyTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Firebase Sync Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Stats Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Accounts", count = accounts.size.toString())
                VerticalDivider(modifier = Modifier.height(36.dp), color = MoneyfyTheme.colors.surfaceVariant)
                StatItem(label = "Categories", count = categories.size.toString())
                VerticalDivider(modifier = Modifier.height(36.dp), color = MoneyfyTheme.colors.surfaceVariant)
                StatItem(label = "Transactions", count = transactions.size.toString())
            }
        }

        // Section: Preferences
        SettingsSectionHeader("Preferences")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Theme Mode",
                    subtitle = if (isDarkTheme) "Dark Theme Active" else "Light Theme Active",
                    trailing = {
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onThemeToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = Primary,
                                uncheckedTrackColor = MoneyfyTheme.colors.surfaceVariant
                            )
                        )
                    }
                )

                HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Default Currency",
                    subtitle = selectedCurrency,
                    onClick = { showCurrencyDialog = true }
                )

                HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                val formattedHour = if (reminderTime.first % 12 == 0) 12 else reminderTime.first % 12
                val amPm = if (reminderTime.first >= 12) "PM" else "AM"
                val formattedMinute = String.format("%02d", reminderTime.second)
                val timeString = "$formattedHour:$formattedMinute $amPm"

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Entry Reminders",
                    subtitle = if (dailyReminders) "Remind me daily at $timeString (Tap to change time)" else "Notifications disabled",
                    onClick = if (dailyReminders) { { showTimePickerDialog = true } } else null,
                    trailing = {
                        Switch(
                            checked = dailyReminders,
                            onCheckedChange = { enabled ->
                                dailyReminders = enabled
                                com.moneyfy.util.BiometricAuthManager.setReminderEnabled(context, enabled)
                                com.moneyfy.util.AppNotificationManager.showNotification(
                                    context,
                                    "Daily Reminders",
                                    if (enabled) "Daily reminder enabled for $timeString" else "Reminders turned OFF"
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Primary
                            )
                        )
                    }
                )
            }
        }

        // Section: Data Management & CSV Export
        SettingsSectionHeader("Data & Export")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Export Data (CSV)",
                    subtitle = "Save CSV file directly to Download/Moneyfy folder",
                    onClick = {
                        exportCsvToDownloads(context, scope, transactions)
                    }
                )

                HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                SettingsItem(
                    icon = Icons.Default.CloudSync,
                    title = "Sync Realtime Database",
                    subtitle = "Force manual sync with Firebase DB",
                    onClick = {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Sync", "Realtime Database is synchronized!")
                    }
                )

                HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                SettingsItem(
                    icon = Icons.Default.Refresh,
                    title = "Seed Default Data",
                    subtitle = "Re-seed standard accounts & categories",
                    onClick = { showResetDataDialog = true }
                )
            }
        }

        // Section: Security & Privacy
        SettingsSectionHeader("Security & Privacy")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.VpnKey,
                    title = "Reset Password",
                    subtitle = "Send password recovery email",
                    onClick = { showPasswordResetDialog = true }
                )
            }
        }

        // Section: Information & Pages
        SettingsSectionHeader("Information & Pages")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Moneyfy",
                    subtitle = "Version, tech stack & project highlights",
                    onClick = onNavigateToAbout
                )

                HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "FAQs, email support & feedback",
                    onClick = onNavigateToHelpSupport
                )
            }
        }

        // Account Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary.copy(alpha = 0.15f),
                    contentColor = Primary
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ExpenseRed
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            "₹ INR (Indian Rupee)",
            "$ USD (US Dollar)",
            "€ EUR (Euro)",
            "£ GBP (British Pound)",
            "¥ JPY (Japanese Yen)"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency", color = MoneyfyTheme.colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCurrency = curr
                                    showCurrencyDialog = false
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Currency Updated", "Currency set to $curr")
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCurrency == curr,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(curr, color = MoneyfyTheme.colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }

    // Time Picker Dialog for Daily Reminder
    if (showTimePickerDialog) {
        val timeSetListener = android.app.TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            reminderTime = Pair(hourOfDay, minute)
            com.moneyfy.util.BiometricAuthManager.saveReminderTime(context, hourOfDay, minute)
            showTimePickerDialog = false

            val formattedHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val amPm = if (hourOfDay >= 12) "PM" else "AM"
            val formattedMinute = String.format("%02d", minute)
            com.moneyfy.util.AppNotificationManager.showNotification(
                context,
                "Daily Reminders",
                "Daily reminder set to $formattedHour:$formattedMinute $amPm"
            )
        }

        DisposableEffect(Unit) {
            val picker = android.app.TimePickerDialog(
                context,
                timeSetListener,
                reminderTime.first,
                reminderTime.second,
                false
            )
            picker.setOnDismissListener { showTimePickerDialog = false }
            picker.show()
            onDispose { picker.dismiss() }
        }
    }

    // Seed Data Dialog
    if (showResetDataDialog) {
        AlertDialog(
            onDismissRequest = { showResetDataDialog = false },
            title = { Text("Seed Default Data", color = MoneyfyTheme.colors.textPrimary) },
            text = { Text("This will check and create default accounts and categories if missing.", color = MoneyfyTheme.colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDataDialog = false
                        scope.launch {
                            try {
                                DefaultDataSeeder.seedIfNeeded()
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Data", "Default data seeded successfully!")
                            } catch (e: Exception) {
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Data Error", "Failed to seed data")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                ) {
                    Text("Seed Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDataDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }

    // Password Reset Dialog
    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("Reset Password", color = MoneyfyTheme.colors.textPrimary) },
            text = {
                Text(
                    "A password reset email will be sent to:\n${currentUser?.email ?: "your registered email"}",
                    color = MoneyfyTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordResetDialog = false
                        val email = currentUser?.email ?: ""
                        if (email.isNotBlank()) {
                            scope.launch {
                                authRepo.resetPassword(email, "")
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Reset", "Password reset email sent!")
                            }
                        } else {
                            com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Reset Error", "No email address found")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                ) {
                    Text("Send Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", color = MoneyfyTheme.colors.textPrimary) },
            text = { Text("Are you sure you want to log out of your Moneyfy account?", color = MoneyfyTheme.colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authRepo.logout()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed, contentColor = Color.White)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = ExpenseRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Warning: Deleting your account will permanently remove all your cloud data, transactions, accounts, and categories. This action cannot be undone!",
                    color = MoneyfyTheme.colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        scope.launch {
                            authRepo.deleteAccount()
                                .onSuccess {
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Account Deleted", "Your account has been deleted")
                                    val intent = Intent(context, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                                .onFailure {
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Account Error", it.message ?: "Failed to delete account")
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed, contentColor = Color.White)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }
}

@Composable
private fun StatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MoneyfyTheme.colors.textSecondary
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MoneyfyTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MoneyfyTheme.colors.textPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MoneyfyTheme.colors.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MoneyfyTheme.colors.textSecondary)
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MoneyfyTheme.colors.textMuted)
        }
    }
}

private fun exportCsvToDownloads(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    transactions: List<com.moneyfy.data.TransactionWithDetails>
) {
    scope.launch(Dispatchers.IO) {
        try {
            val fileName = "Moneyfy_Transactions_${System.currentTimeMillis()}.csv"
            var success = false

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_DOWNLOADS}/Moneyfy")
                }

                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        writeCsvContent(outputStream, transactions)
                    }
                    success = true
                }
            } else {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val moneyfyFolder = java.io.File(downloadsDir, "Moneyfy")
                if (!moneyfyFolder.exists()) {
                    moneyfyFolder.mkdirs()
                }
                val targetFile = java.io.File(moneyfyFolder, fileName)
                targetFile.outputStream().use { outputStream ->
                    writeCsvContent(outputStream, transactions)
                }
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf("text/csv"),
                    null
                )
                success = true
            }

            withContext(Dispatchers.Main) {
                if (success) {
                    com.moneyfy.util.AppNotificationManager.showNotification(context, "CSV Export", "Saved to Downloads/Moneyfy/$fileName")
                } else {
                    com.moneyfy.util.AppNotificationManager.showNotification(context, "CSV Export Error", "Failed to create file in storage")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                com.moneyfy.util.AppNotificationManager.showNotification(context, "CSV Export Error", "Failed to export CSV: ${e.localizedMessage}")
            }
        }
    }
}

private fun writeCsvContent(
    outputStream: java.io.OutputStream,
    transactions: List<com.moneyfy.data.TransactionWithDetails>
) {
    val csvHeader = "ID,Type,Title,Amount,Date,Account,Category,Contact,Notes\n"
    outputStream.write(csvHeader.toByteArray())

    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    transactions.forEach { tx ->
        val line = "${tx.transaction.id},${tx.transaction.type.name},\"${tx.transaction.title}\",${tx.transaction.amount},\"${dateFormat.format(tx.transaction.date)}\",\"${tx.account.name}\",\"${tx.category?.name ?: ""}\",\"${tx.contact?.name ?: ""}\",\"${tx.transaction.notes ?: ""}\"\n"
        outputStream.write(line.toByteArray())
    }
}
