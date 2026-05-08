package com.maryjoy.servconnect.ui.screens.users

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
//  Colour tokens
// ─────────────────────────────────────────────────────────────────────────────

private val GreenPrimary  = Color(0xFF1A6B3C)
private val GreenLight    = Color(0xFF2E9B5C)
private val GreenAccent   = Color(0xFF4FCB85)
private val GoldAccent    = Color(0xFFF4B942)
private val SurfaceBg     = Color(0xFFF4F7F5)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF0D2818)
private val TextSecondary = Color(0xFF5A7A6A)
private val DividerColor  = Color(0xFFE0EDE6)
private val InputBg       = Color(0xFFEEF4F0)
private val WarningRed    = Color(0xFFE53935)
private val WarningAmber  = Color(0xFFF4B942)

// ─────────────────────────────────────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────────────────────────────────────

data class OpportunityDetail(
    val id              : String,
    val title           : String,
    val organization    : String,
    val orgId           : String,
    val location        : String,
    val fullAddress     : String,
    val date            : String,
    val time            : String,
    val type            : String,
    val groupType       : String,
    val category        : String,
    val emoji           : String,
    val spotsLeft       : Int,
    val totalSpots      : Int,
    val hours           : Int,
    val description     : String,
    val skillsNeeded    : List<String>,
    val whatToBring     : List<String>,
    val cardGradient    : List<Color>,
    val orgRating       : Float,
    val isVerified      : Boolean,
    val isBookmarked    : Boolean = false,
    val status          : String = "Open"   // Open | Full | Completed
)

// ─────────────────────────────────────────────────────────────────────────────
//  Sample data (replace with Firebase fetch by id)
// ─────────────────────────────────────────────────────────────────────────────

private fun getSampleDetail(id: String) = OpportunityDetail(
    id           = id,
    title        = "Teach Reading to Kids",
    organization = "Brighter Futures Kenya",
    orgId        = "org_1",
    location     = "Westlands, Nairobi",
    fullAddress  = "Brighter Futures Centre, Ring Road Westlands, Nairobi",
    date         = "Saturday, 17 May 2025",
    time         = "9:00 AM – 12:00 PM",
    type         = "Volunteer",
    groupType    = "Individual",
    category     = "Children",
    emoji        = "🧒",
    spotsLeft    = 4,
    totalSpots   = 10,
    hours        = 3,
    description  = "Join us for a rewarding morning helping children at Brighter Futures Centre improve their reading and comprehension skills. Volunteers will work one-on-one or in small groups with children aged 6–12. No prior teaching experience is required — just patience, enthusiasm, and a love for helping young minds grow.\n\nThis is a great opportunity to give back to your community while building meaningful relationships with the children.",
    skillsNeeded = listOf("Patience", "Communication", "English literacy", "Child care"),
    whatToBring  = listOf("National ID / Student ID", "Comfortable clothes", "Water bottle", "Positive energy 😊"),
    cardGradient = listOf(Color(0xFF1A6B3C), Color(0xFF2E9B5C)),
    orgRating    = 4.7f,
    isVerified   = true,
    status       = "Open"
)

