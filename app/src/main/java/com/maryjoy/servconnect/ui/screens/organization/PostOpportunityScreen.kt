package com.maryjoy.servconnect.ui.screens.organization

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maryjoy.servconnect.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOpportunityScreen(
    onNavigateBack: () -> Unit,
    onPostSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Volunteer") } // Volunteer or Community Service
    var groupSize by remember { mutableStateOf("Individual") } // Individual or Group
    var slots by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Opportunity", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Opportunity Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (e.g. Weekend Tutoring)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = slots,
                    onValueChange = { slots = it },
                    label = { Text("Slots") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Type & Grouping", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = type == "Volunteer", onClick = { type = "Volunteer" })
                Text("Volunteer")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = type == "Community Service", onClick = { type = "Community Service" })
                Text("Community Service")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = groupSize == "Individual", onClick = { groupSize = "Individual" })
                Text("Individual")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = groupSize == "Group", onClick = { groupSize = "Group" })
                Text("Group")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (auth == null || database == null) return@Button

                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        if (title.isNotBlank() && description.isNotBlank() && location.isNotBlank() && slots.isNotBlank() && date.isNotBlank()) {
                            val opportunityId = database.getReference("opportunities").push().key
                            if (opportunityId != null) {
                                // Fetch organization name from database
                                database.getReference("organizations").child(currentUser.uid).get().addOnSuccessListener {
                                    val organizationName = it.child("orgName").getValue(String::class.java) ?: "Unknown Organization"

                                    val totalSpots = slots.toIntOrNull() ?: 0
                                    val opportunityData = hashMapOf(
                                        "id" to opportunityId,
                                        "title" to title,
                                        "description" to description,
                                        "location" to location,
                                        "type" to type,
                                        "groupType" to groupSize,
                                        "totalSpots" to totalSpots,
                                        "spotsLeft" to totalSpots, // Initially, spotsLeft is equal to totalSpots
                                        "date" to date,
                                        "orgId" to currentUser.uid,
                                        "organization" to organizationName,
                                        "fullAddress" to location, // Assuming location is full address for now
                                        "time" to "Flexible", // Placeholder
                                        "category" to "General", // Placeholder
                                        "emoji" to "👋", // Placeholder
                                        "hours" to 0, // Placeholder
                                        "skillsNeeded" to listOf<String>(), // Placeholder
                                        "whatToBring" to listOf<String>(), // Placeholder
                                        "cardGradient" to listOf<Long>(), // Placeholder, store as ARGB long
                                        "orgRating" to 0.0f, // Placeholder
                                        "isVerified" to false, // Placeholder
                                        "isBookmarked" to false, // Placeholder
                                        "status" to "Open" // Default status
                                    )

                                    database.getReference("opportunities").child(opportunityId).setValue(opportunityData)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Opportunity posted successfully!", Toast.LENGTH_SHORT).show()
                                                onPostSuccess()
                                            } else {
                                                Toast.makeText(context, "Failed to post opportunity: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                                Log.e("PostOpportunityScreen", "Failed to post opportunity", task.exception)
                                            }
                                        }
                                }.addOnFailureListener {
                                    Toast.makeText(context, "Failed to retrieve organization name: ${it.message}", Toast.LENGTH_LONG).show()
                                    Log.e("PostOpportunityScreen", "Failed to retrieve organization name", it)
                                }
                            } else {
                                Toast.makeText(context, "Failed to generate opportunity ID.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "You need to be logged in as an organization to post an opportunity.", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Post Opportunity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostOpportunityScreenPreview() {
    PostOpportunityScreen(onNavigateBack = {}, onPostSuccess = {})
}