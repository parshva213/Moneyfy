package com.moneyfy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyfy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Moneyfy", color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.Bold) },
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
            // App Branding Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Moneyfy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MoneyfyTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Version 1.0.0 (SIH 2026 Edition)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Smart Personal Finance & Multi-Account Expense Manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyfyTheme.colors.textSecondary
                    )
                }
            }

            // Key Highlights Section
            Text(
                text = "Key Highlights",
                style = MaterialTheme.typography.titleMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FeatureRow(icon = Icons.Default.CheckCircle, title = "Offline-First Persistence", desc = "Data is synchronized with Firebase Realtime Database while keeping local offline cache.")
                    HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)
                    FeatureRow(icon = Icons.Default.Security, title = "Role & Account Security", desc = "Secure user authentication with custom security question fallback recovery.")
                    HorizontalDivider(color = MoneyfyTheme.colors.surfaceVariant)
                    FeatureRow(icon = Icons.Default.Star, title = "Modern Jetpack Compose UI", desc = "Fluid dual light/dark theme design system with smooth micro-interactions.")
                }
            }

            // Tech Stack Section
            Text(
                text = "Technology Stack",
                style = MaterialTheme.typography.titleMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TechRow(title = "Language", detail = "Kotlin 2.0 / Java 21 JBR")
                    TechRow(title = "UI Framework", detail = "Android Jetpack Compose & Material 3")
                    TechRow(title = "Backend & Database", detail = "Firebase Authentication & Realtime DB")
                    TechRow(title = "Architecture", detail = "MVVM + Coroutines StateFlow")
                }
            }

            // Credits Section
            Text(
                text = "Credits & Support",
                style = MaterialTheme.typography.titleMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyfyTheme.colors.card)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Designed and Developed for SIH 2026", color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "© 2026 Moneyfy Team. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoneyfyTheme.colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MoneyfyTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun TechRow(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MoneyfyTheme.colors.textSecondary)
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = MoneyfyTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold)
    }
}
