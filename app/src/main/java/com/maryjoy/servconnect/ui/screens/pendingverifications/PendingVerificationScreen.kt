package com.maryjoy.servconnect.ui.screens.pendingverifications

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.R
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_ORG_HOME
import kotlinx.coroutines.delay
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
private val OrangeBg      = Color(0xFFFFF3EE)
private val SuccessGreen  = Color(0xFF388E3C)

@Composable
fun PendingVerificationScreen(navController: NavController) {

    // ── State ─────────────────────────────────────────────────
    // Tracks whether we are currently checking Firebase
    // for the org's verification status
    var isChecking      by remember { mutableStateOf(false) }
    var statusMessage   by remember { mutableStateOf("") }
    var isVerified      by remember { mutableStateOf(false) }

    // Holds the org name fetched from Firestore
    // so we can greet them personally on this screen
    var orgName         by remember { mutableStateOf("") }

    // ── Fetch org name on screen open ─────────────────────────
    // As soon as this screen appears we pull the org's name
    // from Firestore so we can display it in the greeting.
    // We use the currently logged-in user's uid to find their doc.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("organizations")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    orgName = doc.getString("orgName") ?: "Organization"
                }
        }
    }

    // ── Pulsing animation for the clock icon ──────────────────
    // Gives the waiting icon a gentle breathing effect so the
    // screen feels alive rather than frozen
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            pulseScale.animateTo(
                targetValue = 1.08f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = FastOutSlowInEasing)
            )
        }
    }

    // ── Entrance animations ───────────────────────────────────
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { contentAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        launch { contentSlide.animateTo(0f, tween(700, easing = FastOutSlowInEasing)) }
    }

    // ── Check Verification Status ─────────────────────────────
    // Called when the org taps "Check My Status".
    // Looks up their Firestore document and reads
    // the "isVerified" and "status" fields that the
    // admin will have updated after reviewing their profile.
    fun checkVerificationStatus() {
        isChecking = true
        statusMessage = ""

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            isChecking = false
            statusMessage = "Session expired. Please log in again."
            return
        }

        FirebaseFirestore.getInstance()
            .collection("organizations")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                isChecking = false

                if (!doc.exists()) {
                    statusMessage = "Organization profile not found."
                    return@addOnSuccessListener
                }

                // Read the two fields the admin updates
                // when they approve or reject the org
                val status     = doc.getString("status")     ?: "pending"
                val verified   = doc.getBoolean("isVerified") ?: false

                when {
                    verified && status == "verified" -> {
                        // Admin has approved them —
                        // navigate to the org home screen
                        isVerified = true
                        statusMessage = "✅ Your organization has been verified!"
                    }
                    status == "rejected" -> {
                        // Admin rejected the application.
                        // Show the reason if there is one.
                        val reason = doc.getString("rejectionReason")
                            ?: "Please contact support for more information."
                        statusMessage = "❌ Application rejected: $reason"
                    }
                    else -> {
                        // Still pending — nothing changed yet
                        statusMessage =
                            "⏳ Still under review. Please check back later."
                    }
                }
            }
            .addOnFailureListener { e ->
                isChecking = false
                statusMessage = "Failed to check status: ${e.message}"
            }
    }

    // ── Auto-navigate when verified ───────────────────────────
    // If the status check comes back as verified,
    // wait 1.5 seconds then navigate to org home automatically
    LaunchedEffect(isVerified) {
        if (isVerified) {
            delay(1500)
            navController.navigate(ROUT_ORG_HOME) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {

        // Olive gradient header band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, PrimaryOlive)
                    )
                )
        )

        // White curved cutout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(52.dp))

            // ── Header ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo_servconnect),
                    contentDescription = "ServConnect Logo",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Application Submitted",
                    color = SecondaryGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "The Bridge to a Better Community",
                    color = SecondaryGold.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Main Card ──────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = contentSlide.value.dp)
                    .alpha(contentAlpha.value),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // ── Pulsing clock icon ──────────────────────
                    // The gentle scale pulse shows that something
                    // is happening behind the scenes (admin review)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(pulseScale.value)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        SecondaryGold.copy(alpha = 0.4f),
                                        LightGold
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = PrimaryOlive,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // ── Greeting with org name ──────────────────
                    // Pulled from Firestore on screen load
                    Text(
                        text = if (orgName.isNotEmpty())
                            "Thank you, $orgName!"
                        else
                            "Thank you for registering!",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your organization profile has been submitted " +
                                "and is currently under review by our admin team.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // ── Status Steps ───────────────────────────
                    // Visual step tracker showing where they are
                    // in the verification process
                    VerificationStepRow(
                        icon = Icons.Default.CheckCircle,
                        iconColor = SuccessGreen,
                        title = "Profile Submitted",
                        subtitle = "Your organization details have been received",
                        isDone = true
                    )

                    VerificationStepRow(
                        icon = Icons.Default.ManageSearch,
                        iconColor = AccentOrange,
                        title = "Under Review",
                        subtitle = "Our admin team is reviewing your application",
                        isDone = false
                    )

                    VerificationStepRow(
                        icon = Icons.Default.VerifiedUser,
                        iconColor = Color(0xFFBDBDBD),
                        title = "Verification Complete",
                        subtitle = "You'll be able to post opportunities once verified",
                        isDone = false
                    )

                    // ── Timeline notice ─────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGold)
                            .border(
                                1.dp,
                                PrimaryOlive.copy(alpha = 0.25f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "✦  What happens next?",
                                color = PrimaryOlive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Review typically takes 1–2 business days\n" +
                                        "• You will receive an email once approved\n" +
                                        "• Tap the button below to check your status\n" +
                                        "• Contact support if you have any questions",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // ── Status message ─────────────────────────
                    // Appears after the user taps "Check My Status"
                    // Green for verified, red for rejected,
                    // orange for still pending
                    if (statusMessage.isNotEmpty()) {
                        val isSuccess  = statusMessage.startsWith("✅")
                        val isRejected = statusMessage.startsWith("❌")
                        val bgColor = when {
                            isSuccess  -> SuccessGreen.copy(alpha = 0.08f)
                            isRejected -> Color(0xFFD32F2F).copy(alpha = 0.08f)
                            else       -> AccentOrange.copy(alpha = 0.08f)
                        }
                        val borderColor = when {
                            isSuccess  -> SuccessGreen.copy(alpha = 0.3f)
                            isRejected -> Color(0xFFD32F2F).copy(alpha = 0.3f)
                            else       -> AccentOrange.copy(alpha = 0.3f)
                        }
                        val textColor = when {
                            isSuccess  -> SuccessGreen
                            isRejected -> Color(0xFFD32F2F)
                            else       -> AccentOrange
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = statusMessage,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // ── Check Status Button ─────────────────────
                    // Queries Firestore to see if the admin
                    // has updated isVerified to true
                    Button(
                        onClick = { checkVerificationStatus() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryOlive,
                            disabledContainerColor = PrimaryOlive.copy(alpha = 0.5f)
                        ),
                        enabled = !isChecking && !isVerified
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = SecondaryGold,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isVerified) "Verified! Redirecting..."
                                    else "Check My Status",
                                    color = SecondaryGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ── Sign out link ──────────────────────────
                    // Lets the org log out and come back later
                    // to check if they have been verified
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Come back later? ",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sign Out",
                            color = AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                // Signs out of Firebase Auth then
                                // returns to the login screen
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(ROUT_LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ── Verification Step Row ─────────────────────────────────────
// Each row represents one step in the verification process.
// isDone = true makes the icon filled and bold,
// isDone = false makes it look greyed out / in progress
@Composable
fun VerificationStepRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isDone: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = if (isDone) 0.15f else 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Text
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = if (isDone) TextDark else TextMuted,
                fontSize = 13.sp,
                fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingVerificationScreenPreview() {
    PendingVerificationScreen(rememberNavController())
}