package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Brand Colors ──────────────────────────────────────────────
private val PrimaryOlive   = Color(0xFF676B2C)
private val SecondaryGold  = Color(0xFFF3DF90)
private val AccentOrange   = Color(0xFFFF5722)
private val DarkOlive      = Color(0xFF3D4019)
private val LightGold      = Color(0xFFFDF6DC)
private val TextDark       = Color(0xFF1C1C1C)
private val TextMuted      = Color(0xFF7A7A7A)
private val WhiteBg        = Color(0xFFFFFFFF)
private val FieldBorder    = Color(0xFFE0E0E0)
private val SuccessGreen   = Color(0xFF388E3C)
private val SoftBlue       = Color(0xFF1976D2)
private val CertGold       = Color(0xFFFFD700)
private val CertGoldDark   = Color(0xFFB8860B)

// ── Data classes ──────────────────────────────────────────────
data class CertifiableVolunteer(
    val bookingId: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val volunteerEmail: String = "",
    val hoursLogged: Int = 0,
    val checkedIn: Boolean = false,
    val hasCertificate: Boolean = false,
    val certificateId: String = ""
)

@Composable
fun IssueCertificateScreen(
    navController: NavController,
    opportunityId: String
) {
    val scope = rememberCoroutineScope()

    // ── State ─────────────────────────────────────────────────
    var opportunityTitle by remember { mutableStateOf("") }
    var opportunityDate  by remember { mutableStateOf("") }
    var orgName          by remember { mutableStateOf("") }
    var volunteers       by remember { mutableStateOf<List<CertifiableVolunteer>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }
    var firebaseError    by remember { mutableStateOf("") }
    var successMessage   by remember { mutableStateOf("") }
    var issuingId        by remember { mutableStateOf<String?>(null) }

    // Preview certificate dialog
    var showPreview      by remember { mutableStateOf(false) }
    var previewVolunteer by remember { mutableStateOf<CertifiableVolunteer?>(null) }

    // Celebrate animation state
    var showCelebration  by remember { mutableStateOf(false) }
    var celebrationName  by remember { mutableStateOf("") }

    // ── Entrance animations ───────────────────────────────────
    val headerAlpha  = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(300)
            contentAlpha.animateTo(1f, tween(500))
            contentSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
    }

    // ── Trophy bounce animation ───────────────────────────────
    val trophyBounce = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            trophyBounce.animateTo(
                -8f, tween(700, easing = FastOutSlowInEasing)
            )
            trophyBounce.animateTo(
                0f, tween(700, easing = FastOutSlowInEasing)
            )
            delay(800)
        }
    }

    // ── Gold shimmer animation ────────────────────────────────
    val shimmerOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shimmerOffset.animateTo(
            targetValue   = 1000f,
            animationSpec = infiniteRepeatable(
                animation  = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // ── Confetti particles ────────────────────────────────────
    // Each particle has a random position, color and rotation
    val confettiColors = listOf(
        SecondaryGold, AccentOrange, SuccessGreen,
        SoftBlue, Color(0xFFE91E63), Color(0xFF9C27B0)
    )
    data class Particle(
        val x: Float, val y: Float,
        val color: Color,
        val rotation: Float,
        val size: Float
    )

    val particles = remember {
        (0..40).map {
            Particle(
                x        = (0..100).random().toFloat(),
                y        = (-20..80).random().toFloat(),
                color    = confettiColors.random(),
                rotation = (0..360).random().toFloat(),
                size     = (4..10).random().toFloat()
            )
        }
    }

    // Particle fall animation
    val particleFall = remember { Animatable(0f) }
    LaunchedEffect(showCelebration) {
        if (showCelebration) {
            particleFall.snapTo(0f)
            particleFall.animateTo(
                1f,
                tween(3000, easing = LinearEasing)
            )
            delay(3000)
            showCelebration = false
        }
    }

    // ── Fetch data ────────────────────────────────────────────
    // Pulls opportunity details and all completed bookings
    // for this opportunity. Only completed + checked-in
    // volunteers are eligible for a certificate.
    LaunchedEffect(opportunityId) {
        val db  = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect

        // Fetch opportunity details
        db.collection("opportunities")
            .document(opportunityId)
            .get()
            .addOnSuccessListener { doc ->
                opportunityTitle = doc.getString("title")   ?: ""
                opportunityDate  = doc.getString("date")    ?: ""
                orgName          = doc.getString("orgName") ?: ""
            }

        // Fetch completed bookings for this opportunity.
        // We filter by "completed" status — only volunteers
        // who have finished the activity can get a certificate.
        db.collection("bookings")
            .whereEqualTo("opportunityId", opportunityId)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { snapshot ->
                volunteers = snapshot.documents.map { doc ->
                    CertifiableVolunteer(
                        bookingId       = doc.id,
                        volunteerId     = doc.getString("volunteerId")    ?: "",
                        volunteerName   = doc.getString("volunteerName")  ?: "",
                        volunteerEmail  = doc.getString("volunteerEmail") ?: "",
                        hoursLogged     = (doc.getLong("hours")           ?: 0).toInt(),
                        checkedIn       = doc.getBoolean("checkedIn")     ?: false,
                        hasCertificate  = doc.getBoolean("hasCertificate") ?: false,
                        certificateId   = doc.getString("certificateId")  ?: ""
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load volunteers: ${e.message}"
                isLoading     = false
            }
    }

    // ── Issue Certificate ─────────────────────────────────────
    // Generates a unique certificate record in Firestore and
    // links it to both the booking and the volunteer's portfolio.
    // The certificate stores all the info the volunteer needs:
    // who issued it, what activity, how many hours, when.
    fun issueCertificate(volunteer: CertifiableVolunteer) {
        issuingId    = volunteer.bookingId
        firebaseError = ""

        val db  = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return

        // Build the certificate document
        val certificateData = hashMapOf(
            // Who the certificate is for
            "volunteerId"      to volunteer.volunteerId,
            "volunteerName"    to volunteer.volunteerName,
            "volunteerEmail"   to volunteer.volunteerEmail,

            // What they did
            "opportunityId"    to opportunityId,
            "opportunityTitle" to opportunityTitle,
            "activityDate"     to opportunityDate,
            "hoursCompleted"   to volunteer.hoursLogged,

            // Who issued it
            "orgId"            to uid,
            "orgName"          to orgName,

            // Certificate metadata
            "issuedAt"         to System.currentTimeMillis(),
            "certificateCode"  to generateCertCode(
                volunteer.volunteerId, opportunityId
            ),
            "isValid"          to true
        )

        // Step 1: Create the certificate document
        db.collection("certificates")
            .add(certificateData)
            .addOnSuccessListener { certRef ->

                val certId = certRef.id

                // Step 2: Mark the booking as having a certificate
                // so the UI shows the "Issued" badge
                db.collection("bookings")
                    .document(volunteer.bookingId)
                    .update(
                        "hasCertificate", true,
                        "certificateId",  certId
                    )
                    .addOnSuccessListener {

                        // Step 3: Add the certificate to the
                        // volunteer's service portfolio so it
                        // appears in their profile in the app
                        db.collection("users")
                            .document(volunteer.volunteerId)
                            .collection("portfolio")
                            .document(certId)
                            .set(certificateData)
                            .addOnSuccessListener {

                                // Update local state immediately
                                volunteers = volunteers.map {
                                    if (it.bookingId == volunteer.bookingId)
                                        it.copy(
                                            hasCertificate = true,
                                            certificateId  = certId
                                        )
                                    else it
                                }

                                issuingId      = null
                                celebrationName = volunteer.volunteerName
                                showCelebration = true
                                successMessage = "Certificate issued to " +
                                        "${volunteer.volunteerName}! 🎉"

                                scope.launch {
                                    delay(4000)
                                    successMessage = ""
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        issuingId     = null
                        firebaseError = "Failed to update booking: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                issuingId     = null
                firebaseError = "Failed to issue certificate: ${e.message}"
            }
    }

    // ── Certificate Preview Dialog ────────────────────────────
    if (showPreview && previewVolunteer != null) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            containerColor   = WhiteBg,
            shape            = RoundedCornerShape(24.dp),
            title            = {
                Text(
                    text = "Certificate Preview",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                // Visual certificate preview card inside the dialog
                CertificatePreviewCard(
                    volunteerName    = previewVolunteer!!.volunteerName,
                    opportunityTitle = opportunityTitle,
                    orgName          = orgName,
                    date             = opportunityDate,
                    hours            = previewVolunteer!!.hoursLogged,
                    certCode         = if (previewVolunteer!!.hasCertificate)
                        previewVolunteer!!.certificateId.take(8).uppercase()
                    else "PREVIEW",
                    shimmerOffset    = shimmerOffset.value
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPreview = false },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Close",
                        color = SecondaryGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // ── Root UI ───────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4EE))
    ) {

        // ── Confetti overlay ───────────────────────────────────
        // Draws animated confetti particles over the whole screen
        // when a certificate is successfully issued
        if (showCelebration) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        particles.forEach { p ->
                            val yPos = (p.y + particleFall.value * 120f)
                                .coerceIn(0f, 100f)
                            drawCircle(
                                color  = p.color.copy(
                                    alpha = (1f - particleFall.value).coerceIn(0f, 1f)
                                ),
                                radius = p.size,
                                center = Offset(
                                    x = size.width * (p.x / 100f),
                                    y = size.height * (yPos / 100f)
                                )
                            )
                            // Also draw small squares for variety
                            drawRect(
                                color  = p.color.copy(
                                    alpha = (0.7f - particleFall.value)
                                        .coerceIn(0f, 0.7f)
                                ),
                                topLeft = Offset(
                                    x = size.width * ((p.x + 5f) / 100f),
                                    y = size.height * ((yPos + 3f) / 100f)
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    p.size * 1.5f, p.size * 1.5f
                                )
                            )
                        }
                    }
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Rich Header ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkOlive, PrimaryOlive)
                        )
                    )
                    .drawBehind {
                        // Decorative pattern
                        drawDecorativePattern(this)
                    }
                    .padding(
                        start = 8.dp, end = 20.dp,
                        top = 48.dp, bottom = 24.dp
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(headerAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Back row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = SecondaryGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Issue Certificates",
                                color = SecondaryGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Recognise your volunteers' hard work",
                                color = SecondaryGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Bouncing trophy icon ───────────────────
                    Box(
                        modifier = Modifier
                            .offset(y = trophyBounce.value.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glowing circle behind trophy
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            CertGold.copy(alpha = 0.3f),
                                            CertGold.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CertGold, CertGoldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = WhiteBg,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Opportunity title
                    Text(
                        text = opportunityTitle,
                        color = SecondaryGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = SecondaryGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = orgName,
                            color = SecondaryGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(SecondaryGold.copy(alpha = 0.4f))
                        )
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = SecondaryGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = opportunityDate,
                            color = SecondaryGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Stats row ──────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val total   = volunteers.size
                        val issued  = volunteers.count { it.hasCertificate }
                        val pending = total - issued

                        CertStatBox(
                            modifier = Modifier.weight(1f),
                            value    = total.toString(),
                            label    = "Eligible",
                            icon     = Icons.Default.Group,
                            color    = SecondaryGold
                        )
                        CertStatBox(
                            modifier = Modifier.weight(1f),
                            value    = issued.toString(),
                            label    = "Issued",
                            icon     = Icons.Default.CardMembership,
                            color    = CertGold
                        )
                        CertStatBox(
                            modifier = Modifier.weight(1f),
                            value    = pending.toString(),
                            label    = "Pending",
                            icon     = Icons.Default.Pending,
                            color    = AccentOrange
                        )
                    }
                }
            }

            // ── Success / Error banners ────────────────────────
            AnimatedVisibility(
                visible = successMessage.isNotEmpty(),
                enter   = slideInVertically() + fadeIn(),
                exit    = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SuccessGreen, Color(0xFF4CAF50))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CertGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = successMessage,
                            color = WhiteBg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = firebaseError.isNotEmpty(),
                enter   = slideInVertically() + fadeIn(),
                exit    = slideOutVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD32F2F))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ $firebaseError",
                        color = WhiteBg,
                        fontSize = 13.sp
                    )
                }
            }

            // ── Content ────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = PrimaryOlive,
                            strokeWidth = 3.dp,
                            modifier    = Modifier.size(48.dp)
                        )
                        Text(
                            "Loading volunteers...",
                            color    = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (volunteers.isEmpty()) {
                // Rich empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Layered circles empty state
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(LightGold.copy(alpha = 0.4f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(LightGold)
                            )
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(SecondaryGold, LightGold)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CardMembership,
                                    contentDescription = null,
                                    tint = PrimaryOlive,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Text(
                            text = "No eligible volunteers yet",
                            color = TextDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Only volunteers who have completed\n" +
                                    "this activity will appear here.\n" +
                                    "Mark bookings as completed first.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        // Decorative dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(PrimaryOlive, AccentOrange, CertGold).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(c.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(contentAlpha.value)
                        .offset(y = contentSlide.value.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Info card at top
                    item {
                        CertInfoBanner()
                    }

                    // Section label
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CertGold)
                            )
                            Text(
                                text = "${volunteers.size} volunteer" +
                                        if (volunteers.size != 1) "s" else "",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            // Issued count
                            val issued = volunteers.count { it.hasCertificate }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(LightGold)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$issued issued",
                                    color = PrimaryOlive,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Volunteer certificate cards
                    items(
                        items = volunteers,
                        key   = { it.bookingId }
                    ) { volunteer ->
                        CertificateVolunteerCard(
                            volunteer        = volunteer,
                            opportunityTitle = opportunityTitle,
                            isIssuing        = issuingId == volunteer.bookingId,
                            shimmerOffset    = shimmerOffset.value,
                            onIssue          = { issueCertificate(volunteer) },
                            onPreview        = {
                                previewVolunteer = volunteer
                                showPreview      = true
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }

        // ── Celebration overlay ────────────────────────────────
        // Full screen celebration card when cert is issued
        AnimatedVisibility(
            visible = showCelebration,
            enter   = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit    = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier  = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                shape     = RoundedCornerShape(28.dp),
                colors    = CardDefaults.cardColors(containerColor = WhiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Big trophy
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CertGold, CertGoldDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = WhiteBg,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Text(
                        text = "Certificate Issued! 🎉",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$celebrationName has received their\n" +
                                "certificate of service!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // Star row
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = CertGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGold)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "The certificate has been added to " +
                                    "$celebrationName's service portfolio " +
                                    "automatically. ✓",
                            color = PrimaryOlive,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Certificate Volunteer Card ────────────────────────────────
// The main card for each volunteer on this screen.
// Has a shimmer effect on the gold border when hovering.
// Shows issued/pending state with different visuals.
@Composable
fun CertificateVolunteerCard(
    volunteer: CertifiableVolunteer,
    opportunityTitle: String,
    isIssuing: Boolean,
    shimmerOffset: Float,
    onIssue: () -> Unit,
    onPreview: () -> Unit
) {
    val cardBorder = if (volunteer.hasCertificate) {
        Brush.sweepGradient(
            colors = listOf(CertGold, CertGoldDark, CertGold)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(FieldBorder, FieldBorder)
        )
    }

    val avatarColors = when (
        volunteer.volunteerName.firstOrNull()?.lowercaseChar()
    ) {
        in 'a'..'e' -> listOf(PrimaryOlive, Color(0xFF8FA038))
        in 'f'..'j' -> listOf(AccentOrange, Color(0xFFFF8A65))
        in 'k'..'o' -> listOf(SoftBlue, Color(0xFF42A5F5))
        in 'p'..'t' -> listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8))
        else         -> listOf(DarkOlive, PrimaryOlive)
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .border(
                width = if (volunteer.hasCertificate) 2.dp else 1.dp,
                brush = cardBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (volunteer.hasCertificate)
                LightGold.copy(alpha = 0.3f) else WhiteBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Shimmer gold bar at top for issued certificates
            if (volunteer.hasCertificate) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CertGoldDark,
                                    CertGold,
                                    CertGoldDark
                                ),
                                startX = shimmerOffset - 200f,
                                endX   = shimmerOffset + 200f
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(FieldBorder)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Volunteer info row ─────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gradient avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = avatarColors)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = volunteer.volunteerName
                                .firstOrNull()?.uppercaseChar()?.toString() ?: "V",
                            color = WhiteBg,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = volunteer.volunteerName,
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = volunteer.volunteerEmail,
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        // Hours badge
                        if (volunteer.hoursLogged > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SoftBlue.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = SoftBlue,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "${volunteer.hoursLogged} hours completed",
                                            color = SoftBlue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Certificate status icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (volunteer.hasCertificate)
                                    Brush.linearGradient(
                                        colors = listOf(CertGold, CertGoldDark)
                                    )
                                else Brush.linearGradient(
                                    colors = listOf(FieldBorder, FieldBorder)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (volunteer.hasCertificate)
                                Icons.Default.CardMembership
                            else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (volunteer.hasCertificate) WhiteBg else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // ── Certificate status banner ──────────────────
                if (volunteer.hasCertificate) {
                    // Issued state — gold banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        CertGold.copy(alpha = 0.15f),
                                        CertGoldDark.copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                CertGold.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = CertGoldDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Certificate Issued ✓",
                                    color = CertGoldDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Added to volunteer's service portfolio",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Preview button for issued cert
                    OutlinedButton(
                        onClick = onPreview,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        border   = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                colors = listOf(CertGold, CertGoldDark)
                            )
                        )
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CertGoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preview Certificate",
                            color = CertGoldDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {
                    // Not yet issued state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3EE))
                            .border(
                                1.dp,
                                AccentOrange.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Certificate not yet issued. " +
                                        "Tap the button below to recognise " +
                                        "this volunteer's contribution.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Issue button
                    Button(
                        onClick  = onIssue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brush.horizontalGradient(
                                colors = listOf(PrimaryOlive, DarkOlive)
                            ).let { PrimaryOlive },
                            disabledContainerColor = PrimaryOlive.copy(alpha = 0.5f)
                        ),
                        enabled = !isIssuing
                    ) {
                        if (isIssuing) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = SecondaryGold,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CardMembership,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Issue Certificate",
                                    color = SecondaryGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = CertGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Certificate Preview Card ──────────────────────────────────
// A visual representation of what the certificate looks like.
// Shown inside the preview dialog.
@Composable
fun CertificatePreviewCard(
    volunteerName: String,
    opportunityTitle: String,
    orgName: String,
    date: String,
    hours: Int,
    certCode: String,
    shimmerOffset: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkOlive, PrimaryOlive)
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(CertGold, CertGoldDark, CertGold),
                    startX = shimmerOffset - 200f,
                    endX   = shimmerOffset + 200f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = CertGold,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "CERTIFICATE OF SERVICE",
                    color = CertGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = CertGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Divider in gold
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CertGold.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Text(
                text = "This certifies that",
                color = SecondaryGold.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic
            )

            Text(
                text = volunteerName,
                color = SecondaryGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "has successfully completed",
                color = SecondaryGold.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic
            )

            Text(
                text = opportunityTitle,
                color = SecondaryGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // Gold divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CertGold.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CertDetail(
                    icon  = Icons.Default.Business,
                    label = "Issued by",
                    value = orgName
                )
                CertDetail(
                    icon  = Icons.Default.Schedule,
                    label = "Hours",
                    value = "${hours}h"
                )
                CertDetail(
                    icon  = Icons.Default.CalendarToday,
                    label = "Date",
                    value = date
                )
            }

            // Certificate code
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WhiteBg.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "CERT #$certCode",
                    color = CertGold.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            // ServConnect branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = CertGold,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Verified by ServConnect",
                    color = SecondaryGold.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// ── Cert Detail ───────────────────────────────────────────────
// Small icon+label+value column inside the certificate card
@Composable
fun CertDetail(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CertGold.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = SecondaryGold.copy(alpha = 0.5f),
            fontSize = 9.sp
        )
        Text(
            text = value,
            color = SecondaryGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Cert Info Banner ──────────────────────────────────────────
// Explains how certificates work at the top of the list
@Composable
fun CertInfoBanner() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = LightGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CertGold, CertGoldDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = WhiteBg,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "About Certificates",
                    color = PrimaryOlive,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Certificates are issued per activity and appear " +
                            "in the volunteer's service portfolio inside the app. " +
                            "Each certificate includes your organization's name, " +
                            "the activity, hours completed and a unique code.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ── Cert Stat Box ─────────────────────────────────────────────
// Stat box in the header — eligible, issued, pending counts
@Composable
fun CertStatBox(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteBg.copy(alpha = 0.12f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            color = SecondaryGold,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = SecondaryGold.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Decorative pattern for header ────────────────────────────
fun drawDecorativePattern(scope: DrawScope) {
    val white = Color.White.copy(alpha = 0.04f)
    listOf(
        Offset(scope.size.width * 0.85f, scope.size.height * 0.1f) to 90f,
        Offset(scope.size.width * 0.08f, scope.size.height * 0.5f) to 60f,
        Offset(scope.size.width * 0.92f, scope.size.height * 0.75f) to 45f,
        Offset(scope.size.width * 0.2f,  scope.size.height * 0.85f) to 35f,
    ).forEach { (pos, r) ->
        scope.drawCircle(color = white, radius = r, center = pos)
    }
    // Draw subtle arc in corner
    scope.drawArc(
        color      = Color.White.copy(alpha = 0.03f),
        startAngle = 180f,
        sweepAngle = 90f,
        useCenter  = false,
        topLeft    = Offset(scope.size.width * 0.6f, -50f),
        size       = androidx.compose.ui.geometry.Size(200f, 200f),
        style      = Stroke(width = 40f)
    )
}

// ── Certificate Code Generator ────────────────────────────────
// Generates a unique cert code from volunteer + opportunity IDs
fun generateCertCode(volunteerId: String, opportunityId: String): String {
    val combined = "${volunteerId.take(4)}${opportunityId.take(4)}" +
            System.currentTimeMillis().toString().takeLast(4)
    return combined.uppercase()
}

@Preview(showBackground = true)
@Composable
fun IssueCertificateScreenPreview() {
    IssueCertificateScreen(
        navController = rememberNavController(),
        opportunityId = "preview_id"
    )
}