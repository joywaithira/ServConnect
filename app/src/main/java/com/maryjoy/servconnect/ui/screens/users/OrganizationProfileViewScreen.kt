package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

// --- DATA MODELS ---
data class Organization(
    val id: String,
    val name: String,
    val description: String,
    val location: String,
    val email: String,
    val phone: String,
    val isVerified: Boolean,
    val imageUrl: String,
    val remoteHelpNeeds: String? = null, // For those who can't go physically
)

data class OrgOpportunity(
    val id: String,
    val title: String,
    val date: String,
    val type: String
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationProfileViewScreen(
    orgId: String,
    onNavigateBack: () -> Unit,
    onOpportunityClick: (String) -> Unit,
    onMessageClick: (String) -> Unit
) {
    // Mock data for the organization
    val organization = Organization(
        id = orgId,
        name = "Nairobi Children's Home",
        description = "We provide a safe haven and education for orphaned and vulnerable children in Nairobi. Our mission is to empower the next generation through love and care.",
        location = "Lower Kabete, Nairobi",
        email = "info@nairobihome.org",
        phone = "+254 712 345 678",
        isVerified = true,
        imageUrl = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=400",
        remoteHelpNeeds = "We are currently in need of dry cereals, school stationery, and winter clothing. You can donate via our office or contact us for pickup."
    )

    val opportunities = listOf(
        OrgOpportunity("1", "Weekend Tutoring", "Every Sat-Sun", "Volunteer"),
        OrgOpportunity("2", "Garden Maintenance", "May 15, 2024", "Community Service"),
        OrgOpportunity("3", "Storytelling Session", "May 20, 2024", "Volunteer")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organization Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onMessageClick(organization.name) }) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Message", tint = Color.White)
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
        ) {
            // 1. Header Image & Profile Info
            item { OrgHeader(organization) }

            // 2. Contact Info
            item { ContactSection(organization) }

            // 3. Remote Help Section (If available)
            organization.remoteHelpNeeds?.let { needs ->
                item { RemoteHelpCard(needs) }
            }

            // 4. Opportunities Title
            item {
                Text(
                    text = "Open Opportunities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D),
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 5. Opportunities List
            items(opportunities) { opportunity ->
                OrgOpportunityItem(opportunity) { onOpportunityClick(opportunity.id) }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun OrgHeader(org: Organization) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = org.imageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = org.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                if (org.isVerified) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = org.location, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = org.description,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ContactSection(org: Organization) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ContactItem(icon = Icons.Default.Email, text = org.email)
            Spacer(modifier = Modifier.height(12.dp))
            ContactItem(icon = Icons.Default.Phone, text = org.phone)
        }
    }
}

@Composable
fun ContactItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun RemoteHelpCard(needs: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = PrimaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to help from home", fontWeight = FontWeight.Bold, color = PrimaryColor)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = needs,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun OrgOpportunityItem(opportunity: OrgOpportunity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(opportunity.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(opportunity.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PrimaryColor.copy(alpha = 0.1f)
            ) {
                Text(
                    text = opportunity.type,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OrganizationProfileViewScreenPreview(){

    OrganizationProfileViewScreen(
        orgId = "123",
        onNavigateBack = {},
        onOpportunityClick = {},
        onMessageClick = {}
    )


}
