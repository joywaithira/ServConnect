package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// --- DATA MODELS ---
data class OrgOpportunity(
    val id: String,
    val title: String,
    val date: String,
    val slotsFilled: Int,
    val totalSlots: Int,
    val status: String // "Open", "Full", "Completed"
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOpportunitiesScreen(
    onNavigateBack: () -> Unit,
    onOpportunityClick: (String) -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }
    val userId = auth.currentUser?.uid

    var opportunities by remember { mutableStateOf<List<OrgOpportunity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val query = database.getReference("opportunities")
                .orderByChild("orgId").equalTo(userId)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<OrgOpportunity>()
                    for (child in snapshot.children) {
                        val id = child.child("id").value?.toString() ?: ""
                        val title = child.child("title").value?.toString() ?: ""
                        val date = child.child("date").value?.toString() ?: ""
                        val totalSlots = (child.child("totalSlots").value as? Long)?.toInt() ?: 0
                        val spotsLeft = (child.child("spotsLeft").value as? Long)?.toInt() ?: 0
                        val slotsFilled = totalSlots - spotsLeft
                        val status = child.child("status").value?.toString() ?: "Open"
                        
                        list.add(OrgOpportunity(id, title, date, slotsFilled, totalSlots, status))
                    }
                    opportunities = list
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            }
            query.addValueEventListener(listener)
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Opportunities", fontWeight = FontWeight.Bold, color = Color.White) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else if (opportunities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No opportunities posted yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(opportunities) { opportunity ->
                    OrgOpportunityCard(opportunity, onClick = { onOpportunityClick(opportunity.id) })
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun OrgOpportunityCard(opportunity: OrgOpportunity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = opportunity.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                StatusBadge(opportunity.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(opportunity.date, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar for Slots
            val progress = opportunity.slotsFilled.toFloat() / opportunity.totalSlots.toFloat()
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Slots Filled", fontSize = 12.sp, color = Color.Gray)
                    Text("${opportunity.slotsFilled}/${opportunity.totalSlots}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (progress >= 1f) Color.Red else PrimaryColor,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "Open" -> PrimaryColor
        "Full" -> Color.Red
        "Completed" -> Color.Gray
        else -> PrimaryColor
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MyOpportunitiesScreenPreview() {
    MyOpportunitiesScreen(onNavigateBack = {}, onOpportunityClick = {})
}