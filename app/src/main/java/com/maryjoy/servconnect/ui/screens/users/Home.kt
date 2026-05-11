package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor


// --- DATA MODELS ---
data class Opportunity(
    val id: String,
    val title: String,
    val organization: String,
    val location: String,
    val type: String, // "Volunteer" or "Community Service"
    val imageUrl: String,
    val status: String = "Open"
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerHomeScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToQrCheckIn: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }
    val userId = auth.currentUser?.uid

    var userName by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var recommendedOpportunities by remember { mutableStateOf<List<Opportunity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId != null) {
            database.getReference("volunteers").child(userId).get().addOnSuccessListener { snapshot ->
                userName = snapshot.child("fullName").value?.toString()?.split(" ")?.firstOrNull() ?: "User"
                profileImageUrl = snapshot.child("profileImageUrl").value?.toString()
            }
        }

        // Fetch all opportunities for the home screen
        database.getReference("opportunities").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<Opportunity>()
            for (child in snapshot.children) {
                list.add(Opportunity(
                    id = child.child("id").value?.toString() ?: "",
                    title = child.child("title").value?.toString() ?: "",
                    organization = child.child("organization").value?.toString() ?: "",
                    location = child.child("location").value?.toString() ?: "",
                    type = child.child("type").value?.toString() ?: "Volunteer",
                    imageUrl = child.child("imageUrl").value?.toString() ?: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=400"
                ))
            }
            recommendedOpportunities = list
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onProfileClick = onNavigateToProfile,
                onNotificationClick = onNavigateToNotifications,
                profileImageUrl = profileImageUrl
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Welcome Header
                item { WelcomeHeader(userName = userName) }

                // 2. Search Bar
                item { SearchBarSection() }

                // 3. Quick Action: QR Check-in Card
                item { QrQuickActionCard(onClick = onNavigateToQrCheckIn) }

                // 4. Categories Row
                item { CategorySection() }

                // 5. Recommended Section
                item {
                    SectionTitle(title = "Recommended for You", onSeeAllClick = onNavigateToExplore)
                    if (recommendedOpportunities.isEmpty()) {
                        Text("No opportunities found.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            items(recommendedOpportunities) { opportunity ->
                                OpportunityCard(opportunity, onClick = { onNavigateToDetails(opportunity.id) })
                            }
                        }
                    }
                }

                // 6. Nearby Section
                item {
                    SectionTitle(title = "Nearby Opportunities", onSeeAllClick = onNavigateToExplore)
                }
                items(recommendedOpportunities.reversed()) { opportunity ->
                    NearbyOpportunityItem(opportunity, onClick = { onNavigateToDetails(opportunity.id) })
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onProfileClick: () -> Unit, onNotificationClick: () -> Unit, profileImageUrl: String?) {
    CenterAlignedTopAppBar(
        title = {
            Text("ServConnect", fontWeight = FontWeight.ExtraBold, color = PrimaryColor, fontSize = 20.sp)
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = PrimaryColor)
            }
            IconButton(onClick = onProfileClick) {
                AsyncImage(
                    model = profileImageUrl ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun WelcomeHeader(userName: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hello, $userName! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
        Text(text = "Find a way to give back today", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
private fun SearchBarSection() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search opportunities, locations...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryColor) },
        trailingIcon = { Icon(Icons.Default.Tune, contentDescription = "Filter", tint = PrimaryColor) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

@Composable
fun QrQuickActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(SecondaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("QR Check-in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Arrived at a facility? Scan now", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CategorySection() {
    val categories = listOf("All", "Kids", "Elderly", "Environment", "Health")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == "All",
                onClick = {},
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryColor,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
        Text(
            text = "See All",
            fontSize = 14.sp,
            color = AccentColor,
            modifier = Modifier.clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(240.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = opportunity.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(opportunity.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(opportunity.organization, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentColor, modifier = Modifier.size(14.dp))
                    Text(opportunity.location, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
fun NearbyOpportunityItem(opportunity: Opportunity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = opportunity.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(opportunity.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(opportunity.organization, fontSize = 12.sp, color = Color.Gray)
                Text(opportunity.type, fontSize = 10.sp, color = PrimaryColor, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VolunteerHomeScreenPreview(){

    VolunteerHomeScreen(
        onNavigateToDetails = {},
        onNavigateToQrCheckIn = {},
        onNavigateToNotifications = {},
        onNavigateToProfile = {},
        onNavigateToExplore = {}
    )


}
