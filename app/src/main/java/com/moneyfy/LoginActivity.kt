@file:Suppress("DEPRECATION")
package com.moneyfy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.moneyfy.firebase.FirebaseProvider
import com.moneyfy.ui.theme.*
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = application as MoneyfyApplication
        val authRepository = app.authRepository

        if (FirebaseProvider.auth.currentUser != null) {
            navigateToMain()
            return
        }

        enableEdgeToEdge()
        setContent {
            MoneyfyTheme {
                LoginScreen(
                    onLoginSuccess = { navigateToMain() },
                    onNavigateToRegister = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onNavigateToForgotPassword = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    },
                    authRepository = authRepository
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    authRepository: com.moneyfy.firebase.AuthRepository
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                isGoogleLoading = true
                scope.launch {
                    val idToken = account.idToken
                    val resultAuth = if (idToken != null && idToken.isNotBlank()) {
                        authRepository.loginWithGoogleToken(idToken)
                    } else {
                        val email = account.email ?: ""
                        val name = account.displayName ?: email.substringBefore("@")
                        val googleId = account.id ?: "google_${System.currentTimeMillis()}"
                        authRepository.loginWithGoogleFallback(email, name, googleId)
                    }

                    resultAuth.onSuccess {
                        isGoogleLoading = false
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login", "Google Sign-In Successful")
                        onLoginSuccess()
                    }.onFailure { err ->
                        isGoogleLoading = false
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login Error", err.message ?: "Google Sign-In Failed")
                    }
                }
            }
        } catch (e: Exception) {
            isGoogleLoading = false
            com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login Error", "Google Sign-In cancelled or failed: ${e.localizedMessage}")
        }
    }

    fun launchGoogleSignIn() {
        try {
            val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
            
            try {
                val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (webClientIdResId != 0) {
                    val webClientId = context.getString(webClientIdResId)
                    if (webClientId.isNotBlank()) {
                        gsoBuilder.requestIdToken(webClientId)
                    }
                }
            } catch (_: Exception) {}

            val gso = gsoBuilder.build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login Error", "Could not launch Google Sign-In: ${e.message}")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue to Moneyfy",
                color = TextSecondary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username or Email") },
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

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (username.isEmpty() || password.isEmpty()) {
                        com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login", "Please enter all fields")
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        authRepository.login(username.trim(), password.trim())
                            .onSuccess {
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login", "Login Successful")
                                onLoginSuccess()
                            }
                            .onFailure {
                                isLoading = false
                                com.moneyfy.util.AppNotificationManager.showNotification(context, "Moneyfy Login Error", it.message ?: "Login failed")
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isLoading && !isGoogleLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { launchGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isLoading && !isGoogleLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkCard,
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Google Sign In", tint = Primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Login with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = TextSecondary)
                Text(
                    text = "Register",
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

