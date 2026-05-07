package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.maryjoy.servconnect.R
import com.maryjoy.servconnect.ui.screens.navigation.*
import com.maryjoy.servconnect.ui.screens.shared.auth.SectionLabel
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
private val FieldBorder   = Color(0xFFE0E0E0)

// ── Data Classes ──────────────────────────────────────────────
// Represents one opportunity posted by the org
data class OpportunityItem(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = "",
    val slotsTotal: Int = 0,
    val slotsBooked: Int = 0,
    val status: String = "open",  // open / full / completed
    val type: String = "volunteer" // volunteer / community
)

// Represents one booking made by a volunteer
data class BookingItem(
    val id: String = "",
    val volunteerName: String = "",
    val opportunityTitle: String = "",
    val date: String = "",
    val status: String = "confirmed" // confirmed / pending / cancelled
)

// Represents one message preview
data class MessagePreview(
    val id: String = "",
    val senderName: String = "",
    val preview: String = "",
    val time: String = "",
    val isRead: Boolean = false
)

@Composable
fun OrgDashboardScreen(navController: NavController) {

    // ── State ─────────────────────────────────────────────────
    // Org profile info fetched from Firestore on load
    var orgName       by remember { mutableStateOf("") }
    var isVerified    by remember { mutableStateOf(false) }
    var selectedTab   by remember { mutableIntStateOf(0) }

    // Stats fetched from Firestore
    var totalOpportunities by remember { mutableIntStateOf(0) }
    var totalVolunteers    by remember { mutableIntStateOf(0) }
    var totalHours         by remember { mutableIntStateOf(0) }
    var pendingBookings    by remember { mutableIntStateOf(0) }

    // Lists fetched from Firestore
    var opportunities  by remember { mutableStateOf<List<OpportunityItem>>(emptyList()) }
    var bookings       by remember { mutableStateOf<List<BookingItem>>(emptyList()) }
    var messages       by remember { mutableStateOf<List<MessagePreview>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }

    // ── Tab definitions ───────────────────────────────────────
    val tabs = listOf("Overview", "Opportunities", "Bookings", "Messages")

    // ── Fetch all data on screen load ─────────────────────────
    // We pull everything the org needs in one go when the
    // dashboard opens. Each piece comes from a different
    // Firestore collection.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect

        val db = FirebaseFirestore.getInstance()

        // 1. Fetch org profile — name and verification badge
        db.collection("organizations")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                orgName    = doc.getString("orgName")   ?: "Organization"
                isVerified = doc.getBoolean("isVerified") ?: false
            }

        // 2. Fetch this org's posted opportunities
        // We filter by the org's uid so we only get their posts
        db.collection("opportunities")
            .whereEqualTo("orgId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                totalOpportunities = snapshot.size()

                // Map Firestore documents to our data class
                opportunities = snapshot.documents.map { doc ->
                    OpportunityItem(
                        id           = doc.id,
                        title        = doc.getString("title")       ?: "",
                        date         = doc.getString("date")        ?: "",
                        location     = doc.getString("location")    ?: "",
                        slotsTotal   = (doc.getLong("slotsTotal")   ?: 0).toInt(),
                        slotsBooked  = (doc.getLong("slotsBooked")  ?: 0).toInt(),
                        status       = doc.getString("status")      ?: "open",
                        type         = doc.getString("type")        ?: "volunteer"
                    )
                }
            }

        // 3. Fetch bookings for this org's opportunities
        db.collection("bookings")
            .whereEqualTo("orgId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                // Count unique volunteers across all bookings
                totalVolunteers = snapshot.documents
                    .map { it.getString("volunteerId") ?: "" }
                    .distinct()
                    .size

                // Count bookings still waiting for confirmation
                pendingBookings = snapshot.documents
                    .count { it.getString("status") == "pending" }

                bookings = snapshot.documents.map { doc ->
                    BookingItem(
                        id                = doc.id,
                        volunteerName     = doc.getString("volunteerName")     ?: "",
                        opportunityTitle  = doc.getString("opportunityTitle")  ?: "",
                        date              = doc.getString("date")              ?: "",
                        status            = doc.getString("status")            ?: "confirmed"
                    )
                }
            }

        // 4. Fetch message previews for this org
        db.collection("messages")
            .whereEqualTo("orgId", uid)
            .orderBy("timestamp")
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                messages = snapshot.documents.map { doc ->
                    MessagePreview(
                        id          = doc.id,
                        senderName  = doc.getString("senderName") ?: "",
                        preview     = doc.getString("lastMessage") ?: "",
                        time        = doc.getString("time")        ?: "",
                        isRead      = doc.getBoolean("isRead")     ?: false
                    )
                }
            }

        // 5. Estimate total hours from completed bookings
        // Each booking stores the hours the volunteer logged
        db.collection("bookings")
            .whereEqualTo("orgId", uid)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { snapshot ->
                totalHours = snapshot.documents
                    .sumOf { (it.getLong("hours") ?: 0).toInt() }
                isLoading = false
            }
    }

    // ── Entrance animation ────────────────────────────────────
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    // ── UI ────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // ── Top Header ─────────────────────────────────────────
        // Olive gradient band with org name, verified badge,
        // notification bell and profile avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, PrimaryOlive)
                    )
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 48.dp,
                    bottom = 20.dp
                )
        ) {
            Column(modifier = Modifier.alpha(contentAlpha.value)) {

                // Top row — avatar + greeting + notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Org avatar circle with initials
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SecondaryGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                // Takes first letter of org name
                                // as the avatar initial
                                text = if (orgName.isNotEmpty())
                                    orgName.first().uppercaseChar().toString()
                                else "O",
                                color = PrimaryOlive,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column {
                            Text(
                                text = "Welcome back,",
                                color = SecondaryGold.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = orgName.ifEmpty { "Organization" },
                                    color = SecondaryGold,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Verified badge — only shows if
                                // admin has verified this org
                                if (isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = SecondaryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Notification bell
                    IconButton(
                        onClick = {
                            navController.navigate(ROUT_ORG_NOTIFICATIONS)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(WhiteBg.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = SecondaryGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Quick action buttons ────────────────────────
                // The two most common org actions right on the header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Post new opportunity — primary action
                    Button(
                        onClick = { navController.navigate(ROUT_POST_OPPORTUNITY) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentOrange
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = WhiteBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Post Opportunity",
                            color = WhiteBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // View org profile
                    OutlinedButton(
                        onClick = { navController.navigate(ROUT_ORG_PROFILE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, SecondaryGold.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = SecondaryGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My Profile",
                            color = SecondaryGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ── Top Tab Bar ────────────────────────────────────────
        // Scrollable tab row for the 4 main sections
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = WhiteBg,
            contentColor = PrimaryOlive,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        tabPositions[selectedTab]
                    ),
                    height = 3.dp,
                    color = AccentOrange
                )
            },
            divider = {
                HorizontalDivider(color = FieldBorder)
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == index)
                            FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index)
                            PrimaryOlive else TextMuted,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 12.dp
                        )
                    )
                }
            }
        }

        // ── Tab Content ────────────────────────────────────────
        if (isLoading) {
            // Loading state — centered spinner
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryOlive,
                    strokeWidth = 3.dp
                )
            }
        } else {
            when (selectedTab) {
                0 -> OverviewTab(
                    totalOpportunities = totalOpportunities,
                    totalVolunteers    = totalVolunteers,
                    totalHours         = totalHours,
                    pendingBookings    = pendingBookings,
                    opportunities      = opportunities.take(3),
                    bookings           = bookings.take(3),
                    navController      = navController
                )
                1 -> OpportunitiesTab(
                    opportunities = opportunities,
                    navController = navController
                )
                2 -> BookingsTab(
                    bookings      = bookings,
                    navController = navController
                )
                3 -> MessagesTab(
                    messages      = messages,
                    navController = navController
                )
            }
        }
    }
}

