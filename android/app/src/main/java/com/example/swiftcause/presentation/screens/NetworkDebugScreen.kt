package com.example.swiftcause.presentation.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swiftcause.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

@Composable
fun NetworkDebugScreen(
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var networkType by remember { mutableStateOf("Checking...") }
    var isNetworkAvailable by remember { mutableStateOf(false) }
    var dnsTest by remember { mutableStateOf("Not tested") }
    var httpTest by remember { mutableStateOf("Not tested") }
    var cloudFunctionTest by remember { mutableStateOf("Not tested") }
    var isTesting by remember { mutableStateOf(false) }
    
    fun runTests() {
        scope.launch {
            isTesting = true
            
            // Test 1: Network Type
            networkType = NetworkUtils.getNetworkType(context)
            
            // Test 2: Network Availability
            isNetworkAvailable = NetworkUtils.isNetworkAvailable(context)
            
            // Test 3: DNS Resolution
            dnsTest = withContext(Dispatchers.IO) {
                try {
                    InetAddress.getByName("google.com")
                    "✅ Success"
                } catch (e: Exception) {
                    "❌ Failed: ${e.message}"
                }
            }
            
            // Test 4: HTTP Request
            httpTest = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://www.google.com")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.connect()
                    val code = connection.responseCode
                    connection.disconnect()
                    "✅ Success (HTTP $code)"
                } catch (e: Exception) {
                    "❌ Failed: ${e.message}"
                }
            }
            
            // Test 5: Cloud Function
            cloudFunctionTest = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://us-central1-swiftcause-app.cloudfunctions.net/kioskLogin")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 10000
                    connection.requestMethod = "GET"
                    connection.connect()
                    val code = connection.responseCode
                    connection.disconnect()
                    "✅ Reachable (HTTP $code)"
                } catch (e: Exception) {
                    "❌ Failed: ${e.message}"
                }
            }
            
            isTesting = false
        }
    }
    
    LaunchedEffect(Unit) {
        runTests()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Network Diagnostics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Use this screen to diagnose network connectivity issues",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Test Results
            TestResultCard("Network Type", networkType)
            TestResultCard("Network Available", if (isNetworkAvailable) "✅ Yes" else "❌ No")
            TestResultCard("DNS Resolution (google.com)", dnsTest)
            TestResultCard("HTTPS Request (google.com)", httpTest)
            TestResultCard("Cloud Function", cloudFunctionTest)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { runTests() },
                    enabled = !isTesting,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retest")
                }
                
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Troubleshooting Tips
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Troubleshooting Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Restart emulator with: emulator -avd <name> -dns-server 8.8.8.8")
                    Text("• Check AVD Manager → Edit → Network → Set to NAT")
                    Text("• Open Chrome in emulator and test google.com")
                    Text("• Try 'adb shell ping 8.8.8.8' in terminal")
                    Text("• Use a physical device instead of emulator")
                }
            }
            
            if (isTesting) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun TestResultCard(label: String, result: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = when {
                    result.contains("✅") -> MaterialTheme.colorScheme.primary
                    result.contains("❌") -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
