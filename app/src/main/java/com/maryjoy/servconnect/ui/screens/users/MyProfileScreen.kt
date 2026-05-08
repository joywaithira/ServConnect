package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onNavigateToPortfolio: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Profile Header (Avatar, Name, Location)
            item { ProfileHeader() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Stats Row (Hours, Activities, Rating)
            item { StatsRow() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Skills & Interests
            item { SkillsAndInterestsSection() }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Navigation Menu (Portfolio, History)
            item {
                MenuSection(
                    onNavigateToPortfolio = onNavigateToPortfolio,
                    onNavigateToHistory = onNavigateToHistory
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ProfileHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar
        AsyncImage(
            model = "https://i.pravatar.cc/150?img=32", // Placeholder image
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp )
                .clip(CircleShape)
                .background(SecondaryColor)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Jane Doe",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1D1D)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = AccentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Nairobi, Kenya",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = "120", label = "Hours", icon = Icons.Default.AccessTime)
        StatItem(value = "15", label = "Activities", icon = Icons.Default.VolunteerActivism)
        StatItem(value = "4.8", label = "Rating", icon = Icons.Default.Star, isRating = true)
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, isRating: Boolean = false) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.size(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isRating) SecondaryColor else PrimaryColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1D1D)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SkillsAndInterestsSection() {
    val skills = listOf("Childcare", "Teaching", "First Aid", "Event Organizing", "Elderly Care")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Skills & Interests",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1D1D),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Use a simple Column of Rows to avoid FlowRow binary compatibility issues
        // which can cause NoSuchMethodError on some devices or versions.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            skills.chunked(3).forEach { rowSkills ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowSkills.forEach { skill ->
                        SkillChip(skill)
                    }
                }
            }
        }
    }
}

@Composable
fun SkillChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SecondaryColor.copy(alpha = 0.3f),
        contentColor = PrimaryColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MenuSection(
    onNavigateToPortfolio: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MenuItem(
            title = "Service Portfolio",
            subtitle = "View your certificates and achievements",
            icon = Icons.Default.WorkspacePremium,
            onClick = onNavigateToPortfolio
        )

        MenuItem(
            title = "Activity History",
            subtitle = "Past volunteering and community service",
            icon = Icons.Default.History,
            onClick = onNavigateToHistory
        )

        MenuItem(
            title = "Legacy Badge (Family Streak)",
            subtitle = "Track your family's community goals",
            icon = Icons.Default.LocalFireDepartment,
            iconTint = AccentColor
        ) { /* TODO: Implement later as per requirements */ }
    }
}

@Composable
fun MenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = PrimaryColor,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecondaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1D)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyProfileScreenPreview(){
    MyProfileScreen(
        onNavigateToPortfolio = {},
        onNavigateToHistory = {},
        onNavigateToSettings = {}
    ) {}
}



