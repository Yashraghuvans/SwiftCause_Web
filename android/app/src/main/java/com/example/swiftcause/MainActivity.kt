package com.example.swiftcause

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.swiftcause.presentation.screens.KioskLoginScreen
import com.example.swiftcause.ui.theme.SwiftCauseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwiftCauseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var isLoggedIn by remember { mutableStateOf(false) }
                    
                    if (!isLoggedIn) {
                        KioskLoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                            }
                        )
                    } else {
                        // Placeholder for logged-in screen
                        LoggedInScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun LoggedInScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Kiosk is now logged in! 🎉\n\nCampaign list will be shown here.",
        modifier = modifier
    )
}
