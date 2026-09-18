package com.moneyfy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyfy.ui.theme.*
import kotlinx.coroutines.launch

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authRepository = (application as MoneyfyApplication).authRepository

        enableEdgeToEdge()
        setContent {
            MoneyfyTheme {
                ForgotPasswordScreen(
                    onNavigateBack = { finish() },
                    authRepository = authRepository
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authRepository: com.moneyfy.firebase.AuthRepository
) {
    var username by remember { mutableStateOf("") }
    var fetchedQuestion by remember { mutableStateOf<String?>(null) }
    var answer by remember { mutableStateOf("") }
    var isAnswerVerified by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var isSuccessMessage by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 0.dp, top = 8.dp, bottom = 16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            
            Text(
                text = "Forgot Password",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recover your account password",
                color = TextSecondary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (!isAnswerVerified && !isSuccessMessage) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        fetchedQuestion = null 
                    },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (fetchedQuestion == null) {
                    Button(
                        onClick = {
                            if (username.isEmpty()) {
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery", "Enter username")
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                authRepository.getSecurityQuestion(username.trim())
                                    .onSuccess { question ->
                                        fetchedQuestion = question
                                        isLoading = false
                                    }
                                    .onFailure {
                                        isLoading = false
                                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery Error", it.message ?: "User not found")
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Fetch Security Question", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Security Question", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fetchedQuestion!!, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text("Answer") },
                        leadingIcon = { Icon(Icons.Default.QuestionMark, contentDescription = null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = DarkSurfaceVariant,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (answer.isEmpty()) {
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery", "Enter answer")
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                authRepository.verifySecurityAnswer(username.trim(), answer.trim())
                                    .onSuccess {
                                        isAnswerVerified = true
                                        isLoading = false
                                    }
                                    .onFailure {
                                        isLoading = false
                                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery Error", it.message ?: "Incorrect answer")
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Verify Answer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (isAnswerVerified && !isSuccessMessage) {
                Text(
                    text = "Answer verified! Enter a new password.",
                    color = IncomeGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (newPassword.length < 6) {
                            com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery", "Password must be at least 6 characters")
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            authRepository.resetPassword(username.trim(), newPassword.trim())
                                .onSuccess {
                                    isSuccessMessage = true
                                    isLoading = false
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery", "Password Reset Email Sent")
                                }
                                .onFailure {
                                    isLoading = false
                                    com.moneyfy.util.AppNotificationManager.showNotification(context, "Password Recovery Error", it.message ?: "Error resetting password")
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Reset Link", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Success!",
                            color = IncomeGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A reset email has been sent to your registered address to confirm the change.",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Return to Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
