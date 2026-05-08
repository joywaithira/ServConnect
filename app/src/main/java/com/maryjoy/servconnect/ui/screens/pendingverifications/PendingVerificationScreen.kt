package com.maryjoy.servconnect.ui.screens.pendingverifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

@Composable
fun PendingVerificationScreen(
    onLogout: () -> Unit,
    onContactSupport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(24.dp),
            color = SecondaryColor.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Verification Pending",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Thank you for registering with ServConnect! Our admin team is currently reviewing your organization's details.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "This process usually takes 24-48 hours. You will receive an email once your account is verified.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContactSupport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Icon(Icons.Default.SupportAgent, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Support", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingVerificationScreenPreview() {
    PendingVerificationScreen(
        onLogout = {},
        onContactSupport = {}
    )
}