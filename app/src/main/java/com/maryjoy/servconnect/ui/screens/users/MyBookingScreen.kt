package com.maryjoy.servconnect.ui.screens.volunteer

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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
private val ErrorRed      = Color(0xFFD32F2F)

// ── Data class ────────────────────────────────────────────────
data class VolunteerBooking(
    val bookingId: String = "",
    val opportunityId: String = "",
    val opportunityTitle: String = "",
    val orgName: String = "",
    val orgId: String = "",
    val location: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val type: String = "volunteer",
    val status: String = "confirmed",
    val checkedIn: Boolean = false,
    val hoursLogged: Int = 0,
    val hasCertificate: Boolean = false,
    val bookedAt: Long = 0L
)

@Composable
fun MyBookingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQrCheckIn: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // ── State ─────────────────────────────────────────────────
    var bookings        by remember { mutableStateOf<List<VolunteerBooking>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var selectedFilter  by remember { mutableStateOf("All") }
    var firebaseError   by remember { mutableStateOf("") }
    var successMessage  by remember { mutableStateOf("") }
    var cancelTargetId  by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val filters = listOf("All", "Confirmed", "Pending", "Completed", "Cancelled")

    // ── Entrance animations ───────────────────────────────────
    val headerAlpha  = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(250)
            contentAlpha.animateTo(1f, tween(600))
            contentSlide.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
        }
    }

    // ── Floating icon animation ───────────────────────────────
    // The calendar icon in the header gently floats up and down
    val iconFloat = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            iconFloat.animateTo(-6f, tween(900, easing = FastOutSlowInEasing))
            iconFloat.animateTo(0f, tween(900, easing = FastOutSlowInEasing))
        }
    }

    // ── Fetch bookings ────────────────────────────────────────
    // Pulls all bookings where volunteerId matches the
    // currently logged-in user's uid. Ordered newest first.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect

        FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("volunteerId", uid)
            .orderBy("bookedAt",
                com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                bookings = snapshot.documents.map { doc ->
                    VolunteerBooking(
                        bookingId        = doc.id,
                        opportunityId    = doc.getString("opportunityId")    ?: "",
                        opportunityTitle = doc.getString("opportunityTitle") ?: "",
                        orgName          = doc.getString("orgName")          ?: "",
                        orgId            = doc.getString("orgId")            ?: "",
                        location         = doc.getString("location")         ?: "",
                        date             = doc.getString("date")             ?: "",
                        startTime        = doc.getString("startTime")        ?: "",
                        endTime          = doc.getString("endTime")          ?: "",
                        type             = doc.getString("type")             ?: "volunteer",
                        status           = doc.getString("status")           ?: "confirmed",
                        checkedIn        = doc.getBoolean("checkedIn")       ?: false,
                        hoursLogged      = (doc.getLong("hours")             ?: 0).toInt(),
                        hasCertificate   = doc.getBoolean("hasCertificate") ?: false,
                        bookedAt         = doc.getLong("bookedAt")           ?: 0L
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load bookings: ${e.message}"
                isLoading     = false
            }
    }

    // ── Cancel booking ────────────────────────────────────────
    // Updates the booking status to "cancelled" in Firestore.
    // Also decrements the slotsBooked on the opportunity so
    // the slot becomes available for another volunteer.
    fun cancelBooking(bookingId: String, opportunityId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("bookings")
            .document(bookingId)
            .update("status", "cancelled")
            .addOnSuccessListener {
                // Update locally right away
                bookings = bookings.map {
                    if (it.bookingId == bookingId) it.copy(status = "cancelled")
                    else it
                }

                // Free up the slot on the opportunity
                db.collection("opportunities")
                    .document(opportunityId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val current = (doc.getLong("slotsBooked") ?: 1).toInt()
                        val newCount = (current - 1).coerceAtLeast(0)
                        doc.reference.update(
                            "slotsBooked", newCount,
                            "status", if (newCount < (doc.getLong("slotsTotal")
                                    ?.toInt() ?: 0)) "open" else "full"
                        )
                    }

                successMessage = "Booking cancelled successfully"
                scope.launch {
                    delay(3000)
                    successMessage = ""
                }
            }
            .addOnFailureListener { e ->
                firebaseError = "Cancel failed: ${e.message}"
            }
    }

    // ── Filtered list ─────────────────────────────────────────
    val filteredBookings = bookings.filter { booking ->
        when (selectedFilter) {
            "Confirmed"  -> booking.status == "confirmed"
            "Pending"    -> booking.status == "pending"
            "Completed"  -> booking.status == "completed"
            "Cancelled"  -> booking.status == "cancelled"
            else         -> true
        }
    }

    // ── Summary counts ────────────────────────────────────────
    val confirmedCount  = bookings.count { it.status == "confirmed" }
    val completedCount  = bookings.count { it.status == "completed" }
    val pendingCount    = bookings.count { it.status == "pending" }
    val cancelledCount  = bookings.count { it.status == "cancelled" }
    val totalHours      = bookings.sumOf { it.hoursLogged }

    // ── Cancel Dialog ─────────────────────────────────────────
    if (showCancelDialog && cancelTargetId != null) {
        val target = bookings.find { it.bookingId == cancelTargetId }
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
                cancelTargetId   = null
            },
            containerColor = WhiteBg,
            shape          = RoundedCornerShape(24.dp),
            icon           = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        tint     = ErrorRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text       = "Cancel Booking?",
                    color      = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text      = "Are you sure you want to cancel your booking for " +
                            "\"${target?.opportunityTitle}\"? " +
                            "Your slot will be released for other volunteers.",
                    color     = TextMuted,
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        target?.let {
                            cancelBooking(it.bookingId, it.opportunityId)
                        }
                        showCancelDialog = false
                        cancelTargetId   = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, Cancel", color = WhiteBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCancelDialog = false
                        cancelTargetId   = null
                    },
                    shape  = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FieldBorder)
                ) {
                    Text("Keep Booking", color = PrimaryOlive, fontWeight = FontWeight.SemiBold)
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
                        // Decorative circles
                        listOf(
                            Offset(size.width * 0.88f, size.height * 0.15f) to 80f,
                            Offset(size.width * 0.05f, size.height * 0.5f)  to 55f,
                            Offset(size.width * 0.75f, size.height * 0.85f) to 40f,
                            Offset(size.width * 0.2f,  size.height * 0.9f)  to 60f,
                        ).forEach { (pos, r) ->
                            drawCircle(
                                color  = Color.White.copy(alpha = 0.04f),
                                radius = r,
                                center = pos
                            )
                        }
                        // Subtle arc decoration
                        drawArc(
                            color      = Color.White.copy(alpha = 0.03f),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter  = false,
                            topLeft    = Offset(-100f, size.height * 0.3f),
                            size       = androidx.compose.ui.geometry.Size(300f, 300f),
                            style      = Stroke(width = 60f)
                        )
                    }
                    .padding(
                        start  = 20.dp,
                        end    = 20.dp,
                        top    = 52.dp,
                        bottom = 24.dp
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(headerAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Back + title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WhiteBg.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = SecondaryGold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = "My Bookings",
                                color      = SecondaryGold,
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text    = "${bookings.size} total bookings",
                                color   = SecondaryGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        // Floating calendar icon
                        Box(
                            modifier = Modifier
                                .offset(y = iconFloat.value.dp)
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            SecondaryGold.copy(alpha = 0.3f),
                                            SecondaryGold.copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint     = SecondaryGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── 5-stat summary row ─────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BookingStatBox(
                            modifier = Modifier.weight(1f),
                            value    = confirmedCount.toString(),
                            label    = "Active",
                            icon     = Icons.Default.CheckCircle,
                            color    = SuccessGreen
                        )
                        BookingStatBox(
                            modifier = Modifier.weight(1f),
                            value    = pendingCount.toString(),
                            label    = "Pending",
                            icon     = Icons.Default.HourglassEmpty,
                            color    = AccentOrange
                        )
                        BookingStatBox(
                            modifier = Modifier.weight(1f),
                            value    = completedCount.toString(),
                            label    = "Done",
                            icon     = Icons.Default.TaskAlt,
                            color    = SoftBlue
                        )
                        BookingStatBox(
                            modifier = Modifier.weight(1f),
                            value    = "${totalHours}h",
                            label    = "Hours",
                            icon     = Icons.Default.Schedule,
                            color    = SecondaryGold
                        )
                    }
                }
            }

            // ── Filter chips ───────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    val chipColor = when (filter) {
                        "Confirmed"  -> SuccessGreen
                        "Pending"    -> AccentOrange
                        "Completed"  -> SoftBlue
                        "Cancelled"  -> ErrorRed
                        else         -> PrimaryOlive
                    }
                    val count = when (filter) {
                        "Confirmed"  -> confirmedCount
                        "Pending"    -> pendingCount
                        "Completed"  -> completedCount
                        "Cancelled"  -> cancelledCount
                        else         -> bookings.size
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedFilter = filter },
                        label    = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text       = filter,
                                    fontSize   = 12.sp,
                                    fontWeight = if (isSelected)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected)
                                                    WhiteBg.copy(alpha = 0.3f)
                                                else chipColor.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text       = count.toString(),
                                            fontSize   = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = if (isSelected) WhiteBg else chipColor
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor,
                            selectedLabelColor     = WhiteBg,
                            containerColor         = Color(0xFFF5F5F5),
                            labelColor             = TextMuted
                        )
                    )
                }
            }

            HorizontalDivider(color = FieldBorder)

            // ── Banners ────────────────────────────────────────
            AnimatedVisibility(
                visible = successMessage.isNotEmpty(),
                enter   = slideInVertically() + fadeIn(),
                exit    = slideOutVertically() + fadeOut()
            ) {
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
                            tint     = WhiteBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text       = successMessage,
                            color      = WhiteBg,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold
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
                        .background(ErrorRed)
                        .padding(12.dp)
                ) {
                    Text("⚠ $firebaseError", color = WhiteBg, fontSize = 13.sp)
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
                            "Loading your bookings...",
                            color    = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (filteredBookings.isEmpty()) {
                BookingsEmptyState(
                    filter        = selectedFilter,
                    onExplore     = {
                        // In real app, navigate to explore
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(contentAlpha.value)
                        .offset(y = contentSlide.value.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Results label
                    item {
                        Text(
                            text     = "${filteredBookings.size} booking" +
                                    if (filteredBookings.size != 1) "s" else "",
                            color    = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    items(
                        items = filteredBookings,
                        key   = { it.bookingId }
                    ) { booking ->
                        VolunteerBookingCard(
                            booking  = booking,
                            onCancel = {
                                cancelTargetId   = booking.bookingId
                                showCancelDialog = true
                            },
                            onViewDetails = {
                                // View details
                            },
                            onQrCheckIn = onNavigateToQrCheckIn,
                            onMessage = {
                                // Message
                            },
                            onViewCertificate = {
                                // View cert
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Volunteer Booking Card ────────────────────────────────────
// The main card for each booking. Rich with icons, colors,
// status indicators, check-in state and action buttons.
@Composable
fun VolunteerBookingCard(
    booking: VolunteerBooking,
    onCancel: () -> Unit,
    onViewDetails: () -> Unit,
    onQrCheckIn: () -> Unit,
    onMessage: () -> Unit,
    onViewCertificate: () -> Unit
) {
    // Status colors and icons
    val statusColor = when (booking.status) {
        "confirmed"  -> SuccessGreen
        "pending"    -> AccentOrange
        "cancelled"  -> ErrorRed
        "completed"  -> SoftBlue
        else         -> TextMuted
    }
    val statusBg = when (booking.status) {
        "confirmed"  -> Color(0xFFE8F5E9)
        "pending"    -> Color(0xFFFFF3EE)
        "cancelled"  -> Color(0xFFFFEBEE)
        "completed"  -> Color(0xFFE3F2FD)
        else         -> Color(0xFFF5F5F5)
    }
    val statusIcon = when (booking.status) {
        "confirmed"  -> Icons.Default.CheckCircle
        "pending"    -> Icons.Default.HourglassEmpty
        "cancelled"  -> Icons.Default.Cancel
        "completed"  -> Icons.Default.TaskAlt
        else         -> Icons.Default.Info
    }

    // Type color
    val typeColor = if (booking.type == "volunteer") PrimaryOlive else AccentOrange

    // Gradient for the top bar per status
    val topBarColors = when (booking.status) {
        "confirmed"  -> listOf(SuccessGreen, Color(0xFF4CAF50))
        "pending"    -> listOf(AccentOrange, Color(0xFFFF8A65))
        "cancelled"  -> listOf(ErrorRed, Color(0xFFEF5350))
        "completed"  -> listOf(SoftBlue, Color(0xFF42A5F5))
        else         -> listOf(TextMuted, FieldBorder)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Gradient top accent bar ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(colors = topBarColors)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Top row: title + status chip ───────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Activity type icon box
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        typeColor.copy(alpha = 0.2f),
                                        LightGold
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (booking.type == "volunteer")
                                Icons.Default.VolunteerActivism
                            else Icons.Default.Groups,
                            contentDescription = null,
                            tint     = typeColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text       = booking.opportunityTitle,
                            color      = TextDark,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines   = 2,
                            overflow   = TextOverflow.Ellipsis
                        )
                        // Org name with building icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint     = PrimaryOlive,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text     = booking.orgName,
                                color    = PrimaryOlive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Status chip with icon
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint     = statusColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text       = booking.status.replaceFirstChar
                                { it.uppercase() },
                                color      = statusColor,
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── Info strip ─────────────────────────────────
                // Date, time, location in a styled row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F8F8))
                        .border(1.dp, FieldBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Date + time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BookingInfoItem(
                                icon  = Icons.Default.CalendarToday,
                                text  = booking.date,
                                color = PrimaryOlive
                            )
                            if (booking.startTime.isNotBlank()) {
                                BookingInfoItem(
                                    icon  = Icons.Default.AccessTime,
                                    text  = "${booking.startTime} – ${booking.endTime}",
                                    color = SoftBlue
                                )
                            }
                        }
                        // Location
                        if (booking.location.isNotBlank()) {
                            BookingInfoItem(
                                icon  = Icons.Default.LocationOn,
                                text  = booking.location,
                                color = AccentOrange
                            )
                        }
                    }
                }

                // ── Check-in + hours strip ─────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Check-in badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (booking.checkedIn)
                                    SuccessGreen.copy(alpha = 0.1f)
                                else FieldBorder.copy(alpha = 0.5f)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (booking.checkedIn)
                                    Icons.Default.LocationOn
                                else Icons.Default.LocationOff,
                                contentDescription = null,
                                tint     = if (booking.checkedIn) SuccessGreen else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text     = if (booking.checkedIn) "Checked In" else "Not Checked In",
                                color    = if (booking.checkedIn) SuccessGreen else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Hours badge — only if logged
                    if (booking.hoursLogged > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftBlue.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint     = SoftBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text     = "${booking.hoursLogged}h logged",
                                    color    = SoftBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Certificate badge
                    if (booking.hasCertificate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    Color(0xFFFFD700).copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CardMembership,
                                    contentDescription = null,
                                    tint     = Color(0xFFB8860B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text     = "Certificate",
                                    color    = Color(0xFFB8860B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = FieldBorder)

                // ── Action buttons ─────────────────────────────
                // Show different actions based on booking status
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: View details + Message org
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // View details
                        OutlinedButton(
                            onClick  = onViewDetails,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp, PrimaryOlive.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint     = PrimaryOlive,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Details",
                                color      = PrimaryOlive,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Message org
                        OutlinedButton(
                            onClick  = onMessage,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFF7B1FA2).copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Message,
                                contentDescription = null,
                                tint     = Color(0xFF7B1FA2),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Message",
                                color      = Color(0xFF7B1FA2),
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Row 2: Status-specific actions
                    when (booking.status) {
                        "confirmed" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // QR Check-in
                                Button(
                                    onClick  = onQrCheckIn,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryOlive
                                    ),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint     = SecondaryGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        "QR Check-In",
                                        color      = SecondaryGold,
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Cancel
                                OutlinedButton(
                                    onClick  = onCancel,
                                    modifier = Modifier.weight(1f),
                                    shape    = RoundedCornerShape(10.dp),
                                    border   = androidx.compose.foundation.BorderStroke(
                                        1.dp, ErrorRed.copy(alpha = 0.5f)
                                    ),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint     = ErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Cancel",
                                        color      = ErrorRed,
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        "completed" -> {
                            // View certificate if available
                            if (booking.hasCertificate) {
                                Button(
                                    onClick  = onViewCertificate,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB8860B)
                                    ),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CardMembership,
                                        contentDescription = null,
                                        tint     = WhiteBg,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "View Certificate",
                                        color      = WhiteBg,
                                        fontSize   = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(LightGold)
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint     = PrimaryOlive,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text     = "Certificate will appear here once issued by the organization.",
                                            color    = TextMuted,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        "cancelled" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorRed.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        ErrorRed.copy(alpha = 0.2f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint     = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text     = "This booking was cancelled.",
                                        color    = ErrorRed,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Bookings Empty State ──────────────────────────────────────
@Composable
fun BookingsEmptyState(filter: String, onExplore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Layered decorative circles
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(LightGold.copy(alpha = 0.4f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(LightGold)
                )
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SecondaryGold, LightGold)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (filter != "All")
                            Icons.Default.FilterAlt
                        else Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint     = PrimaryOlive,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text       = if (filter != "All")
                    "No $filter bookings"
                else "No bookings yet",
                color      = TextDark,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            Text(
                text       = if (filter != "All")
                    "You don't have any $filter bookings right now."
                else "You haven't booked any opportunities yet.\nStart by exploring what's near you!",
                color      = TextMuted,
                fontSize   = 13.sp,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (filter == "All") {
                Button(
                    onClick = onExplore,
                    shape   = RoundedCornerShape(14.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Explore,
                        contentDescription = null,
                        tint     = WhiteBg,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Explore Opportunities",
                        color      = WhiteBg,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            // Decorative dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PrimaryOlive, AccentOrange, SecondaryGold).forEach { c ->
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
}

// ── Booking Stat Box ──────────────────────────────────────────
@Composable
fun BookingStatBox(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WhiteBg.copy(alpha = 0.13f))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint     = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text       = value,
            color      = SecondaryGold,
            fontSize   = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text    = label,
            color   = SecondaryGold.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Booking Info Item ─────────────────────────────────────────
@Composable
fun BookingInfoItem(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint     = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text     = text,
            color    = TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyBookingsScreenPreview() {
    MyBookingsScreen(onNavigateBack = {}, onNavigateToQrCheckIn = {})
}

