package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

// --- DATA MODELS ---
data class ActivityHistoryItem(
    val id: String,
    val title: String,
    val organization: String,
    val date: String,
    val status: String, // "Completed", "Cancelled"
    val type: String // "Volunteer" or "Community Service"
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    onNavigateBack: () -> Unit,
    onOrgClick: (String) -> Unit
) {
    val historyItems = listOf(
        ActivityHistoryItem("1", "Food Distribution", "Hope Kitchen", "May 01, 2024", "Completed", "Community Service"),
        ActivityHistoryItem("2", "Beach Cleanup", "Eco Warriors", "April 20, 2024", "Completed", "Volunteer"),
        ActivityHistoryItem("3", "Animal Shelter Help", "Paws & Claws", "April 10, 2024", "Cancelled", "Volunteer"),
        ActivityHistoryItem("4", "Library Organizing", "City Library", "March 15, 2024", "Completed", "Community Service")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(historyItems) { item ->
                HistoryCard(item, onClick = { onOrgClick(item.id) }) // Using item.id as mock orgId
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun HistoryCard(item: ActivityHistoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.status == "Completed") PrimaryColor.copy(alpha = 0.1f)
                        else Color.Red.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.status == "Completed") Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (item.status == "Completed") PrimaryColor else Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1D1D)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SecondaryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = item.type,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }

                Text(
                    text = item.organization,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityHistoryScreenPreview(){
    ActivityHistoryScreen(onNavigateBack = {}, onOrgClick = {})
}



