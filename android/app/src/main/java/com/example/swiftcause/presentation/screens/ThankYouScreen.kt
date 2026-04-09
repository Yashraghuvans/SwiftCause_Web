package com.example.swiftcause.presentation.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swiftcause.R
import com.example.swiftcause.ui.theme.PrimaryGreen
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay

/**
 * Thank You Screen shown after successful donation
 * Displays QR code with magic link if available
 * Auto-dismisses after 5 seconds
 */
@Composable
fun ThankYouScreen(
    campaignTitle: String,
    amount: Long,
    currency: String,
    magicLinkToken: String?,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 30 seconds if magic link present, otherwise 5 seconds
    val dismissDelay = if (magicLinkToken != null) 30000L else 5000L
    
    LaunchedEffect(magicLinkToken) {
        delay(dismissDelay)
        onDismiss()
    }

    // Countdown timer for visual feedback
    val totalSeconds = if (magicLinkToken != null) 30 else 5
    var secondsRemaining by remember { mutableIntStateOf(totalSeconds) }
    LaunchedEffect(magicLinkToken) {
        repeat(totalSeconds) {
            delay(1000)
            secondsRemaining--
        }
    }

    // Scale animation for success icon
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Success icon with animation
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = PrimaryGreen,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )

            // Thank you message
            Text(
                text = stringResource(R.string.thank_you),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Text(
                text = stringResource(
                    R.string.donation_successful,
                    formatAmount(amount, currency),
                    campaignTitle
                ),
                fontSize = 16.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            // QR Code section (only if magic link token is available)
            if (magicLinkToken != null) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.magic_link_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = stringResource(R.string.magic_link_instructions),
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                // QR Code
                QRCodeDisplay(
                    url = buildMagicLinkUrl(magicLinkToken),
                    modifier = Modifier.size(300.dp)
                )

                Text(
                    text = stringResource(R.string.magic_link_expires),
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auto-dismiss countdown
            Text(
                text = stringResource(R.string.returning_to_campaigns, secondsRemaining),
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays a QR code for the given URL
 */
@Composable
fun QRCodeDisplay(
    url: String,
    modifier: Modifier = Modifier
) {
    val qrBitmap = remember(url) { generateQRCode(url, 1024) }

    if (qrBitmap != null) {
        Surface(
            modifier = modifier,
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        // Fallback if QR generation fails
        Surface(
            modifier = modifier,
            color = Color(0xFFF1F5F9)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.magic_link_qr_unavailable),
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Generates a QR code bitmap from URL
 */
private fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        android.util.Log.e("ThankYouScreen", "Failed to generate QR code", e)
        null
    }
}

/**
 * Builds the full magic link URL
 */
private fun buildMagicLinkUrl(token: String): String {
    return "${com.example.swiftcause.BuildConfig.MAGIC_LINK_BASE_URL}/link/$token"
}

/**
 * Formats amount with currency symbol
 */
private fun formatAmount(amountInMinorUnits: Long, currency: String): String {
    val amount = amountInMinorUnits / 100.0
    return when (currency.lowercase()) {
        "gbp" -> "£%.2f".format(amount)
        "usd" -> "$%.2f".format(amount)
        "eur" -> "€%.2f".format(amount)
        else -> "${currency.uppercase()} %.2f".format(amount)
    }
}
