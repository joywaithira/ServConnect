package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.google.firebase.firestore.FirebaseFirestore
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
private val FieldBorder   = Color(0xFFE0E0E0)
private val SuccessGreen  = Color(0xFF388E3C)
private val SoftBlue      = Color(0xFF1976D2)
private val SoftPurple    = Color(0xFF7B1FA2)

// ── Volunteer Full Profile Data Class ─────────────────────────
data class VolunteerProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val location: String = "",
    val interests: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val totalHours: Int = 0,
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val createdAt: Long = 0L
)

// ── Past Activity Data Class ──────────────────────────────────
data class VolunteerActivity(
    val title: String = "",
    val orgName: String = "",
    val date: String = "",
    val hours: Int = 0,
    val status: String = "completed",
    val hasCertificate: Boolean = false
)

@Composable
fun VolunteerProfileViewScreen(
    navController: NavController,
    volunteerId: String
) {
    val scope = rememberCoroutineScope()

    // ── State ─────────────────────────────────────────────────
    var profile        by remember { mutableStateOf<VolunteerProfile?>(null) }
    var activities     by remember { mutableStateOf<List<VolunteerActivity>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }
    var firebaseError  by remember { mutableStateOf("") }
    var selectedTab    by remember { mutableIntStateOf(0) }
    var showRateDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableFloatStateOf(0f) }
    var ratingSuccess  by remember { mutableStateOf("") }

    val tabs = listOf("Profile", "Activity", "Skills")

    // ── Entrance animations ───────────────────────────────────
    val headerAlpha   = remember { Animatable(0f) }
    val avatarScale   = remember { Animatable(0f) }
    val contentAlpha  = remember { Animatable(0f) }
    val contentSlide  = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(200)
            avatarScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            )
        }
        launch {
            delay(400)
            contentAlpha.animateTo(1f, tween(500))
            contentSlide.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }
    }

    // ── Rotating ring animation for avatar ────────────────────
    val ringRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        ringRotation.animateTo(
            targetValue   = 360f,
            animationSpec = infiniteRepeatable(
                animation  = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // ── Fetch volunteer profile ───────────────────────────────
    // Pulls the volunteer's full profile from the "users"
    // collection using their uid. Then separately fetches
    // their completed bookings to build the activity history.
    LaunchedEffect(volunteerId) {
        val db = FirebaseFirestore.getInstance()

        // Fetch volunteer profile document
        db.collection("users")
            .document(volunteerId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    profile = VolunteerProfile(
                        uid           = doc.id,
                        fullName      = doc.getString("fullName")     ?: "Volunteer",
                        email         = doc.getString("email")        ?: "",
                        location      = doc.getString("location")     ?: "",
                        interests     = (doc.get("interests") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        skills        = (doc.get("skills") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        totalHours    = (doc.getLong("totalHours")    ?: 0).toInt(),
                        averageRating = doc.getDouble("averageRating") ?: 0.0,
                        totalRatings  = (doc.getLong("totalRatings")  ?: 0).toInt(),
                        createdAt     = doc.getLong("createdAt")      ?: 0L
                    )
                }

                // Fetch this volunteer's completed activities
                // from the bookings collection. We show their
                // history to the org so they can assess reliability.
                db.collection("bookings")
                    .whereEqualTo("volunteerId", volunteerId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        activities = snapshot.documents.map { b ->
                            VolunteerActivity(
                                title          = b.getString("opportunityTitle") ?: "",
                                orgName        = b.getString("orgName")          ?: "",
                                date           = b.getString("date")             ?: "",
                                hours          = (b.getLong("hours")             ?: 0).toInt(),
                                status         = b.getString("status")           ?: "completed",
                                hasCertificate = b.getBoolean("hasCertificate") ?: false
                            )
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        firebaseError = "Failed to load activities: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load profile: ${e.message}"
                isLoading = false
            }
    }

    // ── Save overall rating for volunteer ─────────────────────
    fun saveRating(rating: Float) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(volunteerId)
            .update(
                "averageRating", rating.toDouble(),
                "totalRatings",  (profile?.totalRatings ?: 0) + 1
            )
            .addOnSuccessListener {
                profile = profile?.copy(averageRating = rating.toDouble())
                ratingSuccess = "Rating saved successfully!"
                scope.launch {
                    delay(2000)
                    ratingSuccess = ""
                }
            }
    }

    // ── Rate Dialog ───────────────────────────────────────────
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            containerColor   = WhiteBg,
            shape            = RoundedCornerShape(24.dp),
            icon             = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(LightGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Rate ${profile?.fullName ?: "Volunteer"}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How would you rate this volunteer's\noverall performance?",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    // 5 star row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) { index ->
                            val starValue = (index + 1).toFloat()
                            Icon(
                                imageVector = if (starValue <= selectedRating)
                                    Icons.Default.Star else Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = if (starValue <= selectedRating)
                                    SecondaryGold else FieldBorder,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedRating = starValue }
                            )
                        }
                    }
                    if (selectedRating > 0f) {
                        Text(
                            text = when (selectedRating.toInt()) {
                                1 -> "Poor"
                                2 -> "Fair"
                                3 -> "Good"
                                4 -> "Very Good"
                                5 -> "Excellent!"
                                else -> ""
                            },
                            color = PrimaryOlive,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedRating > 0f) {
                            saveRating(selectedRating)
                            showRateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedRating > 0f
                ) {
                    Text(
                        "Save Rating",
                        color = SecondaryGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showRateDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, FieldBorder
                    )
                ) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4EE))
    ) {
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
                        color = PrimaryOlive,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Loading profile...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── Rich Header ────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(DarkOlive, PrimaryOlive)
                                )
                            )
                            .drawBehind {
                                // Decorative circles on header
                                listOf(
                                    Offset(size.width * 0.88f, size.height * 0.1f) to 80f,
                                    Offset(size.width * 0.05f, size.height * 0.3f) to 50f,
                                    Offset(size.width * 0.92f, size.height * 0.7f) to 40f,
                                    Offset(size.width * 0.15f, size.height * 0.8f) to 60f,
                                ).forEach { (pos, r) ->
                                    drawCircle(
                                        color  = Color.White.copy(alpha = 0.04f),
                                        radius = r,
                                        center = pos
                                    )
                                }
                            }
                            .padding(bottom = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(headerAlpha.value)
                        ) {

                            // Back button row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 8.dp, end = 20.dp,
                                        top = 48.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = SecondaryGold
                                    )
                                }

                                // Message + Rate quick actions in header
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Message button
                                    IconButton(
                                        onClick = { /* TODO: navigate to messages */ },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(WhiteBg.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            Icons.Default.Message,
                                            contentDescription = "Message",
                                            tint = SecondaryGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Rate button
                                    IconButton(
                                        onClick = { showRateDialog = true },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AccentOrange.copy(alpha = 0.8f))
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Rate",
                                            tint = WhiteBg,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ── Avatar section ─────────────────
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Avatar with rotating ring
                                Box(
                                    modifier = Modifier
                                        .scale(avatarScale.value),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Rotating gradient ring
                                    Box(
                                        modifier = Modifier
                                            .size(108.dp)
                                            .rotate(ringRotation.value)
                                            .border(
                                                width = 2.5.dp,
                                                brush = Brush.sweepGradient(
                                                    colors = listOf(
                                                        SecondaryGold,
                                                        SecondaryGold.copy(alpha = 0.1f),
                                                        AccentOrange,
                                                        SecondaryGold.copy(alpha = 0.1f),
                                                        SecondaryGold
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                    )

                                    // Avatar circle with gradient
                                    val avatarColors = when (
                                        profile?.fullName?.firstOrNull()?.lowercaseChar()
                                    ) {
                                        in 'a'..'e' -> listOf(PrimaryOlive, Color(0xFF8FA038))
                                        in 'f'..'j' -> listOf(AccentOrange, Color(0xFFFF8A65))
                                        in 'k'..'o' -> listOf(SoftBlue, Color(0xFF42A5F5))
                                        in 'p'..'t' -> listOf(SoftPurple, Color(0xFFBA68C8))
                                        else         -> listOf(DarkOlive, PrimaryOlive)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(colors = avatarColors)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile?.fullName
                                                ?.firstOrNull()
                                                ?.uppercaseChar()
                                                ?.toString() ?: "V",
                                            color = WhiteBg,
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                // Name + location
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = profile?.fullName ?: "Volunteer",
                                        color = SecondaryGold,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.3.sp
                                    )

                                    if (profile?.location?.isNotBlank() == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = SecondaryGold.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = profile?.location ?: "",
                                                color = SecondaryGold.copy(alpha = 0.7f),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    // Star rating display
                                    if ((profile?.averageRating ?: 0.0) > 0.0) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            repeat(5) { index ->
                                                val filled =
                                                    (index + 1) <= (profile?.averageRating
                                                        ?.toInt() ?: 0)
                                                Icon(
                                                    imageVector = if (filled)
                                                        Icons.Default.Star
                                                    else Icons.Default.StarOutline,
                                                    contentDescription = null,
                                                    tint = if (filled)
                                                        SecondaryGold
                                                    else SecondaryGold.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = String.format(
                                                    "%.1f", profile?.averageRating ?: 0.0
                                                ) + " (${profile?.totalRatings} ratings)",
                                                color = SecondaryGold.copy(alpha = 0.75f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                // ── Impact stat strip ──────────
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VolunteerStatPill(
                                        modifier = Modifier.weight(1f),
                                        icon     = Icons.Default.Schedule,
                                        value    = "${profile?.totalHours ?: 0}h",
                                        label    = "Hours",
                                        color    = SecondaryGold
                                    )
                                    VolunteerStatPill(
                                        modifier = Modifier.weight(1f),
                                        icon     = Icons.Default.EventAvailable,
                                        value    = activities
                                            .count { it.status == "completed" }
                                            .toString(),
                                        label    = "Activities",
                                        color    = SecondaryGold
                                    )
                                    VolunteerStatPill(
                                        modifier = Modifier.weight(1f),
                                        icon     = Icons.Default.CardMembership,
                                        value    = activities
                                            .count { it.hasCertificate }
                                            .toString(),
                                        label    = "Certificates",
                                        color    = SecondaryGold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tab bar ────────────────────────────────────
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor   = WhiteBg,
                        contentColor     = PrimaryOlive,
                        indicator        = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(
                                    tabPositions[selectedTab]
                                ),
                                height = 3.dp,
                                color  = AccentOrange
                            )
                        },
                        divider = { HorizontalDivider(color = FieldBorder) }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick  = { selectedTab = index }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index)
                                        FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index)
                                        PrimaryOlive else TextMuted,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp, vertical = 12.dp
                                    )
                                )
                            }
                        }
                    }
                }

                // ── Success banner ─────────────────────────────
                if (ratingSuccess.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SuccessGreen)
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = WhiteBg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    ratingSuccess,
                                    color = WhiteBg,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // ── Tab content ────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(contentAlpha.value)
                            .offset(y = contentSlide.value.dp)
                    ) {
                        when (selectedTab) {
                            0 -> profile?.let { ProfileTab(it) }
                            1 -> ActivityTab(activities)
                            2 -> profile?.let { SkillsTab(it) }
                        }
                    }
                }
            }
        }
    }
}

