package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.maryjoy.servconnect.ui.screens.users.OpportunityDetailScreen
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgOpportunityDetailScreen(
    opportunityId: String,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onManageBookingsClick: () -> Unit,
    onViewQrCodeClick: () -> Unit
) {
    val database = remember { FirebaseDatabase.getInstance() }
    var title by remember { mutableStateOf("Loading...") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var slotsFilled by remember { mutableStateOf(0) }
    var totalSlots by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("Open") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(opportunityId) {
        database.getReference("opportunities").child(opportunityId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    title = snapshot.child("title").value?.toString() ?: ""
                    date = snapshot.child("date").value?.toString() ?: ""
                    location = snapshot.child("location").value?.toString() ?: ""
                    description = snapshot.child("description").value?.toString() ?: ""
                    totalSlots = (snapshot.child("totalSlots").value as? Long)?.toInt() ?: 0
                    val spotsLeft = (snapshot.child("spotsLeft").value as? Long)?.toInt() ?: 0
                    slotsFilled = totalSlots - spotsLeft
                    status = snapshot.child("status").value?.toString() ?: "Open"
                    imageUrl = snapshot.child("imageUrl").value?.toString()
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opportunity Detail", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Header Image
                AsyncImage(
                    model = imageUrl ?: "https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?auto=format&fit=crop&w=800",
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    // 2. Title and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Info Row (Date, Location, Slots)
                    InfoRow(icon = Icons.Default.CalendarToday, text = date)
                    InfoRow(icon = Icons.Default.LocationOn, text = location)
                    InfoRow(icon = Icons.Default.People, text = "$slotsFilled / $totalSlots Slots Filled")

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. Description
                    Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1D1D))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        description,
                        fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 5. Action Buttons
                    Button(
                        onClick = onManageBookingsClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Bookings", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onViewQrCodeClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View QR Code", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { /* Close Opportunity */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Close Opportunity", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Preview(showBackground = true)
@Composable
fun OpportunityDetailScreenPreview() {
    OrgOpportunityDetailScreen(
        opportunityId = "preview_id",
        onNavigateBack = {},
        onEditClick = {},
        onManageBookingsClick = {},
        onViewQrCodeClick = {}
    )
}