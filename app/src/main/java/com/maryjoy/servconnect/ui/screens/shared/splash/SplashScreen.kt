package com.maryjoy.servconnect.ui.screens.shared.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_SPLASH
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController){

    // =========================
    // Navigation Delay
    // =========================
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(ROUT_LOGIN) {
            popUpTo(ROUT_SPLASH) { inclusive = true }
        }
    }

    // =========================
    // UI
    // =========================


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary), // Your green
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // App Name
        Text(
            text = "ServConnect",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tagline
        Text(
            text = "Connecting You to Impact",
            color = MaterialTheme.colorScheme.secondary // Your yellow
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Loader
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.tertiary // Your orange
        )



    }


}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){

    SplashScreen(rememberNavController())


}