package com.example.swiftcause.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swiftcause.R
import com.example.swiftcause.domain.models.Campaign
import com.example.swiftcause.presentation.components.CampaignCard
import com.example.swiftcause.ui.theme.BackgroundGray
import com.example.swiftcause.ui.theme.TextPrimary

@Composable
fun CampaignListScreen(
    campaigns: List<Campaign>,
    isLoading: Boolean = false,
    onCampaignClick: (Campaign) -> Unit = {},
    onAmountClick: (Campaign, Long) -> Unit = { _, _ -> },
    onDonateClick: (Campaign) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = BackgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Loading State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                campaigns.isEmpty() -> {
                    // Empty State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.campaign_list_empty),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
                
                else -> {
                    // Campaign Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 380.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        items(campaigns) { campaign ->
                            CampaignCard(
                                campaign = campaign,
                                onCardClick = { onCampaignClick(campaign) },
                                onAmountClick = { amount -> onAmountClick(campaign, amount) },
                                onDonateClick = { onDonateClick(campaign) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
