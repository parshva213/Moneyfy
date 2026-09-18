package com.moneyfy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moneyfy.firebase.FirebaseProvider
import com.moneyfy.ui.screens.MainScreen
import com.moneyfy.ui.theme.MoneyfyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseProvider.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            MoneyfyTheme {
                MainScreen()
            }
        }
    }
}

