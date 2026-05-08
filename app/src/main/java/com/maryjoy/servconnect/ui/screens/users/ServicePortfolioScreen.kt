package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// --- DATA MODELS (Included in same file as requested) ---
data class Certificate(
    val id: String,
    val organizationName: String,
    val activityTitle: String,
    val hoursCompleted: Int,
    val dateIssued: String
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicePortfolioScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    // Mock data for certificates
    val certificates = listOf(
        Certificate("1", "Red Cross Kenya", "First Aid Support", 20, "12 May 2024"),
        Certificate("2", "Green Earth NGO", "Tree Planting Drive", 8, "05 April 2024"),
        Certificate("3", "Nairobi Children's Home", "Weekend Tutoring", 15, "20 March 2024")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Portfolio", fontWeight = FontWeight.Bold, color = Color.White) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PortfolioSummary(totalHours = 43, totalCertificates = 3, onViewHistory = onNavigateToHistory) }

            item {
                Text(
                    text = "Your Certificates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
            }

            items(certificates) { certificate ->
                CertificateCard(certificate)
            }
        }
    }
}

// --- SUB-COMPOSABLES ---

@Composable
fun PortfolioSummary(totalHours: Int, totalCertificates: Int, onViewHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewHistory() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total Impact", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("$totalHours Hours", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.White.copy(alpha = 0.3f))

            Column {
                Text("Certificates", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("$totalCertificates Earned", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CertificateCard(certificate: Certificate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = certificate.activityTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                Text(
                    text = certificate.organizationName,
                    fontSize = 14.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(certificate.dateIssued, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${certificate.hoursCompleted} hrs", fontSize = 12.sp, color = Color.Gray)
                }
            }

            IconButton(onClick = { /* TODO: Download/Share PDF */ }) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = PrimaryColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServicePortfolioScreenPreview(){
    ServicePortfolioScreen(onNavigateBack = {}, onNavigateToHistory = {})
}