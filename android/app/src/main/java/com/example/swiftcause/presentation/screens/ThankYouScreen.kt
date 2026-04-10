package com.example.swiftcause.presentation.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swiftcause.BuildConfig
import com.example.swiftcause.R
import com.example.swiftcause.ui.theme.PrimaryGreen
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay

private const val DISMISS_DELAY_WITH_QR = 30000L

@Composable
fun ThankYouScreen(
    magicLinkToken: String,
    onDismiss: () -> Unit
) {
    val totalSeconds = (DISMISS_DELAY_WITH_QR / 1000).toInt()
    var secondsRemaining by remember { mutableIntStateOf(totalSeconds) }

    val animatedProgress by animateFloatAsState(
        targetValue = secondsRemaining.toFloat() / totalSeconds,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "countdownProgress"
    )

    LaunchedEffect(magicLinkToken) {
        secondsRemaining = totalSeconds
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        onDismiss()
    }

    var showCheck by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showCheck = true
    }

    val scale by animateFloatAsState(
        targetValue = if (showCheck) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4)) // Light green background
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Checkmark
        Box(
            modifier = Modifier
                .size(100.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Thank You Message
        Text(
            text = stringResource(R.string.thank_you_for_your_donation),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF14532D), // Darker green
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // QR Code
        val qrBitmap = remember(magicLinkToken) {
            generateQrCode(magicLinkToken)
        }
        qrBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.qr_code_content_description),
                modifier = Modifier.size(220.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructional Text
        Text(
            text = stringResource(R.string.scan_qr_for_gift_aid),
            fontSize = 16.sp,
            color = Color(0xFF166534), // Medium green
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Countdown Timer
        CountdownCircle(
            secondsRemaining = secondsRemaining,
            progress = animatedProgress
        )
    }
}

@Composable
private fun CountdownCircle(secondsRemaining: Int, progress: Float) {
    val strokeWidth = with(LocalDensity.current) { 6.dp.toPx() }
    val primaryColor = PrimaryGreen
    val trackColor = Color(0xFFBBF7D0) // Light green track

    Box(
        modifier = Modifier.size(70.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = secondsRemaining.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
        )
    }
}

private fun generateQrCode(token: String?): Bitmap? {
    if (token.isNullOrBlank()) return null
    val fullUrl = "${BuildConfig.MAGIC_LINK_BASE_URL}/claim/$token"
    return try {
        val writer = QRCodeWriter()
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = writer.encode(fullUrl, BarcodeFormat.QR_CODE, 512, 512, hints)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
