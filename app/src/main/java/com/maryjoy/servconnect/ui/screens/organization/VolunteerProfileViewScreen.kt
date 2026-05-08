package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerProfileViewScreen(
    volunteerId: String,
    onNavigateBack: () -> Unit,
    onIssueCertificateClick: (String, String) -> Unit,
    onMessageClick: (String) -> Unit
) {
    val volunteerName = "Jane Doe" // Mocking for now
    val activityTitle = "Weekend Tutoring" // Mocking for now

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onMessageClick(volunteerName) }) {
                        Icon(Icons.Default.Message, contentDescription = "Message", tint = Color.White)
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Header
            AsyncImage(
                model = "https://i.pravatar.cc/150?img=32",
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(volunteerName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
            Text("Volunteer since Jan 2024", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VolunteerStatItem("120", "Total Hours")
                VolunteerStatItem("15", "Activities")
                VolunteerStatItem("4.8", "Rating")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skills Section
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Skills", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkillBadge("Teaching")
                    SkillBadge("First Aid")
                    SkillBadge("Childcare")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = { onIssueCertificateClick(volunteerName, activityTitle) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Issue Certificate", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun VolunteerStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun SkillBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SecondaryColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PrimaryColor
        )
    }
}


@Preview(showBackground = true)
@Composable
fun VolunteerProfileViewScreenPreview() {
    VolunteerProfileViewScreen(
        volunteerId = "preview_id",
        onNavigateBack = {},
        onIssueCertificateClick = { _, _ -> },
        onMessageClick = {}
    )
}