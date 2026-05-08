package com.maryjoy.servconnect.ui.screens.organization
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.maryjoy.servconnect.ui.theme.PrimaryColor
import com.maryjoy.servconnect.ui.theme.SecondaryColor
import android.util.Log
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueCertificateScreen(
    volunteerName: String,
    activityTitle: String,
    onNavigateBack: () -> Unit,
    onIssueSuccess: () -> Unit,
    onIssueCertificate: (hours: Int, remarks: String) -> Unit = { _, _ -> }
) {
    var hours by remember { mutableStateOf("4") }
    var remarks by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Issue Certificate", fontWeight = FontWeight.Bold, color = Color.White) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Certificate Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SecondaryColor, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Certificate of Participation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This is to certify that", fontSize = 12.sp, color = Color.Gray)
                    Text(volunteerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("has successfully completed", fontSize = 12.sp, color = Color.Gray)
                    Text(activityTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Hours: $hours", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = PrimaryColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text("Confirm Hours Completed") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val hoursInt = hours.toIntOrNull() ?: 0
                    onIssueCertificate(hoursInt, remarks)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Confirm & Issue Certificate", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Once issued, the certificate will be available in the volunteer\'s service portfolio.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IssueCertificateScreenPreview() {
    IssueCertificateScreen(
        volunteerName = "John Doe",
        activityTitle = "Tree Planting Activity",
        onNavigateBack = {},
        onIssueSuccess = {}
    )
}