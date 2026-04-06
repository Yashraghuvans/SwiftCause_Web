package com.example.swiftcause

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.swiftcause.domain.models.Campaign
import com.example.swiftcause.presentation.screens.CampaignListScreen
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
                        // Sample campaigns data for testing/development
                        // TODO: Replace with real API data from CampaignRepository
                        val sampleCampaigns = listOf(
                            Campaign(
                                id = "1",
                                title = "Clean Water for Rural Communities",
                                coverImageUrl = "https://images.unsplash.com/photo-1541544181051-e46607bc22a4?w=800",
                                raised = 45000, // $450.00
                                goal = 1000, // $1,000
                                predefinedAmounts = listOf(10, 25, 50),
                                currency = "USD"
                            ),
                            Campaign(
                                id = "2",
                                title = "Education Programs for Underprivileged Children",
                                coverImageUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=800",
                                raised = 78500, // $785.00
                                goal = 2000, // $2,000
                                predefinedAmounts = listOf(15, 30, 75),
                                currency = "USD"
                            ),
                            Campaign(
                                id = "3",
                                title = "Emergency Medical Supplies Fund",
                                coverImageUrl = "https://images.unsplash.com/photo-1584515933487-779824d29309?w=800",
                                raised = 125000, // $1,250.00
                                goal = 5000, // $5,000
                                predefinedAmounts = listOf(20, 50, 100),
                                currency = "USD"
                            ),
                            Campaign(
                                id = "4",
                                title = "Reforestation & Climate Action Initiative",
                                coverImageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=800",
                                raised = 32000, // $320.00
                                goal = 1500, // $1,500
                                predefinedAmounts = listOf(5, 15, 30),
                                currency = "USD"
                            ),
                            Campaign(
                                id = "5",
                                title = "Support for Homeless Families",
                                coverImageUrl = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=800",
                                raised = 55000, // $550.00
                                goal = 3000, // $3,000
                                predefinedAmounts = listOf(10, 25, 50),
                                currency = "USD"
                            ),
                            Campaign(
                                id = "6",
                                title = "Women's Empowerment & Job Training",
                                coverImageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=800",
                                raised = 89000, // $890.00
                                goal = 2500, // $2,500
                                predefinedAmounts = listOf(20, 40, 100),
                                currency = "USD"
                            )
                        )
                        
                        CampaignListScreen(
                            campaigns = sampleCampaigns,
                            isLoading = false,
                            onCampaignClick = { campaign ->
                                // Handle campaign click
                            },
                            onAmountClick = { campaign, amount ->
                                // Handle amount selection
                            },
                            onDonateClick = { campaign ->
                                // Handle donate button click
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
