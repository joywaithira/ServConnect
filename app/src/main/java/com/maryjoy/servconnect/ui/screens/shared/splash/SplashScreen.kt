package com.maryjoy.servconnect.ui.screens.shared.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
import kotlinx.coroutines.delay

// Brand Colors
private val PrimaryOlive   = Color(0xFF676B2C)
private val SecondaryGold  = Color(0xFFF3DF90)
private val AccentOrange   = Color(0xFFFF5722)
private val DarkOlive      = Color(0xFF3D4019)

@Composable
fun SplashScreen(navController: NavController) {

    // --- Animation states ---
    val logoScale   = remember { Animatable(0.4f) }
    val logoAlpha   = remember { Animatable(0f) }
    val titleAlpha  = remember { Animatable(0f) }
    val taglineAlpha= remember { Animatable(0f) }
    val dotAlpha    = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Logo pops in
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )

        // 2. App name fades in
        delay(200)
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // 3. Tagline fades in
        delay(300)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )

        // 4. Loading dots appear
        delay(200)
        dotAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )

        // 5. Wait then navigate
        delay(1200)
        navController.navigate(ROUT_LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    // --- Background gradient (dark olive → olive) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkOlive, PrimaryOlive)
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Decorative circle behind the logo
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SecondaryGold.copy(alpha = 0.25f),
                            SecondaryGold.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo circle ──
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(CircleShape)
                    .background(SecondaryGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SC",
                    color = PrimaryOlive,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── App name ──
            Text(
                text = "ServConnect",
                color = SecondaryGold,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Accent divider ──
            Box(
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .width(60.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(AccentOrange)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tagline ──
            Text(
                text = "Connecting Services, Simplifying Lives",
                color = SecondaryGold.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // ── Loading indicator ──
            CircularProgressIndicator(
                modifier = Modifier
                    .size(28.dp)
                    .alpha(dotAlpha.value),
                color = AccentOrange,
                strokeWidth = 2.5.dp,
                trackColor = SecondaryGold.copy(alpha = 0.2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(rememberNavController())
}