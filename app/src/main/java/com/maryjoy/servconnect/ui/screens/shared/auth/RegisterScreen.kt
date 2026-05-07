package com.maryjoy.servconnect.ui.screens.shared.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.R
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_REGISTER_VOLUNTEER
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_REGISTER_ORGANIZATION
import kotlinx.coroutines.launch

// ── Brand Colors ──────────────────────────────────────────────
private val PrimaryOlive  = Color(0xFF676B2C)
private val SecondaryGold = Color(0xFFF3DF90)
private val AccentOrange  = Color(0xFFFF5722)
private val DarkOlive     = Color(0xFF3D4019)
private val LightGold     = Color(0xFFFDF6DC)
private val TextDark      = Color(0xFF1C1C1C)
private val TextMuted     = Color(0xFF7A7A7A)
private val WhiteBg       = Color(0xFFFFFFFF)

@Composable
fun RegisterRoleScreen(navController: NavController) {

    // ── Entrance Animations ───────────────────────────────────
    val headerAlpha  = remember { Animatable(0f) }
    val card1Alpha   = remember { Animatable(0f) }
    val card1Slide   = remember { Animatable(60f) }
    val card2Alpha   = remember { Animatable(0f) }
    val card2Slide   = remember { Animatable(60f) }
    val bottomAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch {
            card1Alpha.animateTo(1f, tween(500, delayMillis = 300))
            card1Slide.animateTo(0f, tween(500, delayMillis = 300, easing = FastOutSlowInEasing))
        }
        launch {
            card2Alpha.animateTo(1f, tween(500, delayMillis = 450))
            card2Slide.animateTo(0f, tween(500, delayMillis = 450, easing = FastOutSlowInEasing))
        }
        launch { bottomAlpha.animateTo(1f, tween(500, delayMillis = 650)) }
    }

    // ── Root ──────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {

        // Olive gradient header band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, PrimaryOlive)
                    )
                )
        )

        // White curved bottom of header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(WhiteBg)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(52.dp))

            // ── Logo + Header ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo_servconnect),
                    contentDescription = "ServConnect Logo",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Join ServConnect",
                    color = SecondaryGold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Who are you joining as?",
                    color = SecondaryGold.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Volunteer Card ─────────────────────────────────
            RoleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(card1Alpha.value)
                    .offset(y = card1Slide.value.dp),
                icon = Icons.Default.VolunteerActivism,
                title = "I'm a Volunteer",
                subtitle = "Find opportunities to give back,\nbuild your service portfolio & earn certificates.",
                accentColor = PrimaryOlive,
                backgroundColor = LightGold,
                iconBackgroundColor = SecondaryGold,
                iconTint = PrimaryOlive,
                onClick = { navController.navigate(ROUT_REGISTER_VOLUNTEER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Organization Card ──────────────────────────────
            RoleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(card2Alpha.value)
                    .offset(y = card2Slide.value.dp),
                icon = Icons.Default.Business,
                title = "We're an Organization",
                subtitle = "Post volunteer opportunities, manage\nbookings & connect with willing hands.",
                accentColor = AccentOrange,
                backgroundColor = Color(0xFFFFF3EE),
                iconBackgroundColor = AccentOrange,
                iconTint = WhiteBg,
                onClick = { navController.navigate(ROUT_REGISTER_ORGANIZATION) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomAlpha.value)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
                Text(
                    text = "  or  ",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Already have account ───────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomAlpha.value),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Text(
                    text = "Sign In",
                    color = AccentOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(ROUT_LOGIN)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tagline ────────────────────────────────────────
            Text(
                text = "The Bridge to a Better Community",
                color = TextMuted,
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(bottomAlpha.value)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Reusable Role Card ────────────────────────────────────────
@Composable
fun RoleCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    backgroundColor: Color,
    iconBackgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = accentColor.copy(alpha = 0.15f),
                spotColor = accentColor.copy(alpha = 0.15f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Icon circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Text block
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            // Arrow indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            accentColor,
                            accentColor.copy(alpha = 0.3f)
                        )
                    )
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterRoleScreenPreview() {
    RegisterRoleScreen(rememberNavController())
}
