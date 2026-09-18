package com.moneyfy.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneyfy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MoneyfyTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MoneyfyTheme.colors.background)
            )
        },
        containerColor = MoneyfyTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card
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
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("How can we help you?", style = MaterialTheme.typography.titleLarge, color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.Bold)
                        Text("Explore FAQs or reach out to support team", style = MaterialTheme.typography.bodySmall, color = MoneyfyTheme.colors.textSecondary)
                    }
                }
            }

            // Section: Frequently Asked Questions
            Text("Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)

            val faqs = listOf(
                "How do I add a new transaction?" to "Tap the + button from the Home screen or Transactions tab. Select Expense, Income, or Transfer, enter the amount, title, and account, then tap Save.",
                "Can I export my data to Excel or CSV?" to "Yes! Go to Settings > Export Data (CSV). You can select your own folder destination to save your full transaction history.",
                "How does offline mode work?" to "Moneyfy saves all accounts and transactions locally. Once internet connectivity is restored, it syncs automatically with Firebase Realtime Database.",
                "How do transfers between accounts work?" to "In the Add Transaction screen, select 'Transfer'. Choose your source account (From Account) and destination account (To Account).",
                "How do I change my security question?" to "You can update security recovery details when registering or through the Security Settings option in the Settings tab."
            )

            faqs.forEach { (question, answer) ->
                FaqExpandableCard(question = question, answer = answer)
            }

            // Section: Contact & Feedback
            Text("Contact Us", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@moneyfy.app")
                                    putExtra(Intent.EXTRA_SUBJECT, "Moneyfy App Support Inquiry")
                                }
                                try {
                                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                                } catch (e: Exception) {
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Support Error", "Email client not found. Support email: support@moneyfy.app")
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Email Support", style = MaterialTheme.typography.titleMedium, color = MoneyfyTheme.colors.textPrimary)
                            Text("support@moneyfy.app", style = MaterialTheme.typography.bodySmall, color = MoneyfyTheme.colors.textSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MoneyfyTheme.colors.textMuted)
                    }

                    HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFeedbackDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Send Feedback", style = MaterialTheme.typography.titleMedium, color = MoneyfyTheme.colors.textPrimary)
                            Text("Help us improve Moneyfy with your ideas", style = MaterialTheme.typography.bodySmall, color = MoneyfyTheme.colors.textSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MoneyfyTheme.colors.textMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send Feedback", color = MoneyfyTheme.colors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("Write your feedback or bug report here...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MoneyfyTheme.colors.surfaceVariant,
                        focusedTextColor = MoneyfyTheme.colors.textPrimary,
                        unfocusedTextColor = MoneyfyTheme.colors.textPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFeedbackDialog = false
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Feedback Received", "Thank you for your feedback!")
                    },
                    enabled = feedbackText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancel", color = MoneyfyTheme.colors.textSecondary)
                }
            },
            containerColor = MoneyfyTheme.colors.card
        )
    }
}

@Composable
private fun FaqExpandableCard(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    color = MoneyfyTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Primary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MoneyfyTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
