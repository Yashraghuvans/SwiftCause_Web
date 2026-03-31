package com.example.swiftcause.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swiftcause.R
import com.example.swiftcause.domain.models.Campaign
import com.example.swiftcause.ui.theme.PrimaryGreen
import com.example.swiftcause.ui.theme.WarmWhite
import com.example.swiftcause.utils.CurrencyFormatter
import kotlinx.coroutines.delay

@Composable
fun CampaignDetailsScreen(
    campaign: Campaign,
    onBackClick: () -> Unit,
    onDonateClick: (amount: Long, isRecurring: Boolean, interval: String?) -> Unit
) {
    var selectedAmount by remember { mutableLongStateOf(0L) }
    var customAmount by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedInterval by remember { mutableStateOf("monthly") }
    var currentImageIndex by remember { mutableIntStateOf(0) }
    
    val images = campaign.getAllImages()
    val scrollState = rememberScrollState()
    
    // Auto-rotate carousel
    LaunchedEffect(images.size) {
        if (images.size > 1) {
            while (true) {
                delay(5000)
                currentImageIndex = (currentImageIndex + 1) % images.size
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmWhite)
                .verticalScroll(scrollState)
                .padding(bottom = 320.dp) // Space for fixed bottom panel
        ) {
            // Header with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back_button),
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.back),
                        color = PrimaryGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Image Carousel
            ImageCarousel(
                images = images,
                currentIndex = currentImageIndex,
                onIndexChange = { currentImageIndex = it },
                campaignTitle = campaign.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Campaign Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Title
                    Text(
                        text = campaign.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        lineHeight = 31.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Short Description
                    if (campaign.shortDescription.isNotEmpty()) {
                        Text(
                            text = campaign.shortDescription,
                            fontSize = 15.sp,
                            color = Color(0xFF334155),
                            lineHeight = 23.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    
                    // Progress Section
                    ProgressSection(campaign = campaign)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Long Description Card
            if (campaign.longDescription.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.about_campaign),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = campaign.longDescription,
                            fontSize = 15.sp,
                            color = Color(0xFF334155),
                            lineHeight = 23.sp
                        )
                    }
                }
            }
        }
        
        // Fixed Bottom Donation Panel
        DonationPanel(
            campaign = campaign,
            selectedAmount = selectedAmount,
            customAmount = customAmount,
            isRecurring = isRecurring,
            selectedInterval = selectedInterval,
            onAmountSelected = { 
                selectedAmount = it
                customAmount = ""
            },
            onCustomAmountChanged = {
                customAmount = it
                selectedAmount = 0L
            },
            onRecurringToggle = { isRecurring = it },
            onIntervalSelected = { selectedInterval = it },
            onDonateClick = {
                val amount = if (selectedAmount > 0) selectedAmount else customAmount.toLongOrNull() ?: 0L
                if (amount > 0) {
                    onDonateClick(amount, isRecurring, if (isRecurring) selectedInterval else null)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ImageCarousel(
    images: List<String>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    campaignTitle: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box {
                AsyncImage(
                    model = images.getOrNull(currentIndex) ?: "",
                    contentDescription = stringResource(R.string.content_description_campaign_image, campaignTitle),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                            )
                        )
                )
                
                // Navigation arrows (only if multiple images)
                if (images.size > 1) {
                    // Left arrow
                    IconButton(
                        onClick = { 
                            onIndexChange((currentIndex - 1 + images.size) % images.size)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.content_description_previous_image),
                            tint = PrimaryGreen
                        )
                    }
                    
                    // Right arrow
                    IconButton(
                        onClick = { 
                            onIndexChange((currentIndex + 1) % images.size)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.content_description_next_image),
                            tint = PrimaryGreen
                        )
                    }
                    
                    // Dot indicators
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentIndex) PrimaryGreen 
                                        else Color.White.copy(alpha = 0.6f)
                                    )
                                    .clickable { onIndexChange(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressSection(
    campaign: Campaign,
    modifier: Modifier = Modifier
) {
    val progress = campaign.getProgressPercentage()
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        label = "progress"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.community_support),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Text(
                text = "${progress.toInt()}%",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryGreen
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryGreen)
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = stringResource(
                R.string.raised_of_goal,
                CurrencyFormatter.formatCurrency(campaign.raised, campaign.currency),
                CurrencyFormatter.formatCurrencyFromMajor(campaign.goal, campaign.currency)
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )
    }
}

@Composable
private fun DonationPanel(
    campaign: Campaign,
    selectedAmount: Long,
    customAmount: String,
    isRecurring: Boolean,
    selectedInterval: String,
    onAmountSelected: (Long) -> Unit,
    onCustomAmountChanged: (String) -> Unit,
    onRecurringToggle: (Boolean) -> Unit,
    onIntervalSelected: (String) -> Unit,
    onDonateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amounts = campaign.predefinedAmounts.ifEmpty { listOf(10L, 25L, 50L, 100L, 250L, 500L) }
    val isDonateEnabled = selectedAmount > 0 || (customAmount.toLongOrNull() ?: 0) > 0
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                spotColor = Color(0xFF0F172A).copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = WarmWhite,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Amount label
            Text(
                text = stringResource(R.string.choose_amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount buttons grid (2 rows of 3)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(amounts.take(6)) { _, amount ->
                    AmountButton(
                        amount = amount,
                        currency = campaign.currency,
                        isSelected = selectedAmount == amount,
                        onClick = { onAmountSelected(amount) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Custom amount input
            OutlinedTextField(
                value = customAmount,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() }) {
                        onCustomAmountChanged(it)
                    }
                },
                placeholder = { 
                    Text(
                        text = stringResource(R.string.custom_amount),
                        color = Color(0xFF9CA3AF)
                    )
                },
                prefix = {
                    Text(
                        text = CurrencyFormatter.getCurrencySymbol(campaign.currency),
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = PrimaryGreen,
                    unfocusedContainerColor = WarmWhite,
                    focusedContainerColor = WarmWhite
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            // Recurring toggle (if enabled)
            if (campaign.enableRecurring) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.make_recurring),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = onRecurringToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                
                // Interval selector
                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IntervalButton(
                            text = stringResource(R.string.monthly),
                            isSelected = selectedInterval == "monthly",
                            onClick = { onIntervalSelected("monthly") },
                            modifier = Modifier.weight(1f)
                        )
                        IntervalButton(
                            text = stringResource(R.string.quarterly),
                            isSelected = selectedInterval == "quarterly",
                            onClick = { onIntervalSelected("quarterly") },
                            modifier = Modifier.weight(1f)
                        )
                        IntervalButton(
                            text = stringResource(R.string.yearly),
                            isSelected = selectedInterval == "yearly",
                            onClick = { onIntervalSelected("yearly") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Donate button
            Button(
                onClick = onDonateClick,
                enabled = isDonateEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.donate),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Secure payment text
            Text(
                text = stringResource(R.string.secure_payment),
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AmountButton(
    amount: Long,
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGreen else WarmWhite,
        label = "bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else PrimaryGreen,
        label = "text"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .widthIn(min = 80.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = CurrencyFormatter.formatCurrencyFromMajor(amount, currency),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun IntervalButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGreen else Color.White,
        label = "bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF334155),
        label = "text"
    )
    
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