// ── TAB 1: Profile ────────────────────────────────────────────
// Basic info, contact details and interests overview
@Composable
fun ProfileTab(profile: VolunteerProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Contact info card
        VolunteerInfoCard(
            icon  = Icons.Default.Person,
            title = "Contact Information",
            color = PrimaryOlive,
            bgColor = LightGold
        ) {
            VolunteerInfoRow(
                icon  = Icons.Default.Email,
                label = "Email",
                value = profile.email,
                color = PrimaryOlive
            )
            VolunteerInfoRow(
                icon  = Icons.Default.LocationOn,
                label = "Location",
                value = profile.location.ifBlank { "Not specified" },
                color = PrimaryOlive
            )
            VolunteerInfoRow(
                icon  = Icons.Default.CalendarToday,
                label = "Member since",
                value = if (profile.createdAt > 0L) {
                    val sdf = java.text.SimpleDateFormat(
                        "MMMM yyyy", java.util.Locale.getDefault()
                    )
                    sdf.format(java.util.Date(profile.createdAt))
                } else "Unknown",
                color = PrimaryOlive
            )
        }

        // Rating overview card
        VolunteerInfoCard(
            icon    = Icons.Default.Star,
            title   = "Reputation",
            color   = AccentOrange,
            bgColor = Color(0xFFFFF3EE)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReputationStat(
                    value = String.format("%.1f", profile.averageRating),
                    label = "Avg Rating",
                    icon  = Icons.Default.Star,
                    color = SecondaryGold
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(FieldBorder)
                )
                ReputationStat(
                    value = profile.totalRatings.toString(),
                    label = "Reviews",
                    icon  = Icons.Default.RateReview,
                    color = AccentOrange
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(FieldBorder)
                )
                ReputationStat(
                    value = "${profile.totalHours}h",
                    label = "Volunteered",
                    icon  = Icons.Default.Schedule,
                    color = PrimaryOlive
                )
            }

            // Visual star bar
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val filled = (index + 1) <= profile.averageRating.toInt()
                    Icon(
                        imageVector = if (filled) Icons.Default.Star
                        else Icons.Default.StarOutline,
                        contentDescription = null,
                        tint = if (filled) SecondaryGold else FieldBorder,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1f", profile.averageRating),
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Interests card
        if (profile.interests.isNotEmpty()) {
            VolunteerInfoCard(
                icon    = Icons.Default.Favorite,
                title   = "Volunteer Interests",
                color   = SoftPurple,
                bgColor = Color(0xFFF3E5F5)
            ) {
                // Interest chips in a wrapping flow
                profile.interests.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { interest ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(LightGold)
                                    .border(
                                        1.dp,
                                        PrimaryOlive.copy(alpha = 0.3f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical   = 6.dp
                                    )
                            ) {
                                Text(
                                    text = interest,
                                    color = PrimaryOlive,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── TAB 2: Activity ───────────────────────────────────────────
// Full history of the volunteer's past activities
@Composable
fun ActivityTab(activities: List<VolunteerActivity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(LightGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = null,
                            tint = PrimaryOlive,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        text = "No activity yet",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This volunteer has not completed\nany activities yet.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            Text(
                text = "${activities.size} activities",
                color = TextMuted,
                fontSize = 12.sp
            )

            activities.forEach { activity ->
                ActivityHistoryCard(activity = activity)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── TAB 3: Skills ─────────────────────────────────────────────
// Volunteer's skills and how they map to service types
@Composable
fun SkillsTab(profile: VolunteerProfile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (profile.skills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(LightGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Construction,
                            contentDescription = null,
                            tint = PrimaryOlive,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        "No skills listed yet",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "This volunteer hasn't added\nany skills to their profile yet.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            VolunteerInfoCard(
                icon    = Icons.Default.Construction,
                title   = "Skills & Expertise",
                color   = SoftBlue,
                bgColor = Color(0xFFE3F2FD)
            ) {
                // Skills as chips
                profile.skills.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                PrimaryOlive.copy(alpha = 0.12f),
                                                LightGold
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        PrimaryOlive.copy(alpha = 0.25f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical   = 8.dp
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryOlive,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = skill,
                                        color = TextDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Skills recommendation card
            VolunteerInfoCard(
                icon    = Icons.Default.Lightbulb,
                title   = "Recommended For",
                color   = AccentOrange,
                bgColor = Color(0xFFFFF3EE)
            ) {
                Text(
                    text = "Based on this volunteer's skills and interests, " +
                            "they would be a great fit for:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Map skills to service types
                val recommendations = buildRecommendations(
                    profile.skills, profile.interests
                )
                recommendations.forEach { rec ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentOrange)
                        )
                        Text(
                            text = rec,
                            color = TextDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Activity History Card ─────────────────────────────────────
// Shows one past activity in the volunteer's history tab
@Composable
fun ActivityHistoryCard(activity: VolunteerActivity) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Colored top bar per status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        when (activity.status) {
                            "completed" -> SuccessGreen
                            "confirmed" -> SoftBlue
                            else        -> AccentOrange
                        }
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Activity type icon circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (activity.hasCertificate)
                            Icons.Default.CardMembership
                        else Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = PrimaryOlive,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activity.title,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activity.orgName,
                        color = PrimaryOlive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = activity.date,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (activity.hours > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = SoftBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${activity.hours}h",
                                    color = SoftBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (activity.status) {
                                    "completed" -> Color(0xFFE8F5E9)
                                    "confirmed" -> Color(0xFFE3F2FD)
                                    else        -> Color(0xFFFFF3EE)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = activity.status.replaceFirstChar { it.uppercase() },
                            color = when (activity.status) {
                                "completed" -> SuccessGreen
                                "confirmed" -> SoftBlue
                                else        -> AccentOrange
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Certificate badge
                    if (activity.hasCertificate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(LightGold)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Default.CardMembership,
                                    contentDescription = null,
                                    tint = PrimaryOlive,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "Cert",
                                    color = PrimaryOlive,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Volunteer Info Card ───────────────────────────────────────
// Reusable card with colored icon header for profile sections
@Composable
fun VolunteerInfoCard(
    icon: ImageVector,
    title: String,
    color: Color,
    bgColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = FieldBorder)

            content()
        }
    }
}

// ── Volunteer Info Row ────────────────────────────────────────
// Icon + label + value row inside profile cards
@Composable
fun VolunteerInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(LightGold),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Volunteer Stat Pill ───────────────────────────────────────
// Small pill in the header showing hours, activities, certs
@Composable
fun VolunteerStatPill(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(WhiteBg.copy(alpha = 0.13f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            color = SecondaryGold,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = SecondaryGold.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

// ── Reputation Stat ───────────────────────────────────────────
// Used in the rating card — big value + icon + label
@Composable
fun ReputationStat(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            color = TextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

// ── Skill → Service Recommendation mapper ────────────────────
// Maps the volunteer's skills and interests to service types
// they would be well suited for — shown in the Skills tab
fun buildRecommendations(
    skills: List<String>,
    interests: List<String>
): List<String> {
    val recs = mutableSetOf<String>()
    val all  = (skills + interests).map { it.lowercase() }

    if (all.any { "teach" in it || "educat" in it || "tutor" in it })
        recs.add("Teaching & tutoring programs")
    if (all.any { "medic" in it || "health" in it || "nurs" in it })
        recs.add("Healthcare & wellness volunteering")
    if (all.any { "child" in it || "kids" in it || "youth" in it })
        recs.add("Children's homes & youth programs")
    if (all.any { "elder" in it || "senior" in it || "aged" in it })
        recs.add("Elderly care & companionship")
    if (all.any { "cook" in it || "food" in it || "cater" in it })
        recs.add("Food distribution & community kitchens")
    if (all.any { "tech" in it || "code" in it || "it" in it || "computer" in it })
        recs.add("Digital literacy programs")
    if (all.any { "art" in it || "craft" in it || "music" in it || "creat" in it })
        recs.add("Arts & creative therapy programs")
    if (all.any { "sport" in it || "fit" in it || "athlet" in it })
        recs.add("Sports coaching & recreation programs")
    if (all.any { "environ" in it || "nature" in it || "green" in it })
        recs.add("Environmental & conservation activities")

    if (recs.isEmpty()) recs.add("General community service opportunities")
    return recs.toList()
}

@Preview(showBackground = true)
@Composable
fun VolunteerProfileViewScreenPreview() {
    VolunteerProfileViewScreen(
        navController = rememberNavController(),
        volunteerId   = "preview_id"
    )
}