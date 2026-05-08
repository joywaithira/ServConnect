package com.maryjoy.servconnect.ui.screens.shared.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.screens.navigation.Screen
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSelectionScreen(
    onNavigateBack: () -> Unit,
    onVolunteerSelected: () -> Unit,
    onOrgSelected: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold, color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Join ServConnect",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )

            Text(
                "Choose how you want to use the platform",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            SelectionCard(
                title = "Volunteer",
                description = "I want to find opportunities and give back to my community.",
                icon = Icons.Default.Person,
                onClick = onVolunteerSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            SelectionCard(
                title = "Organization",
                description = "I want to post opportunities and manage volunteers for my NGO.",
                icon = Icons.Default.Business,
                onClick = onOrgSelected
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = SecondaryColor.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterRoleScreenPreview() {
    RegisterRoleScreen(rememberNavController())
}

@Composable
fun RegisterRoleScreen(navController: NavHostController) {
    RegisterSelectionScreen(
        onNavigateBack = { navController.popBackStack() },
        onVolunteerSelected = { navController.navigate(Screen.VolunteerRegister.route) },
        onOrgSelected = { navController.navigate(Screen.OrgRegister.route) }
    )
}
