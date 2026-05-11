package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.theme.*

// --- DATA MODELS ---
data class OrgStats(
    val activeOpportunities: Int,
    val totalVolunteers: Int,
    val totalHours: Int
)

data class RecentActivity(
    val id: String,
    val volunteerName: String,
    val activityTitle: String,
    val time: String,
    val type: String // "Booking", "Check-in", "Check-out"
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgDashboardScreen(
    isVerified: Boolean,
    onNavigateToPostOpportunity: () -> Unit,
    onNavigateToManageBookings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToMyOpportunities: () -> Unit
) {
    val stats = OrgStats(activeOpportunities = 5, totalVolunteers = 128, totalHours = 450)
    val recentActivities = listOf(
        RecentActivity("1", "Jane Doe", "Teaching Kids", "10 mins ago", "Check-in"),
        RecentActivity("2", "John Smith", "Beach Cleanup", "1 hour ago", "Booking"),
        RecentActivity("3", "Alice W.", "Elderly Care", "3 hours ago", "Check-out")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ServConnect", fontWeight = FontWeight.Bold, color = White) },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = White)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        floatingActionButton = {
            if (isVerified) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToPostOpportunity,
                    containerColor = AccentColor,
                    contentColor = White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Post Opportunity") }
                )
            }
        },
        containerColor = OffWhite
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Verification Status Banner
            item { VerificationBanner(isVerified) }

            // 2. Stats Overview
            item { StatsOverview(stats, onNavigateToMyOpportunities) }

            // 3. Quick Actions Grid
            item { QuickActionsSection(onNavigateToManageBookings, onNavigateToMessages) }

            // 4. Recent Activity Title
            item {
                Text(
                    text = "Recent Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // 5. Recent Activity List
            items(recentActivities) { activity ->
                ActivityItem(activity)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun VerificationBanner(isVerified: Boolean) {
    val bgColor = if (isVerified) PrimaryColor.copy(alpha = 0.1f) else AccentColor.copy(alpha = 0.1f)
    val textColor = if (isVerified) PrimaryColor else AccentColor
    val icon = if (isVerified) Icons.Default.Verified else Icons.Default.Pending
    val text = if (isVerified) "Verified Organization" else "Pending Verification"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text, fontWeight = FontWeight.Bold, color = textColor)
                if (!isVerified) {
                    Text(
                        "You can't post opportunities until verified.",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsOverview(stats: OrgStats, onNavigateToMyOpportunities: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Active",
            value = stats.activeOpportunities.toString(),
            icon = Icons.Default.ListAlt,
            onClick = onNavigateToMyOpportunities
        )
        StatCard(modifier = Modifier.weight(1f), label = "Volunteers", value = stats.totalVolunteers.toString(), icon = Icons.Default.People)
        StatCard(modifier = Modifier.weight(1f), label = "Hours", value = stats.totalHours.toString(), icon = Icons.Default.Timer)
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkGray)
            Text(label, fontSize = 12.sp, color = Gray)
        }
    }
}

@Composable
fun QuickActionsSection(onManageBookings: () -> Unit, onMessages: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(
            modifier = Modifier.weight(1f),
            title = "Manage Bookings",
            icon = Icons.Default.EventAvailable,
            color = SecondaryColor,
            onClick = onManageBookings
        )
        ActionCard(
            modifier = Modifier.weight(1f),
            title = "Messages",
            icon = Icons.Default.Chat,
            color = PrimaryColor.copy(alpha = 0.2f),
            onClick = onMessages
        )
    }
}

@Composable
fun ActionCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryColor)
        }
    }
}

@Composable
fun ActivityItem(activity: RecentActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(SecondaryColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(activity.type) {
                        "Check-in" -> Icons.Default.Login
                        "Check-out" -> Icons.Default.Logout
                        else -> Icons.Default.Bookmark
                    },
                    contentDescription = null,
                    tint = PrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${activity.volunteerName} ${activity.type.lowercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(activity.activityTitle, fontSize = 12.sp, color = Gray)
            }
            Text(activity.time, fontSize = 11.sp, color = Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrgDashboardScreenPreview() {
    OrgDashboardScreen(
        isVerified = true,
        onNavigateToPostOpportunity = {},
        onNavigateToManageBookings = {},
        onNavigateToProfile = {},
        onNavigateToMessages = {},
        onNavigateToNotifications = {},
        onNavigateToMyOpportunities = {}
    )
}