// ── TAB 1: Overview ───────────────────────────────────────────
// Shows stats, recent opportunities and recent bookings
// in one scrollable view — the full picture at a glance
@Composable
fun OverviewTab(
    totalOpportunities: Int,
    totalVolunteers: Int,
    totalHours: Int,
    pendingBookings: Int,
    opportunities: List<OpportunityItem>,
    bookings: List<BookingItem>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        // ── Stats grid ─────────────────────────────────────────
        item {
            SectionLabel("Your Impact", PrimaryOlive)
            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 grid of stat cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.EventNote,
                        iconColor = PrimaryOlive,
                        bgColor = LightGold,
                        value = totalOpportunities.toString(),
                        label = "Opportunities\nPosted"
                    )
                    StatCard(
                        icon = Icons.Default.Schedule,
                        iconColor = SuccessGreen,
                        bgColor = Color(0xFFE8F5E9),
                        value = "${totalHours}h",
                        label = "Total Hours\nContributed"
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.Group,
                        iconColor = AccentOrange,
                        bgColor = OrangeBg,
                        value = totalVolunteers.toString(),
                        label = "Unique\nVolunteers"
                    )
                    StatCard(
                        icon = Icons.Default.Pending,
                        iconColor = Color(0xFF1976D2),
                        bgColor = Color(0xFFE3F2FD),
                        value = pendingBookings.toString(),
                        label = "Pending\nBookings"
                    )
                }
            }
        }

        // ── Recent Opportunities ───────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Recent Opportunities", PrimaryOlive)
                Text(
                    text = "See all →",
                    color = AccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        // Switch to opportunities tab
                    }
                )
            }
        }

        if (opportunities.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.EventNote,
                    message = "No opportunities posted yet",
                    actionText = "Post your first opportunity",
                    onAction = { navController.navigate(ROUT_POST_OPPORTUNITY) }
                )
            }
        } else {
            items(opportunities) { opp ->
                OpportunityCard(
                    item = opp,
                    onClick = {
                        navController.navigate("$ROUT_OPPORTUNITY_DETAIL/${opp.id}")
                    }
                )
            }
        }

        // ── Recent Bookings ────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("Recent Bookings", AccentOrange)
                Text(
                    text = "See all →",
                    color = AccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { }
                )
            }
        }

        if (bookings.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.BookOnline,
                    message = "No bookings yet",
                    actionText = null,
                    onAction = {}
                )
            }
        } else {
            items(bookings) { booking ->
                BookingCard(item = booking, navController = navController)
            }
        }
    }
}

