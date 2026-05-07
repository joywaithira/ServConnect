package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_QR_CODE
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_ISSUE_CERTIFICATE
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
private val ErrorRed      = Color(0xFFD32F2F)

// ── Full Opportunity Detail Data Class ────────────────────────
// More complete than OpportunityItem — holds all fields
// including description, requirements, interests and times
data class OpportunityDetail(
    val id: String = "",
    val orgId: String = "",
    val orgName: String = "",
    val title: String = "",
    val description: String = "",
    val requirements: String = "",
    val location: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val type: String = "volunteer",
    val mode: String = "individual",
    val interests: List<String> = emptyList(),
    val slotsTotal: Int = 0,
    val slotsBooked: Int = 0,
    val status: String = "open",
    val createdAt: Long = 0L
)

// ── Booked Volunteer Data Class ───────────────────────────────
// Each volunteer who has booked this opportunity
data class BookedVolunteer(
    val bookingId: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val volunteerEmail: String = "",
    val bookingStatus: String = "confirmed",
    val checkedIn: Boolean = false,
    val hoursLogged: Int = 0
)

@Composable
fun OpportunityDetailScreen(
    navController: NavController,
    // The opportunity ID is passed from MyOpportunitiesScreen
    // via navigation so we know which doc to fetch
    opportunityId: String
) {

    // ── State ─────────────────────────────────────────────────
    var detail         by remember { mutableStateOf<OpportunityDetail?>(null) }
    var bookedVolunteers by remember { mutableStateOf<List<BookedVolunteer>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }
    var firebaseError  by remember { mutableStateOf("") }
    var selectedTab    by remember { mutableIntStateOf(0) }

    // For status update feedback
    var statusUpdateMsg by remember { mutableStateOf("") }

    val tabs = listOf("Overview", "Volunteers", "Actions")

    // ── Entrance animation ────────────────────────────────────
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    // ── Fetch opportunity detail ──────────────────────────────
    // Pulls the full opportunity document from Firestore
    // using the ID passed in from the previous screen.
    // Then separately fetches all bookings for this opportunity.
    LaunchedEffect(opportunityId) {
        val db = FirebaseFirestore.getInstance()

        // Fetch the opportunity document
        db.collection("opportunities")
            .document(opportunityId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    detail = OpportunityDetail(
                        id           = doc.id,
                        orgId        = doc.getString("orgId")        ?: "",
                        orgName      = doc.getString("orgName")      ?: "",
                        title        = doc.getString("title")        ?: "",
                        description  = doc.getString("description")  ?: "",
                        requirements = doc.getString("requirements") ?: "",
                        location     = doc.getString("location")     ?: "",
                        date         = doc.getString("date")         ?: "",
                        startTime    = doc.getString("startTime")    ?: "",
                        endTime      = doc.getString("endTime")      ?: "",
                        type         = doc.getString("type")         ?: "volunteer",
                        mode         = doc.getString("mode")         ?: "individual",
                        interests    = (doc.get("interests") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        slotsTotal   = (doc.getLong("slotsTotal")   ?: 0).toInt(),
                        slotsBooked  = (doc.getLong("slotsBooked")  ?: 0).toInt(),
                        status       = doc.getString("status")       ?: "open",
                        createdAt    = doc.getLong("createdAt")      ?: 0L
                    )
                }

                // Fetch all bookings for this opportunity
                db.collection("bookings")
                    .whereEqualTo("opportunityId", opportunityId)
                    .get()
                    .addOnSuccessListener { bookingSnapshot ->
                        bookedVolunteers = bookingSnapshot.documents.map { b ->
                            BookedVolunteer(
                                bookingId      = b.id,
                                volunteerId    = b.getString("volunteerId")    ?: "",
                                volunteerName  = b.getString("volunteerName")  ?: "",
                                volunteerEmail = b.getString("volunteerEmail") ?: "",
                                bookingStatus  = b.getString("status")         ?: "confirmed",
                                checkedIn      = b.getBoolean("checkedIn")     ?: false,
                                hoursLogged    = (b.getLong("hours")           ?: 0).toInt()
                            )
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        firebaseError = "Failed to load bookings: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load opportunity: ${e.message}"
                isLoading = false
            }
    }

    // ── Update opportunity status in Firestore ────────────────
    // Called from the Actions tab when the org manually
    // changes the status of their opportunity.
    // e.g. marking it as "completed" after the event.
    fun updateStatus(newStatus: String) {
        FirebaseFirestore.getInstance()
            .collection("opportunities")
            .document(opportunityId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Update local state so UI reflects change immediately
                detail = detail?.copy(status = newStatus)
                statusUpdateMsg = "Status updated to ${newStatus.replaceFirstChar { it.uppercase() }}"
            }
            .addOnFailureListener { e ->
                statusUpdateMsg = "Failed to update: ${e.message}"
            }
    }

    // ── UI ────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // ── Header ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, PrimaryOlive)
                    )
                )
                .padding(
                    start = 8.dp, end = 20.dp,
                    top = 48.dp, bottom = 20.dp
                )
        ) {
            Column(modifier = Modifier.alpha(contentAlpha.value)) {

                // Back + title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            text = detail?.title ?: "Opportunity Detail",
                            color = SecondaryGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (detail != null) {
                            Text(
                                text = "${detail!!.date}  •  " +
                                        "${detail!!.startTime} – ${detail!!.endTime}",
                                color = SecondaryGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Status + slot summary row in header
                if (detail != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status chip
                        val statusColor = when (detail!!.status) {
                            "open"      -> SuccessGreen
                            "full"      -> Color(0xFF1976D2)
                            "completed" -> TextMuted
                            else        -> TextMuted
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(WhiteBg.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = detail!!.status.replaceFirstChar { it.uppercase() },
                                color = SecondaryGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Slot fill info
                        Text(
                            text = "${detail!!.slotsBooked}/${detail!!.slotsTotal} slots filled",
                            color = SecondaryGold.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Type badge
                        Text(
                            text = if (detail!!.type == "volunteer")
                                "🤝 Volunteer" else "🏘 Community",
                            color = SecondaryGold.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slot progress bar in header
                    val slotProgress = if (detail!!.slotsTotal > 0)
                        detail!!.slotsBooked.toFloat() / detail!!.slotsTotal.toFloat()
                    else 0f

                    LinearProgressIndicator(
                        progress = { slotProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SecondaryGold,
                        trackColor = WhiteBg.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // ── Tab bar ────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WhiteBg,
            contentColor = PrimaryOlive,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = AccentOrange
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

        // ── Tab content ────────────────────────────────────────
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryOlive,
                    strokeWidth = 3.dp
                )
            }
        } else if (firebaseError.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firebaseError,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            when (selectedTab) {
                0 -> detail?.let {
                    OverviewDetailTab(detail = it)
                }
                1 -> VolunteersTab(
                    volunteers    = bookedVolunteers,
                    navController = navController,
                    opportunityId = opportunityId
                )
                2 -> detail?.let {
                    ActionsTab(
                        detail         = it,
                        navController  = navController,
                        opportunityId  = opportunityId,
                        statusUpdateMsg = statusUpdateMsg,
                        onUpdateStatus = { newStatus -> updateStatus(newStatus) }
                    )
                }
            }
        }
    }
}

// ── TAB 1: Overview ───────────────────────────────────────────
// Full details of the opportunity — description,
// requirements, location, schedule, interests and mode
@Composable
fun OverviewDetailTab(detail: OpportunityDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Description
        item {
            DetailCard(title = "Description", icon = Icons.Default.Description) {
                Text(
                    text = detail.description,
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Requirements
        if (detail.requirements.isNotBlank()) {
            item {
                DetailCard(title = "Requirements", icon = Icons.Default.Checklist) {
                    Text(
                        text = detail.requirements,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Schedule & Location
        item {
            DetailCard(title = "Schedule & Location", icon = Icons.Default.Schedule) {
                DetailRow(
                    icon  = Icons.Default.CalendarToday,
                    label = "Date",
                    value = detail.date
                )
                DetailRow(
                    icon  = Icons.Default.AccessTime,
                    label = "Time",
                    value = "${detail.startTime} – ${detail.endTime}"
                )
                DetailRow(
                    icon  = Icons.Default.LocationOn,
                    label = "Location",
                    value = detail.location
                )
                DetailRow(
                    icon  = if (detail.mode == "group")
                        Icons.Default.Group else Icons.Default.Person,
                    label = "Mode",
                    value = detail.mode.replaceFirstChar { it.uppercase() }
                )
            }
        }

        // Interest tags
        if (detail.interests.isNotEmpty()) {
            item {
                DetailCard(title = "Interest Tags", icon = Icons.Default.Tag) {
                    // Wrap tags in a flow layout using chunked rows
                    detail.interests.chunked(3).forEach { rowTags ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowTags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(LightGold)
                                        .padding(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                ) {
                                    Text(
                                        text = tag,
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
        }

        // Slot summary
        item {
            DetailCard(title = "Slot Summary", icon = Icons.Default.Group) {
                val slotProgress = if (detail.slotsTotal > 0)
                    detail.slotsBooked.toFloat() / detail.slotsTotal.toFloat()
                else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${detail.slotsBooked} booked",
                        color = PrimaryOlive,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${detail.slotsTotal - detail.slotsBooked} remaining",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { slotProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (slotProgress >= 1f)
                        Color(0xFF1976D2) else PrimaryOlive,
                    trackColor = FieldBorder
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${(slotProgress * 100).toInt()}% filled",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ── TAB 2: Volunteers ─────────────────────────────────────────
// List of all volunteers who have booked this opportunity.
// Shows check-in status and hours logged for each.
@Composable
fun VolunteersTab(
    volunteers: List<BookedVolunteer>,
    navController: NavController,
    opportunityId: String
) {
    if (volunteers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(LightGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = PrimaryOlive,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(
                    text = "No volunteers yet",
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Volunteers will appear here once\nthey book this opportunity.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "${volunteers.size} volunteer${if (volunteers.size != 1) "s" else ""}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            items(volunteers) { volunteer ->
                BookedVolunteerCard(
                    volunteer     = volunteer,
                    opportunityId = opportunityId,
                    navController = navController
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ── TAB 3: Actions ────────────────────────────────────────────
// Org can change the opportunity status, generate a QR code
// for check-in and issue certificates to volunteers
@Composable
fun ActionsTab(
    detail: OpportunityDetail,
    navController: NavController,
    opportunityId: String,
    statusUpdateMsg: String,
    onUpdateStatus: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Status update message feedback
        if (statusUpdateMsg.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SuccessGreen.copy(alpha = 0.08f))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✅ $statusUpdateMsg",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ── Change Status ──────────────────────────────────────
        item {
            DetailCard(title = "Update Status", icon = Icons.Default.Update) {
                Text(
                    text = "Current status: ${detail.status.replaceFirstChar { it.uppercase() }}",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Only show statuses that make sense to switch to
                val statusOptions = listOf(
                    Triple("open",      "Mark as Open",      PrimaryOlive),
                    Triple("full",      "Mark as Full",      Color(0xFF1976D2)),
                    Triple("completed", "Mark as Completed", TextMuted)
                ).filter { it.first != detail.status }

                statusOptions.forEach { (status, label, color) ->
                    OutlinedButton(
                        onClick = { onUpdateStatus(status) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, color.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = label,
                            color = color,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ── QR Code ────────────────────────────────────────────
        item {
            DetailCard(
                title = "QR Code Check-In",
                icon  = Icons.Default.QrCode
            ) {
                Text(
                    text = "Generate a QR code that volunteers scan " +
                            "on arrival to automatically start their " +
                            "session timer and log their attendance.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate("$ROUT_QR_CODE/$opportunityId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryOlive
                    )
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate QR Code",
                        color = SecondaryGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Issue Certificates ─────────────────────────────────
        item {
            DetailCard(
                title = "Issue Certificates",
                icon  = Icons.Default.CardMembership
            ) {
                Text(
                    text = "Issue verified certificates to volunteers " +
                            "who completed this activity. Certificates " +
                            "appear in their service portfolio.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate("$ROUT_ISSUE_CERTIFICATE/$opportunityId")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange
                    )
                ) {
                    Icon(
                        Icons.Default.CardMembership,
                        contentDescription = null,
                        tint = WhiteBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Issue Certificates",
                        color = WhiteBg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ── Booked Volunteer Card ─────────────────────────────────────
// Shows one volunteer's name, email, check-in status
// and hours logged. Tapping opens their profile.
@Composable
fun BookedVolunteerCard(
    volunteer: BookedVolunteer,
    opportunityId: String,
    navController: NavController
) {
    val statusColor = when (volunteer.bookingStatus) {
        "confirmed"  -> SuccessGreen
        "pending"    -> AccentOrange
        "cancelled"  -> ErrorRed
        "completed"  -> TextMuted
        else         -> TextMuted
    }
    val statusBg = when (volunteer.bookingStatus) {
        "confirmed"  -> Color(0xFFE8F5E9)
        "pending"    -> Color(0xFFFFF3EE)
        "cancelled"  -> Color(0xFFFFEBEE)
        "completed"  -> Color(0xFFF5F5F5)
        else         -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    "volunteer_profile/${volunteer.volunteerId}"
                )
            },
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (volunteer.checkedIn) PrimaryOlive
                        else LightGold
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (volunteer.volunteerName.isNotEmpty())
                        volunteer.volunteerName.first().uppercaseChar().toString()
                    else "V",
                    color = if (volunteer.checkedIn) SecondaryGold else PrimaryOlive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = volunteer.volunteerName,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = volunteer.volunteerEmail,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Check-in status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (volunteer.checkedIn)
                                Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (volunteer.checkedIn) SuccessGreen else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (volunteer.checkedIn) "Checked in" else "Not checked in",
                            color = if (volunteer.checkedIn) SuccessGreen else TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Hours logged (only shows if > 0)
                    if (volunteer.hoursLogged > 0) {
                        Text(
                            text = "•  ${volunteer.hoursLogged}h logged",
                            color = PrimaryOlive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Booking status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = volunteer.bookingStatus.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Arrow
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Detail Card wrapper ───────────────────────────────────────
// White card with a left-border section label.
// Used for each info block in the Overview tab.
@Composable
fun DetailCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryOlive,
                        modifier = Modifier.size(17.dp)
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

// ── Detail Row ────────────────────────────────────────────────
// A single labeled info row used inside DetailCard.
// e.g. "📅 Date  →  25 June 2025"
@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryOlive,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OpportunityDetailScreenPreview() {
    OpportunityDetailScreen(
        navController = rememberNavController(),
        opportunityId = "preview_id"
    )
}