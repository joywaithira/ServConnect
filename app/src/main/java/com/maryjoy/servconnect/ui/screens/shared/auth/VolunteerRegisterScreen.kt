package com.maryjoy.servconnect.ui.screens.auth

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maryjoy.servconnect.ui.theme.PrimaryColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerRegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }

    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Registration", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.filter { !it.isWhitespace() } },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Interests & Skills", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

            OutlinedTextField(
                value = interests,
                onValueChange = { interests = it },
                label = { Text("e.g. Teaching, Environment, Health") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Tell us what you're passionate about...") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && interests.isNotBlank()) {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (auth != null && database != null) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val user = auth.currentUser
                                        user?.let {
                                            val userId = it.uid
                                            val volunteerData = hashMapOf(
                                                "fullName" to fullName,
                                                "email" to email,
                                                "phone" to phone,
                                                "interests" to interests,
                                                "type" to "volunteer" // Added to differentiate user types
                                            )
                                            database.getReference("volunteers").child(userId).setValue(volunteerData)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        Toast.makeText(context, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show()
                                                        onRegisterSuccess()
                                                    } else {
                                                        Toast.makeText(context, "Failed to save volunteer data: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                                                        Log.e("VolunteerRegisterScreen", "Failed to save volunteer data", it.exception)
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(context, "Registration failed: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.e("VolunteerRegisterScreen", "Registration failed", it.exception)
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VolunteerRegisterScreenPreview() {
    VolunteerRegisterScreen(
        onNavigateBack = {},
        onRegisterSuccess = {}
    )
}