// ── TAB 2: Opportunities ──────────────────────────────────────
// Full list of all opportunities this org has posted
// with status chips and slot progress
@Composable
fun OpportunitiesTab(
    opportunities: List<OpportunityItem>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Post new opportunity button at top
        item {
            Button(
                onClick = { navController.navigate(ROUT_POST_OPPORTUNITY) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = WhiteBg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post New Opportunity",
                    color = WhiteBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (opportunities.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.EventNote,
                    message = "You have not posted any opportunities yet.\nTap the button above to get started.",
                    actionText = null,
                    onAction = {}
                )
            }
        } else {
            items(opportunities) { opp ->
                OpportunityCard(
                    item = opp,
                    onClick = {
                        navController.navigate("$ROUT_OPPORTUNITY_DETAIL/${opp.id}")
                    }
                )
            }
        }
    }
}

// ── TAB 3: Bookings ───────────────────────────────────────────
// Full list of all volunteer bookings across all opportunities
@Composable
fun BookingsTab(
    bookings: List<BookingItem>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionLabel("All Volunteer Bookings", PrimaryOlive)
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (bookings.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.BookOnline,
                    message = "No bookings yet. Volunteers will\nappear here once they book your opportunities.",
                    actionText = null,
                    onAction = {}
                )
            }
        } else {
            items(bookings) { booking ->
                BookingCard(item = booking, navController = navController)
            }
        }
    }
}

// ── TAB 4: Messages ───────────────────────────────────────────
// Preview list of all message threads from volunteers
@Composable
fun MessagesTab(
    messages: List<MessagePreview>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionLabel("Messages", PrimaryOlive)
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (messages.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Message,
                    message = "No messages yet. Volunteers\nwill message you after booking.",
                    actionText = null,
                    onAction = {}
                )
            }
        } else {
            items(messages) { message ->
                MessageCard(
                    item = message,
                    onClick = {
                        navController.navigate("$ROUT_ORG_MESSAGES/${message.id}")
                    }
                )
            }
        }
    }
}

