package com.maryjoy.servconnect.ui.screens.volunteer

import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCheckInScreen(
    onNavigateBack: () -> Unit,
    onCheckInSuccess: () -> Unit
) {
    var isCheckedIn by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var showScanner by remember { mutableStateOf(true) }

    // Timer logic when checked in
    LaunchedEffect(isCheckedIn) {
        if (isCheckedIn) {
            while (true) {
                delay(1000)
                elapsedTime += 1
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Check-in", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isCheckedIn) {
                // --- SCANNER VIEW ---
                ScannerSection(
                    onScanSuccess = {
                        isCheckedIn = true
                        showScanner = false
                        onCheckInSuccess()
                    }
                )
            } else {
                // --- ACTIVE TIMER VIEW ---
                ActiveTimerSection(
                    elapsedTime = elapsedTime,
                    onCheckOut = {
                        isCheckedIn = false
                        elapsedTime = 0L
                        // Navigate to success or portfolio
                    }
                )
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun ScannerSection(onScanSuccess: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Scan QR Code",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )
        Text(
            text = "Scan the QR code at the facility to start your session",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Mock Scanner Box
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.05f))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, you'd use CameraX or a QR library here
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = PrimaryColor.copy(alpha = 0.5f)
            )

            // Animated scanning line
            ScanningLine()
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onScanSuccess,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simulate Scan Success", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ActiveTimerSection(elapsedTime: Long, onCheckOut: () -> Unit) {
    val hours = TimeUnit.SECONDS.toHours(elapsedTime)
    val minutes = TimeUnit.SECONDS.toMinutes(elapsedTime) % 60
    val seconds = elapsedTime % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(SecondaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = timeString,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryColor
                )
                Text(
                    text = "Active Session",
                    fontSize = 12.sp,
                    color = PrimaryColor.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Location", fontSize = 12.sp, color = Color.Gray)
                    Text("Nairobi Children's Home", fontWeight = FontWeight.Bold, color = PrimaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCheckOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Check Out", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Checking out will stop the timer and generate your certificate proof.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun ScanningLine() {
    // Simple animation for the scanning line
    var linePosition by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            linePosition = 0f
            delay(10)
            // This is a simplified mock animation
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(2.dp)
            .background(AccentColor)
    )
}

@Preview(showBackground = true)
@Composable
fun QrCheckInScreenPreview(){
    QrCheckInScreen(
        onNavigateBack = {},
        onCheckInSuccess = {}
    )
}
