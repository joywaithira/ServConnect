package com.maryjoy.servconnect.ui.screens.shared.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maryjoy.servconnect.R
import com.maryjoy.servconnect.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (Boolean) -> Unit // Boolean: true for Org, false for Volunteer
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val auth = remember { if (isPreview) null else FirebaseAuth.getInstance() }
    val database = remember { if (isPreview) null else FirebaseDatabase.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_servconnect2),
            contentDescription = "Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Text(
            text = "Sign in to continue",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryColor) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryColor) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { /* Forgot Password */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = PrimaryColor, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (auth != null && database != null) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    val user = auth.currentUser
                                    user?.let { firebaseUser ->
                                        val userId = firebaseUser.uid
                                        // Check if the user is an Organization
                                        database.getReference("organizations").child(userId).get()
                                            .addOnSuccessListener {
                                                if (it.exists()) {
                                                    val userData = hashMapOf(
                                                        "email" to firebaseUser.email,
                                                        "lastLogin" to System.currentTimeMillis(),
                                                        "type" to "organization"
                                                    )
                                                    database.getReference("users").child(userId)
                                                        .setValue(userData)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Login successful as Organization!",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                onLoginSuccess(true)
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Failed to update user data: ${task.exception?.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                Log.e(
                                                                    "LoginScreen",
                                                                    "Failed to update user data",
                                                                    task.exception
                                                                )
                                                            }
                                                        }
                                                } else {
                                                    // Check if the user is a Volunteer
                                                    database.getReference("volunteers")
                                                        .child(userId).get().addOnSuccessListener {
                                                        if (it.exists()) {
                                                            val userData = hashMapOf(
                                                                "email" to firebaseUser.email,
                                                                "lastLogin" to System.currentTimeMillis(),
                                                                "type" to "volunteer"
                                                            )
                                                            database.getReference("users")
                                                                .child(userId).setValue(userData)
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Login successful as Volunteer!",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        onLoginSuccess(false)
                                                                    } else {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Failed to update user data: ${task.exception?.message}",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                        Log.e(
                                                                            "LoginScreen",
                                                                            "Failed to update user data",
                                                                            task.exception
                                                                        )
                                                                    }
                                                                }
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "User data not found. Please register.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                            Log.e(
                                                                "LoginScreen",
                                                                "User data not found in organizations or volunteers nodes."
                                                            )
                                                        }
                                                    }.addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to retrieve volunteer data: ${it.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        Log.e(
                                                            "LoginScreen",
                                                            "Failed to retrieve volunteer data",
                                                            it
                                                        )
                                                    }
                                                }
                                            }.addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Failed to retrieve organization data: ${it.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Log.e(
                                                "LoginScreen",
                                                "Failed to retrieve organization data",
                                                it
                                            )
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Authentication failed: ${it.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("LoginScreen", "Authentication failed", it.exception)
                                }
                            }
                    } else {
                        Toast.makeText(context, "Firebase not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don\'t have an account?", color = Color.Gray)
            TextButton(onClick = onNavigateToRegister) {
                Text("Register", color = PrimaryColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToRegister = {},
        onLoginSuccess = {}
    )
}