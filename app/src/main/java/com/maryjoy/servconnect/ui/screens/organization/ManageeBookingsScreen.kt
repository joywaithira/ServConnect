package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor


// --- DATA MODELS ---
data class Booking(
    val id: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val volunteerImageUrl: String = "",
    val status: String = "", // "Pending", "Confirmed", "Checked-in"
    val time: String = ""
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBookingsScreen(
    opportunityId: String,
    onNavigateBack: () -> Unit,
    onVolunteerClick: (String) -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(opportunityId) {
        val query = if (opportunityId == "all") {
            firestore.collection("bookings")
        } else {
            firestore.collection("bookings").whereEqualTo("opportunityId", opportunityId)
        }

        query.get().addOnSuccessListener { snapshot ->
            val list = snapshot.documents.map { doc ->
                val vId = doc.getString("volunteerId") ?: ""
                val status = doc.getString("status") ?: "Pending"
                val time = doc.getString("startTime") ?: "N/A"
                
                Booking(
                    id = doc.id,
                    volunteerId = vId,
                    volunteerName = doc.getString("volunteerName") ?: "Volunteer",
                    volunteerImageUrl = doc.getString("volunteerImageUrl") ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
                    status = status.replaceFirstChar { it.uppercase() },
                    time = time
                )
            }
            bookings = list
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Bookings", fontWeight = FontWeight.Bold, color = Color.White) },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Text(
                        if (bookings.isEmpty()) "No Bookings Found" else "Current Bookings",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1D1D)
                    )
                }

                items(bookings) { booking ->
                    BookingItem(booking, onClick = { onVolunteerClick(booking.volunteerId) })
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = booking.volunteerImageUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(booking.volunteerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Booked at ${booking.time}", fontSize = 12.sp, color = Color.Gray)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when(booking.status) {
                "Confirmed" -> PrimaryColor.copy(alpha = 0.1f)
                "Pending" -> Color.Yellow.copy(alpha = 0.1f)
                "Checked-in" -> SecondaryColor.copy(alpha = 0.3f)
                else -> Color.Gray.copy(alpha = 0.1f)
            }
        ) {
            Text(
                text = booking.status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when(booking.status) {
                    "Confirmed" -> PrimaryColor
                    "Pending" -> Color(0xFF856404)
                    "Checked-in" -> PrimaryColor
                    else -> Color.Gray
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}


@Preview(showBackground = true)
@Composable
fun ManageBookingsScreenPreview() {
    ManageBookingsScreen(opportunityId = "all", onNavigateBack = {}, onVolunteerClick = {})
}