// ── Reusable: Stat Card ───────────────────────────────────────
// Each card in the 2x2 stats grid. Shows an icon,
// a big number and a label below it.
@Composable
fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    value: String,
    label: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = value,
                color = TextDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

// ── Reusable: Opportunity Card ────────────────────────────────
// Shows one opportunity with title, date, location,
// a slot progress bar and a colored status chip
@Composable
fun OpportunityCard(
    item: OpportunityItem,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        "open"      -> SuccessGreen
        "full"      -> Color(0xFF1976D2)
        "completed" -> TextMuted
        else        -> TextMuted
    }
    val statusBg = when (item.status) {
        "open"      -> Color(0xFFE8F5E9)
        "full"      -> Color(0xFFE3F2FD)
        "completed" -> Color(0xFFF5F5F5)
        else        -> Color(0xFFF5F5F5)
    }
    val typeColor = if (item.type == "volunteer") PrimaryOlive else AccentOrange
    val slotProgress = if (item.slotsTotal > 0)
        item.slotsBooked.toFloat() / item.slotsTotal.toFloat()
    else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Type chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(typeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (item.type == "volunteer")
                        "🤝 Volunteer" else "🏘 Community Service",
                    color = typeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Date and location
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(item.date, color = TextMuted, fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        item.location,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Slot progress bar
            // Shows how many slots are filled out of total
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Slots filled",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${item.slotsBooked} / ${item.slotsTotal}",
                        color = if (slotProgress >= 1f) Color(0xFF1976D2)
                        else PrimaryOlive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                LinearProgressIndicator(
                    progress = { slotProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (slotProgress >= 1f) Color(0xFF1976D2)
                    else PrimaryOlive,
                    trackColor = FieldBorder
                )
            }
        }
    }
}

// ── Reusable: Booking Card ────────────────────────────────────
// Shows one volunteer booking with name, opportunity,
// date and a colored status chip
@Composable
fun BookingCard(
    item: BookingItem,
    navController: NavController
) {
    val statusColor = when (item.status) {
        "confirmed"  -> SuccessGreen
        "pending"    -> AccentOrange
        "cancelled"  -> Color(0xFFD32F2F)
        "completed"  -> TextMuted
        else         -> TextMuted
    }
    val statusBg = when (item.status) {
        "confirmed"  -> Color(0xFFE8F5E9)
        "pending"    -> OrangeBg
        "cancelled"  -> Color(0xFFFFEBEE)
        "completed"  -> Color(0xFFF5F5F5)
        else         -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Volunteer initial avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightGold),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.volunteerName.isNotEmpty())
                        item.volunteerName.first().uppercaseChar().toString()
                    else "V",
                    color = PrimaryOlive,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.volunteerName,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.opportunityTitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.date,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status.replaceFirstChar { it.uppercase() },
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Reusable: Message Card ────────────────────────────────────
// Shows one message thread preview with sender name,
// message snippet and time. Unread threads are bold.
@Composable
fun MessageCard(
    item: MessagePreview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) LightGold else WhiteBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sender avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (!item.isRead) SecondaryGold
                        else FieldBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.senderName.isNotEmpty())
                        item.senderName.first().uppercaseChar().toString()
                    else "V",
                    color = PrimaryOlive,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.senderName,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = if (!item.isRead) FontWeight.Bold
                    else FontWeight.SemiBold
                )
                Text(
                    text = item.preview,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (!item.isRead) FontWeight.Medium
                    else FontWeight.Normal
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.time,
                    color = TextMuted,
                    fontSize = 10.sp
                )
                // Unread dot indicator
                if (!item.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentOrange)
                    )
                }
            }
        }
    }
}

// ── Reusable: Empty State Card ────────────────────────────────
// Shown in any tab when there is no data yet.
// Has an icon, message and optional action button.
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LightGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryOlive,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = message,
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            if (actionText != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionText,
                        color = AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Reusable: Section Label ───────────────────────────────────
// Olive left-border section title used throughout the screen
@Composable
fun DashboardSectionLabel(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrgDashboardScreenPreview() {
    OrgDashboardScreen(rememberNavController())
}