package com.maryjoy.servconnect.ui.screens.organization
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.maryjoy.servconnect.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var orgName by remember { mutableStateOf("Nairobi Children's Home") }
    var email by remember { mutableStateOf("info@nairobihome.org") }
    var phone by remember { mutableStateOf("+254 712 345 678") }
    var location by remember { mutableStateOf("Lower Kabete, Nairobi") }
    var description by remember { mutableStateOf("We provide a safe haven and education for orphaned and vulnerable children.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organization Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Save Changes */ }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=400",
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { /* Change Photo */ },
                    modifier = Modifier.size(32.dp).background(PrimaryColor, CircleShape)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            OutlinedTextField(
                value = orgName,
                onValueChange = { orgName = it },
                label = { Text("Organization Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Physical Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                elevation = null
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrganizationProfileScreenPreview() {
    OrgProfileScreen(onNavigateBack = {}, onLogout = {})
}