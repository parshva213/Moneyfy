package com.moneyfy.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfy.data.Account
import com.moneyfy.data.AccountType
import com.moneyfy.ui.theme.*
import com.moneyfy.ui.viewmodel.AccountViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accountViewModel: AccountViewModel = viewModel()
) {
    val context = LocalContext.current
    val accounts by accountViewModel.allAccounts.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Accounts & Wallets",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Manage your cash, bank accounts & cards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No accounts found. Tap + to add one.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        AccountCard(
                            account = account,
                            onEdit = { accountToEdit = account },
                            onDelete = { accountViewModel.deleteAccount(account) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog || accountToEdit != null) {
        AddAccountDialog(
            initialAccount = accountToEdit,
            isSaving = isSaving,
            onDismiss = { 
                if (!isSaving) {
                    showAddDialog = false
                    accountToEdit = null
                }
            },
            onAdd = { account ->
                isSaving = true
                if (accountToEdit != null) {
                    accountViewModel.updateAccount(account.copy(id = accountToEdit!!.id))
                    isSaving = false
                    accountToEdit = null
                } else {
                    accountViewModel.addAccount(account) { success ->
                        isSaving = false
                        if (success) showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun AccountCard(account: Account, onEdit: () -> Unit, onDelete: () -> Unit) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val (icon, color) = when (account.type) {
        AccountType.CASH -> Pair(Icons.Default.Payments, IncomeGreen)
        AccountType.BANK -> Pair(Icons.Default.AccountBalance, InfoBlue)
        AccountType.WALLET -> Pair(Icons.Default.AccountBalanceWallet, Accent)
        AccountType.CREDIT_CARD -> Pair(Icons.Default.CreditCard, ExpenseRed)
        AccountType.OTHER -> Pair(Icons.Default.Folder, TextSecondary)
    }

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showMenu = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
            DropdownMenuItem(text = { Text("Delete", color = ExpenseRed) }, onClick = { showMenu = false; onDelete() })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = account.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(account.initialBalance),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Initial",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    initialAccount: Account? = null,
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onAdd: (Account) -> Unit
) {
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var initialBalance by remember { mutableStateOf(initialAccount?.initialBalance?.let { if (it == 0.0) "" else it.toString() } ?: "") }
    var selectedType by remember { mutableStateOf(initialAccount?.type ?: AccountType.BANK) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAccount == null) "Add New Account" else "Edit Account", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. HDFC Bank, Paytm Wallet") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            initialBalance = input
                        }
                    },
                    label = { Text("Initial Balance") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text("Account Type", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccountType.entries.take(4).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.take(4), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = initialBalance.toDoubleOrNull() ?: 0.0
                    onAdd(Account(name = name.trim(), type = selectedType, initialBalance = bal))
                },
                enabled = name.isNotBlank() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
            ) {
                Text(if (isSaving) "Saving..." else if (initialAccount == null) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkCard
    )
}