// ─────────────────────────────────────────────────────────────────────────────
//  Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OpportunityDetailScreen(
    opportunityId   : String,
    onNavigateBack  : () -> Unit,
    onBookingSuccess: () -> Unit,
    onViewOrgProfile: (String) -> Unit,
    onMessageClick  : (String, String) -> Unit
) {
    // In production: val detail by viewModel.getDetail(opportunityId).collectAsState()
    val detail            = getSampleDetail(opportunityId)
    val scrollState       = rememberScrollState()
    var isBookmarked      by remember { mutableStateOf(detail.isBookmarked) }
    var showBooking       by remember { mutableStateOf(false) }
    var showInvite        by remember { mutableStateOf(false) }
    var showShareSnackbar by remember { mutableStateOf(false) }
    val clipboardManager  = LocalClipboardManager.current

    // Collapse top bar on scroll
    val isScrolled by remember { derivedStateOf { scrollState.value > 200 } }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceBg)) {

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            // Hero banner
            DetailHeroBanner(
                detail       = detail,
                isBookmarked = isBookmarked,
                onBookmark   = { isBookmarked = !isBookmarked },
                onBack       = onNavigateBack
            )

            // Status + spots bar
            StatusSpotsSection(detail = detail)

            // Key info grid
            KeyInfoGrid(detail = detail)

            HorizontalDivider(color = DividerColor, thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp))

            // Description
            DescriptionSection(description = detail.description)

            HorizontalDivider(color = DividerColor, thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp))

            // Skills needed
            SkillsSection(skills = detail.skillsNeeded)

            HorizontalDivider(color = DividerColor, thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp))

            // What to bring
            WhatToBringSection(items = detail.whatToBring)

            HorizontalDivider(color = DividerColor, thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 20.dp))

            // Organisation card
            OrganisationCard(
                detail      = detail,
                onViewOrg   = { onViewOrgProfile(detail.orgId) }
            )

            // QR Check-in info nudge
            QrCheckinNudge()

            // Invite friends / share row
            InviteShareRow(
                onInvite = { showInvite = true },
                onShare  = {
                    clipboardManager.setText(AnnotatedString("servconnect://opportunity/${detail.id}"))
                    showShareSnackbar = true
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        // ── Collapsed top bar ──
        AnimatedVisibility(
            visible = isScrolled,
            enter   = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit    = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            CollapsedTopBar(
                title       = detail.title,
                onBack      = onNavigateBack,
                isBookmarked = isBookmarked,
                onBookmark  = { isBookmarked = !isBookmarked }
            )
        }

        // ── Transparent back button when hero visible ──
        AnimatedVisibility(
            visible  = !isScrolled,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 12.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.30f), CircleShape)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // ── Fixed bottom CTA ──
        BottomCta(
            detail   = detail,
            modifier = Modifier.align(Alignment.BottomCenter),
            onBook   = { showBooking = true },
            onMessage = { onMessageClick(detail.orgId, detail.organization) }
        )

        // ── Booking sheet ──
        if (showBooking) {
            BookingBottomSheet(
                detail    = detail,
                onConfirm = {
                    showBooking = false
                    onBookingSuccess()
                },
                onDismiss = { showBooking = false }
            )
        }

        // ── Invite friends sheet ──
        if (showInvite) {
            InviteFriendsSheet(
                opportunityTitle = detail.title,
                onDismiss        = { showInvite = false },
                onCopyLink       = {
                    clipboardManager.setText(AnnotatedString("servconnect://opportunity/${detail.id}"))
                    showInvite = false
                    showShareSnackbar = true
                }
            )
        }

        // ── Snackbar for copy link ──
        AnimatedVisibility(
            visible  = showShareSnackbar,
            enter    = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit     = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = GreenPrimary,
                contentColor   = Color.White
            ) { Text("Link copied to clipboard!") }
        }

        LaunchedEffect(showShareSnackbar) {
            if (showShareSnackbar) {
                delay(2000)
                showShareSnackbar = false
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetailHeroBanner(
    detail       : OpportunityDetail,
    isBookmarked : Boolean,
    onBookmark   : () -> Unit,
    onBack       : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Brush.linearGradient(detail.cardGradient))
    ) {
        // Background gradient and content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) { Text(detail.emoji, fontSize = 14.sp); Spacer(Modifier.width(6.dp)); Text(detail.category, color = Color.White, fontSize = 13.sp) }

                // Bookmark icon
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBookmark() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                detail.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Business, "", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(detail.organization, color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.Star, "", tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${detail.orgRating}", color = Color.White, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, "", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(detail.location, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsedTopBar(
    title       : String,
    onBack      : () -> Unit,
    isBookmarked: Boolean,
    onBookmark  : () -> Unit
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onBookmark) {
                Icon(imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
    )
}

@Composable
private fun StatusSpotsSection(detail: OpportunityDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-30).dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Status", fontSize = 12.sp, color = TextSecondary)
                Text(detail.status, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = DividerColor)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Spots Left", fontSize = 12.sp, color = TextSecondary)
                Text("${detail.spotsLeft}/${detail.totalSpots}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = DividerColor)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hours", fontSize = 12.sp, color = TextSecondary)
                Text("${detail.hours}h", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun KeyInfoGrid(detail: OpportunityDetail) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Key Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoChip(Icons.Filled.CalendarToday, detail.date)
            InfoChip(Icons.Filled.Schedule, detail.time)
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoChip(Icons.Filled.People, detail.groupType)
            InfoChip(Icons.Filled.Category, detail.category)
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(InputBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, "", tint = GreenPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Text(description, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
    }
}

@Composable
private fun SkillsSection(skills: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Skills Needed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        
        // Use a simple Column of Rows to avoid FlowRow binary compatibility issues
        // which can cause NoSuchMethodError on some devices or versions.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            skills.chunked(3).forEach { rowSkills ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowSkills.forEach { skill ->
                        Chip(text = skill)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatToBringSection(items: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("What to Bring", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, "", tint = GreenAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(item, fontSize = 14.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun OrganisationCard(
    detail    : OpportunityDetail,
    onViewOrg : () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("About the Organization", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Business, "", tint = GreenPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(detail.organization, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, "", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${detail.orgRating} (${(detail.orgRating * 20).toInt()} reviews)", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    if (detail.isVerified) {
                        Icon(Icons.Filled.Verified, "", tint = GreenAccent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Verified", fontSize = 13.sp, color = GreenAccent)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onViewOrg,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenLight)
                ) {
                    Text("View Organization Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun QrCheckinNudge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .background(GreenAccent.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.QrCodeScanner, "", tint = GreenPrimary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("QR Check-in Available", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Scan QR at location to check-in", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun InviteShareRow(
    onInvite: () -> Unit,
    onShare : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OutlinedButton(
            onClick = onInvite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GreenPrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.GroupAdd, "", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Invite Friends")
        }
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = onShare,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Share, "", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Share")
        }
    }
}

@Composable
private fun BottomCta(
    detail   : OpportunityDetail,
    modifier : Modifier = Modifier,
    onBook   : () -> Unit,
    onMessage: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onMessage,
            modifier = Modifier
                .size(54.dp)
                .background(InputBg, RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, "Message", tint = GreenPrimary)
        }
        Button(
            onClick = onBook,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text("Reserve Your Spot", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingBottomSheet(
    detail    : OpportunityDetail,
    onConfirm : () -> Unit,
    onDismiss : () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    var selectedDate  by remember { mutableStateOf(detail.date) }
    var selectedSlot  by remember { mutableStateOf("9:00 AM") }
    var isGroupBooking by remember { mutableStateOf(false) }
    var groupSize      by remember { mutableStateOf(1) }
    val timeSlots      = listOf("9:00 AM", "10:00 AM", "11:00 AM")

    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication        = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .navigationBarsPadding()
                .clickable(
                    indication        = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {}
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerColor, CircleShape)
            )

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Text("Reserve Your Spot",
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("You're booking for ${detail.title}",
                    fontSize = 13.sp, color = TextSecondary)

                Spacer(Modifier.height(20.dp))

                // Summary card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBg, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(detail.cardGradient),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) { Text(detail.emoji, fontSize = 22.sp) }
                    Column {
                        Text(detail.title, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${detail.organization} · ${detail.hours}h",
                            fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Date
                SheetSection(title = "📅 Date") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputBg, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(detail.date, fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Icon(Icons.Outlined.CalendarToday, null,
                            tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Time slot
                SheetSection(title = "🕐 Time Slot") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        timeSlots.forEach { slot ->
                            val isSel = slot == selectedSlot
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) GreenPrimary else InputBg,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(1.dp,
                                        if (isSel) Color.Transparent else DividerColor,
                                        RoundedCornerShape(10.dp))
                                    .clickable { selectedSlot = slot }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(slot, fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) Color.White else TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Group booking toggle
                SheetSection(title = "👥 Booking Type") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputBg, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Group Booking",
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                color = TextPrimary)
                            Text("Book multiple spots at once",
                                fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked         = isGroupBooking,
                            onCheckedChange = { isGroupBooking = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = GreenPrimary
                            )
                        )
                    }

                    // Group size stepper
                    AnimatedVisibility(visible = isGroupBooking) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Group size",
                                fontSize = 14.sp, color = TextPrimary,
                                fontWeight = FontWeight.Medium)
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (groupSize > 1) GreenPrimary else InputBg, CircleShape)
                                        .clickable { if (groupSize > 1) groupSize-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("−", fontSize = 18.sp,
                                        color = if (groupSize > 1) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold)
                                }
                                Text(groupSize.toString(),
                                    fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (groupSize < detail.spotsLeft) GreenPrimary else InputBg,
                                            CircleShape
                                        )
                                        .clickable { if (groupSize < detail.spotsLeft) groupSize++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 18.sp,
                                        color = if (groupSize < detail.spotsLeft) Color.White
                                        else TextSecondary,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Confirm button
                Button(
                    onClick  = {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val bookingId = database.getReference("bookings").push().key
                            if (bookingId != null) {
                                val bookingData = hashMapOf(
                                    "opportunityId" to detail.id,
                                    "opportunityTitle" to detail.title,
                                    "organizationId" to detail.orgId,
                                    "organizationName" to detail.organization,
                                    "volunteerId" to currentUser.uid,
                                    "volunteerEmail" to currentUser.email,
                                    "bookingDate" to selectedDate,
                                    "bookingTimeSlot" to selectedSlot,
                                    "isGroupBooking" to isGroupBooking,
                                    "groupSize" to if (isGroupBooking) groupSize else 1,
                                    "timestamp" to ServerValue.TIMESTAMP
                                )

                                database.getReference("bookings").child(bookingId).setValue(bookingData)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(context, "Booking confirmed!", Toast.LENGTH_SHORT).show()
                                            onConfirm()
                                        } else {
                                            Toast.makeText(context, "Failed to confirm booking: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                            Log.e("BookingBottomSheet", "Failed to confirm booking", task.exception)
                                        }
                                    }
                            } else {
                                Toast.makeText(context, "Failed to generate booking ID.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "You need to be logged in to book a spot.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        "Confirm Booking${if (isGroupBooking && groupSize > 1) " · $groupSize people" else ""}",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun SheetSection(title: String, content: @Composable () -> Unit) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        color = TextSecondary, letterSpacing = 0.3.sp)
    Spacer(Modifier.height(8.dp))
    content()
}

// ─────────────────────────────────────────────────────────────────────────────
//  Invite friends sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InviteFriendsSheet(
    opportunityTitle : String,
    onDismiss        : () -> Unit,
    onCopyLink       : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication        = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .navigationBarsPadding()
                .clickable(
                    indication        = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {}
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerColor, CircleShape)
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Text("Invite Friends",
                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text("Share this opportunity with your network",
                    fontSize = 13.sp, color = TextSecondary)

                Spacer(Modifier.height(20.dp))

                // Summary card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InputBg, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4FCB85), Color(0xFF1A6B3C))),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) { Text("🤝", fontSize = 22.sp) }
                    Column {
                        Text(opportunityTitle, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Volunteer Opportunity",
                            fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Share options
                SheetSection(title = "Share via") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShareOption(Icons.Filled.ContentCopy, "Copy Link") { onCopyLink() }
                        ShareOption(Icons.Filled.Whatsapp, "WhatsApp") { /* TODO */ }
                        ShareOption(Icons.Filled.Email, "Email") { /* TODO */ }
                        ShareOption(Icons.Filled.MoreHoriz, "More") { /* TODO */ }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ShareOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(InputBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, "", tint = GreenPrimary) }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun Chip(text: String) {
    Surface(
        color = InputBg,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OpportunityDetailScreenPreview() {
    OpportunityDetailScreen(
        opportunityId = "123",
        onNavigateBack = {},
        onBookingSuccess = {},
        onViewOrgProfile = { _ -> },
        onMessageClick = { _, _ -> }
    )
}