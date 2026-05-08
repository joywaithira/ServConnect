package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor


// --- DATA MODELS ---
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean = false,
)

enum class NotificationType {
    BOOKING_CONFIRMED,
    REMINDER,
    EVENT_FULL,
    CERTIFICATE_READY,
    SYSTEM
}

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    val notifications = listOf(
        NotificationItem(
            "1", "Booking Confirmed",
            "Your spot for 'Teaching Kids' at Nairobi Children's Home is confirmed.",
            "2 mins ago", NotificationType.BOOKING_CONFIRMED, isRead = false
        ),
        NotificationItem(
            "2", "Activity Reminder",
            "Your beach cleanup activity starts in 2 hours. Don't forget to check in!",
            "2 hours ago", NotificationType.REMINDER, isRead = false
        ),
        NotificationItem(
            "3", "Certificate Ready",
            "Your certificate for 'Tree Planting' is now available in your portfolio.",
            "Yesterday", NotificationType.CERTIFICATE_READY, isRead = true
        ),
        NotificationItem(
            "4", "Event Full",
            "The 'Elderly Care' session on Friday is now full. Check other dates!",
            "2 days ago", NotificationType.EVENT_FULL, isRead = true
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { /* Mark all as read */ }) {
                        Text("Mark all as read", color = Color.White, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotificationsView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(notifications) { notification ->
                    NotificationRow(notification)
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun NotificationRow(notification: NotificationItem) {
    val icon: ImageVector
    val iconColor: Color

    when (notification.type) {
        NotificationType.BOOKING_CONFIRMED -> {
            icon = Icons.Default.CheckCircle
            iconColor = PrimaryColor
        }
        NotificationType.REMINDER -> {
            icon = Icons.Default.NotificationsActive
            iconColor = AccentColor
        }
        NotificationType.EVENT_FULL -> {
            icon = Icons.Default.Error
            iconColor = Color.Red
        }
        NotificationType.CERTIFICATE_READY -> {
            icon = Icons.Default.WorkspacePremium
            iconColor = SecondaryColor
        }
        NotificationType.SYSTEM -> {
            icon = Icons.Default.Info
            iconColor = Color.Gray
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.isRead) Color.Transparent else PrimaryColor.copy(alpha = 0.03f))
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                if (!notification.isRead) {
                    Box(modifier = Modifier.size(8.dp).background(AccentColor, CircleShape))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = if (notification.isRead) Color.Gray else Color.DarkGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.time,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No notifications yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("We'll notify you when something important happens", fontSize = 14.sp, color = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview(){

    NotificationsScreen {}


}
