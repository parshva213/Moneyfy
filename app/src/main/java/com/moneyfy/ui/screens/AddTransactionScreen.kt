package com.moneyfy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyfy.data.Account
import com.moneyfy.data.Category
import com.moneyfy.data.CategoryType
import com.moneyfy.data.Contact
import com.moneyfy.data.Transaction
import com.moneyfy.data.TransactionType
import com.moneyfy.ui.theme.*
import com.moneyfy.ui.viewmodel.AccountViewModel
import com.moneyfy.ui.viewmodel.CategoryViewModel
import com.moneyfy.ui.viewmodel.ContactViewModel
import com.moneyfy.ui.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionViewModel: TransactionViewModel = viewModel(),
    accountViewModel: AccountViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    contactViewModel: ContactViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val accounts by accountViewModel.allAccounts.collectAsState(initial = emptyList())
    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    val contacts by contactViewModel.allContacts.collectAsState(initial = emptyList())

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedToAccount by remember { mutableStateOf<Account?>(null) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    // Auto select first account/category if available
    LaunchedEffect(accounts) {
        if (selectedAccount == null && accounts.isNotEmpty()) {
            selectedAccount = accounts.first()
        }
    }
    LaunchedEffect(categories, selectedType) {
        val filteredCats = categories.filter {
            if (selectedType == TransactionType.INCOME) it.type == CategoryType.INCOME
            else it.type == CategoryType.EXPENSE
        }
        if (filteredCats.isNotEmpty() && (selectedCategory == null || selectedCategory?.type?.name != selectedType.name)) {
            selectedCategory = filteredCats.first()
        }
    }

    var accountExpanded by remember { mutableStateOf(false) }
    var toAccountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var contactExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Type Selector (Income, Expense, Transfer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTypeTab(
                    type = TransactionType.EXPENSE,
                    isSelected = selectedType == TransactionType.EXPENSE,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = TransactionType.EXPENSE }
                )
                TransactionTypeTab(
                    type = TransactionType.INCOME,
                    isSelected = selectedType == TransactionType.INCOME,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = TransactionType.INCOME }
                )
                TransactionTypeTab(
                    type = TransactionType.TRANSFER,
                    isSelected = selectedType == TransactionType.TRANSFER,
                    color = TransferBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = TransactionType.TRANSFER }
                )
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                        amountText = input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount (₹)") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title / Description") },
                placeholder = { Text("e.g. Lunch at Cafe, Salary") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Account Selector (From Account)
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Select Account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (selectedType == TransactionType.TRANSFER) "From Account" else "Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name, color = TextPrimary) },
                            onClick = {
                                selectedAccount = acc
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            // Transfer: To Account Selector
            if (selectedType == TransactionType.TRANSFER) {
                ExposedDropdownMenuBox(
                    expanded = toAccountExpanded,
                    onExpandedChange = { toAccountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedToAccount?.name ?: "Select Destination Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toAccountExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = toAccountExpanded,
                        onDismissRequest = { toAccountExpanded = false },
                        modifier = Modifier.background(DarkCard)
                    ) {
                        accounts.filter { it.id != selectedAccount?.id }.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name, color = TextPrimary) },
                                onClick = {
                                    selectedToAccount = acc
                                    toAccountExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Category Selector (for Income & Expense)
            if (selectedType != TransactionType.TRANSFER) {
                val availableCats = categories.filter {
                    if (selectedType == TransactionType.INCOME) it.type == CategoryType.INCOME
                    else it.type == CategoryType.EXPENSE
                }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(DarkCard)
                    ) {
                        availableCats.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Contact Selector (Optional)
            ExposedDropdownMenuBox(
                expanded = contactExpanded,
                onExpandedChange = { contactExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedContact?.name ?: "None (Optional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Contact") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                ExposedDropdownMenu(
                    expanded = contactExpanded,
                    onDismissRequest = { contactExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("None", color = TextSecondary) },
                        onClick = {
                            selectedContact = null
                            contactExpanded = false
                        }
                    )
                    contacts.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name, color = TextPrimary) },
                            onClick = {
                                selectedContact = c
                                contactExpanded = false
                            }
                        )
                    }
                }
            }

            // Notes Field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add any extra details...") },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Submit Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0) {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Add Transaction", "Please enter a valid amount")
                        return@Button
                    }
                    if (title.isBlank()) {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Add Transaction", "Please enter a title")
                        return@Button
                    }
                    if (selectedAccount == null) {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Add Transaction", "Please select an account")
                        return@Button
                    }
                    if (selectedType == TransactionType.TRANSFER && selectedToAccount == null) {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Add Transaction", "Please select destination account")
                        return@Button
                    }

                    val transaction = Transaction(
                        type = selectedType,
                        amount = amt,
                        title = title.trim(),
                        notes = notes.ifBlank { null },
                        date = System.currentTimeMillis(),
                        accountId = selectedAccount!!.id,
                        toAccountId = if (selectedType == TransactionType.TRANSFER) selectedToAccount?.id else null,
                        categoryId = if (selectedType != TransactionType.TRANSFER) selectedCategory?.id else null,
                        contactId = selectedContact?.id
                    )

                    transactionViewModel.addTransaction(transaction)
                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Transaction Added", "Transaction added successfully!")
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionTypeTab(
    type: TransactionType,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else DarkCard
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) (if (color == IncomeGreen) Color.Black else Color.White) else TextSecondary
            )
        }
    }
}
