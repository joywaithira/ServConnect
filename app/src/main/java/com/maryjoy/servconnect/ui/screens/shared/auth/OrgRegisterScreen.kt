package com.maryjoy.servconnect.ui.screens.shared.auth

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
fun OrgRegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var orgName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }

    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organization Registration", fontWeight = FontWeight.Bold, color = Color.White) },
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
            Text("Organization Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)

            OutlinedTextField(
                value = orgName,
                onValueChange = { orgName = it },
                label = { Text("Organization Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) }
            )

            OutlinedTextField(
                value = regNumber,
                onValueChange = { regNumber = it },
                label = { Text("Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.filter { !it.isWhitespace() } },
                label = { Text("Official Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Physical Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "By registering, you agree to provide valid documentation for verification by our admin team.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() && orgName.isNotBlank() && regNumber.isNotBlank() && phone.isNotBlank() && location.isNotBlank()) {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid official email address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (auth != null && database != null) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val user = auth.currentUser
                                        user?.let {
                                            val userId = it.uid
                                            val organizationData = hashMapOf(
                                                "orgName" to orgName,
                                                "regNumber" to regNumber,
                                                "email" to email,
                                                "phone" to phone,
                                                "location" to location,
                                                "type" to "organization" // Added to differentiate user types
                                            )
                                            database.getReference("organizations").child(userId).setValue(organizationData)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        Toast.makeText(context, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show()
                                                        onRegisterSuccess()
                                                    } else {
                                                        Toast.makeText(context, "Failed to save organization data: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                                                        Log.e("OrgRegisterScreen", "Failed to save organization data", it.exception)
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(context, "Registration failed: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.e("OrgRegisterScreen", "Registration failed", it.exception)
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
                Text("Submit for Verification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrgRegisterScreenPreview() {
    OrgRegisterScreen(
        onNavigateBack = {},
        onRegisterSuccess = {}
    )
}