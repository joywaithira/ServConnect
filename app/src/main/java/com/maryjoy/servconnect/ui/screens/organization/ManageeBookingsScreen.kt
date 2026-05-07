package com.maryjoy.servconnect.ui.screens.organization

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
private val ErrorRed       = Color(0xFFD32F2F)
private val SoftBlue       = Color(0xFF1976D2)
private val SoftPurple     = Color(0xFF7B1FA2)

// ── Booking Data Class ────────────────────────────────────────
data class Booking(
    val bookingId: String = "",
    val volunteerId: String = "",
    val volunteerName: String = "",
    val volunteerEmail: String = "",
    val opportunityTitle: String = "",
    val opportunityId: String = "",
    val date: String = "",
    val status: String = "pending",
    val checkedIn: Boolean = false,
    val hoursLogged: Int = 0,
    val rating: Float = 0f,
    val bookedAt: Long = 0L
)

@Composable
fun ManageBookingsScreen(
    navController: NavController,
    // Optional — if passed, shows bookings for one specific
    // opportunity. If null shows all org bookings.
    opportunityId: String? = null
) {
    val scope = rememberCoroutineScope()

    // ── State ─────────────────────────────────────────────────
    var bookings        by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(true) }
    var selectedFilter  by remember { mutableStateOf("All") }
    var searchQuery     by remember { mutableStateOf("") }
    var firebaseError   by remember { mutableStateOf("") }
    var successMessage  by remember { mutableStateOf("") }
    var expandedBookingId by remember { mutableStateOf<String?>(null) }

    // Confirmation dialog state
    var showConfirmDialog  by remember { mutableStateOf(false) }
    var pendingAction      by remember { mutableStateOf<Pair<String, String>?>(null) }
    // Pair of (bookingId, newStatus)

    val filters = listOf("All", "Pending", "Confirmed", "Completed", "Cancelled")

    // ── Entrance animations ───────────────────────────────────
    val headerAlpha  = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(60f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(200)
            contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            contentSlide.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
        }
    }

    // ── Decorative dot pulse animation ────────────────────────
    // Gives the header decorative circles a breathing effect
    val dotPulse = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            dotPulse.animateTo(1.15f, tween(1000, easing = FastOutSlowInEasing))
            dotPulse.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
        }
    }

    // ── Fetch bookings ────────────────────────────────────────
    // If opportunityId is provided fetch only for that opportunity.
    // Otherwise fetch ALL bookings for the logged-in org.
    LaunchedEffect(opportunityId) {
        val db = FirebaseFirestore.getInstance()
        val query = if (opportunityId != null) {
            // Fetch bookings for one specific opportunity
            db.collection("bookings")
                .whereEqualTo("opportunityId", opportunityId)
        } else {
            // Fetch all bookings for this org
            val uid = com.google.firebase.auth.FirebaseAuth
                .getInstance().currentUser?.uid ?: return@LaunchedEffect
            db.collection("bookings")
                .whereEqualTo("orgId", uid)
        }

        query.orderBy("bookedAt",
            com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                bookings = snapshot.documents.map { doc ->
                    Booking(
                        bookingId        = doc.id,
                        volunteerId      = doc.getString("volunteerId")      ?: "",
                        volunteerName    = doc.getString("volunteerName")    ?: "",
                        volunteerEmail   = doc.getString("volunteerEmail")   ?: "",
                        opportunityTitle = doc.getString("opportunityTitle") ?: "",
                        opportunityId    = doc.getString("opportunityId")    ?: "",
                        date             = doc.getString("date")             ?: "",
                        status           = doc.getString("status")           ?: "pending",
                        checkedIn        = doc.getBoolean("checkedIn")       ?: false,
                        hoursLogged      = (doc.getLong("hours")             ?: 0).toInt(),
                        rating           = (doc.getDouble("rating")          ?: 0.0).toFloat(),
                        bookedAt         = doc.getLong("bookedAt")           ?: 0L
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load bookings: ${e.message}"
                isLoading = false
            }
    }

    // ── Update booking status ─────────────────────────────────
    // Called after the org confirms the action dialog.
    // Updates the status field in Firestore and reflects
    // the change immediately in the local list.
    fun updateBookingStatus(bookingId: String, newStatus: String) {
        FirebaseFirestore.getInstance()
            .collection("bookings")
            .document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Update locally so UI reacts immediately
                bookings = bookings.map {
                    if (it.bookingId == bookingId) it.copy(status = newStatus)
                    else it
                }
                successMessage = "Booking ${newStatus.replaceFirstChar()
                    { it.uppercase() }} successfully"

                // Auto-clear the success message after 3 seconds
                scope.launch {
                    delay(3000)
                    successMessage = ""
                }
            }
            .addOnFailureListener { e ->
                firebaseError = "Update failed: ${e.message}"
            }
    }

    // ── Rate volunteer ────────────────────────────────────────
    // Saves a star rating for the volunteer on this booking.
    // Also saves the rating to the volunteer's user document
    // so their average rating updates across the app.
    fun rateVolunteer(bookingId: String, volunteerId: String, rating: Float) {
        val db = FirebaseFirestore.getInstance()

        // Save rating on the booking document
        db.collection("bookings")
            .document(bookingId)
            .update("rating", rating)
            .addOnSuccessListener {
                bookings = bookings.map {
                    if (it.bookingId == bookingId) it.copy(rating = rating)
                    else it
                }

                // Also update the volunteer's average rating
                // by fetching all their ratings and averaging
                db.collection("bookings")
                    .whereEqualTo("volunteerId", volunteerId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val ratings = snapshot.documents
                            .mapNotNull { it.getDouble("rating") }
                            .filter { it > 0 }

                        if (ratings.isNotEmpty()) {
                            val avg = ratings.average()
                            db.collection("users")
                                .document(volunteerId)
                                .update(
                                    "averageRating", avg,
                                    "totalRatings", ratings.size
                                )
                        }
                    }

                successMessage = "Rating saved!"
                scope.launch {
                    delay(2000)
                    successMessage = ""
                }
            }
    }

    // ── Filtered list ─────────────────────────────────────────
    val filteredBookings = bookings.filter { booking ->
        val matchesFilter = when (selectedFilter) {
            "Pending"   -> booking.status == "pending"
            "Confirmed" -> booking.status == "confirmed"
            "Completed" -> booking.status == "completed"
            "Cancelled" -> booking.status == "cancelled"
            else        -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                booking.volunteerName.contains(searchQuery, ignoreCase = true) ||
                booking.opportunityTitle.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    // ── Summary counts ────────────────────────────────────────
    val pendingCount   = bookings.count { it.status == "pending" }
    val confirmedCount = bookings.count { it.status == "confirmed" }
    val completedCount = bookings.count { it.status == "completed" }
    val cancelledCount = bookings.count { it.status == "cancelled" }

    // ── Confirm Action Dialog ─────────────────────────────────
    if (showConfirmDialog && pendingAction != null) {
        val (bookingId, newStatus) = pendingAction!!
        val actionLabel = newStatus.replaceFirstChar { it.uppercase() }
        val actionColor = when (newStatus) {
            "confirmed"  -> SuccessGreen
            "cancelled"  -> ErrorRed
            "completed"  -> SoftBlue
            else         -> PrimaryOlive
        }

        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                pendingAction = null
            },
            containerColor = WhiteBg,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(actionColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (newStatus) {
                            "confirmed" -> Icons.Default.CheckCircle
                            "cancelled" -> Icons.Default.Cancel
                            "completed" -> Icons.Default.TaskAlt
                            else        -> Icons.Default.Update
                        },
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Mark as $actionLabel?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = when (newStatus) {
                        "confirmed" ->
                            "This will confirm the volunteer's booking " +
                                    "and notify them that their spot is secured."
                        "cancelled" ->
                            "This will cancel the booking and free up " +
                                    "the slot for another volunteer."
                        "completed" ->
                            "This marks the volunteer as having completed " +
                                    "this activity. You can then issue a certificate."
                        else -> "Update this booking status?"
                    },
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateBookingStatus(bookingId, newStatus)
                        showConfirmDialog = false
                        pendingAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = actionColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = WhiteBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingAction = null
                    },
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
                    // Decorative dot pattern drawn on the header
                    .drawBehind {
                        drawDecorativeDots(this)
                    }
                    .padding(
                        start = 8.dp, end = 20.dp,
                        top = 48.dp, bottom = 0.dp
                    )

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(headerAlpha.value)
                ) {
                    // Back + title row
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
                                text = "Manage Bookings",
                                color = SecondaryGold,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = "${bookings.size} total bookings",
                                color = SecondaryGold.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        // Notification style pending badge
                        if (pendingCount > 0) {
                            Box(
                                modifier = Modifier
                                    .scale(dotPulse.value)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentOrange)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Pending,
                                        contentDescription = null,
                                        tint = WhiteBg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$pendingCount pending",
                                        color = WhiteBg,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── 4-stat summary strip ───────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeaderStatBox(
                            modifier  = Modifier.weight(1f),
                            value     = pendingCount.toString(),
                            label     = "Pending",
                            icon      = Icons.Default.HourglassEmpty,
                            color     = AccentOrange
                        )
                        HeaderStatBox(
                            modifier  = Modifier.weight(1f),
                            value     = confirmedCount.toString(),
                            label     = "Confirmed",
                            icon      = Icons.Default.CheckCircle,
                            color     = SuccessGreen
                        )
                        HeaderStatBox(
                            modifier  = Modifier.weight(1f),
                            value     = completedCount.toString(),
                            label     = "Completed",
                            icon      = Icons.Default.TaskAlt,
                            color     = SoftBlue
                        )
                        HeaderStatBox(
                            modifier  = Modifier.weight(1f),
                            value     = cancelledCount.toString(),
                            label     = "Cancelled",
                            icon      = Icons.Default.Cancel,
                            color     = ErrorRed
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Search bar ─────────────────────────────
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        placeholder = {
                            Text(
                                "Search volunteer or opportunity...",
                                color = SecondaryGold.copy(alpha = 0.45f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = SecondaryGold.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = SecondaryGold.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = SecondaryGold.copy(alpha = 0.6f),
                            unfocusedBorderColor    = SecondaryGold.copy(alpha = 0.25f),
                            focusedContainerColor   = WhiteBg.copy(alpha = 0.1f),
                            unfocusedContainerColor = WhiteBg.copy(alpha = 0.07f),
                            cursorColor             = SecondaryGold,
                            focusedTextColor        = SecondaryGold,
                            unfocusedTextColor      = SecondaryGold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Filter chip row ────────────────────────────────
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
                        "Pending"   -> AccentOrange
                        "Confirmed" -> SuccessGreen
                        "Completed" -> SoftBlue
                        "Cancelled" -> ErrorRed
                        else        -> PrimaryOlive
                    }
                    val count = when (filter) {
                        "Pending"   -> pendingCount
                        "Confirmed" -> confirmedCount
                        "Completed" -> completedCount
                        "Cancelled" -> cancelledCount
                        else        -> bookings.size
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedFilter = filter },
                        label    = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                                // Count badge on chip
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) WhiteBg.copy(alpha = 0.25f)
                                                else chipColor.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WhiteBg else chipColor
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

            // ── Success / Error banners ────────────────────────
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
                            tint = WhiteBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = successMessage,
                            color = WhiteBg,
                            fontSize = 13.sp,
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = WhiteBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = firebaseError,
                            color = WhiteBg,
                            fontSize = 13.sp
                        )
                    }
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
                            color = PrimaryOlive,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Loading bookings...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (filteredBookings.isEmpty()) {
                RichEmptyState(
                    filter = selectedFilter,
                    searchQuery = searchQuery
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(contentAlpha.value)
                        .offset(y = contentSlide.value.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Results label
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredBookings.size} booking" +
                                        if (filteredBookings.size != 1) "s" else "",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            // Sort indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Newest first",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    items(
                        items = filteredBookings,
                        key   = { it.bookingId }
                    ) { booking ->
                        RichBookingCard(
                            booking          = booking,
                            isExpanded       = expandedBookingId == booking.bookingId,
                            onToggleExpand   = {
                                expandedBookingId =
                                    if (expandedBookingId == booking.bookingId) null
                                    else booking.bookingId
                            },
                            onConfirm = {
                                pendingAction   = booking.bookingId to "confirmed"
                                showConfirmDialog = true
                            },
                            onCancel  = {
                                pendingAction   = booking.bookingId to "cancelled"
                                showConfirmDialog = true
                            },
                            onComplete = {
                                pendingAction   = booking.bookingId to "completed"
                                showConfirmDialog = true
                            },
                            onRate = { rating ->
                                rateVolunteer(booking.bookingId, booking.volunteerId, rating)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Rich Booking Card ─────────────────────────────────────────
// The centrepiece of this screen. Each booking is a card
// that can be expanded/collapsed to reveal action buttons
// and the star rating widget.
@Composable
fun RichBookingCard(
    booking: Booking,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onRate: (Float) -> Unit
) {
    // Colors and icons per status
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

    // Animated expand/collapse
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow"
    )

    // Avatar gradient — each volunteer gets a unique
    // gradient based on the first letter of their name
    val avatarGradient = when (booking.volunteerName.firstOrNull()?.lowercaseChar()) {
        in 'a'..'e' -> listOf(PrimaryOlive, Color(0xFF8FA038))
        in 'f'..'j' -> listOf(AccentOrange, Color(0xFFFF8A65))
        in 'k'..'o' -> listOf(SoftBlue, Color(0xFF42A5F5))
        in 'p'..'t' -> listOf(SoftPurple, Color(0xFFBA68C8))
        else         -> listOf(DarkOlive, PrimaryOlive)
    }

    var selectedRating by remember { mutableFloatStateOf(booking.rating) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Colored top accent bar ─────────────────────────
            // Each status gets its own color bar at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                statusColor,
                                statusColor.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            // ── Main card content ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Top row — avatar + info + status + expand arrow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gradient avatar with initials
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = avatarGradient)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = booking.volunteerName
                                .firstOrNull()?.uppercaseChar()?.toString() ?: "V",
                            color = WhiteBg,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Name, email, opportunity
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = booking.volunteerName,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.volunteerEmail,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        // Opportunity name chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LightGold)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = booking.opportunityTitle,
                                color = PrimaryOlive,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Status + expand arrow column
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Status pill with icon
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
                                    tint = statusColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = booking.status.replaceFirstChar
                                    { it.uppercase() },
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Expand/collapse arrow
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }

                // ── Info strip ─────────────────────────────────
                // Date, check-in status and hours in a row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoPill(
                        icon  = Icons.Default.CalendarToday,
                        label = booking.date.ifBlank { "No date" },
                        color = PrimaryOlive
                    )
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color    = FieldBorder
                    )
                    InfoPill(
                        icon  = if (booking.checkedIn)
                            Icons.Default.LocationOn
                        else Icons.Default.LocationOff,
                        label = if (booking.checkedIn) "Checked In" else "Not Yet",
                        color = if (booking.checkedIn) SuccessGreen else TextMuted
                    )
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color    = FieldBorder
                    )
                    InfoPill(
                        icon  = Icons.Default.Schedule,
                        label = if (booking.hoursLogged > 0)
                            "${booking.hoursLogged}h logged" else "0h",
                        color = if (booking.hoursLogged > 0) SoftBlue else TextMuted
                    )
                }
            }

            // ── Expanded content ───────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically(spring(Spring.DampingRatioMediumBouncy)) +
                        fadeIn(tween(200)),
                exit    = shrinkVertically() + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp, end = 16.dp,
                            bottom = 16.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    HorizontalDivider(color = FieldBorder)

                    // ── Action buttons ─────────────────────────
                    // Only show actions relevant to current status
                    Text(
                        text = "Quick Actions",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Confirm — only for pending
                        if (booking.status == "pending") {
                            ActionButton(
                                label    = "Confirm",
                                icon     = Icons.Default.CheckCircle,
                                color    = SuccessGreen,
                                modifier = Modifier.weight(1f),
                                onClick  = onConfirm
                            )
                        }

                        // Complete — only for confirmed
                        if (booking.status == "confirmed") {
                            ActionButton(
                                label    = "Complete",
                                icon     = Icons.Default.TaskAlt,
                                color    = SoftBlue,
                                modifier = Modifier.weight(1f),
                                onClick  = onComplete
                            )
                        }

                        // Cancel — for pending or confirmed only
                        if (booking.status in listOf("pending", "confirmed")) {
                            ActionButton(
                                label    = "Cancel",
                                icon     = Icons.Default.Cancel,
                                color    = ErrorRed,
                                modifier = Modifier.weight(1f),
                                onClick  = onCancel
                            )
                        }

                        // Message volunteer
                        ActionButton(
                            label    = "Message",
                            icon     = Icons.Default.Message,
                            color    = SoftPurple,
                            modifier = Modifier.weight(1f),
                            onClick  = { /* TODO: navigate to messages */ }
                        )
                    }

                    // ── Star rating ────────────────────────────
                    // Only shown for completed bookings.
                    // Org can rate the volunteer 1–5 stars.
                    if (booking.status == "completed") {
                        HorizontalDivider(color = FieldBorder)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SecondaryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Rate this volunteer",
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // 5 star rating row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) { index ->
                                    val starValue = (index + 1).toFloat()
                                    val isFilled  = starValue <= selectedRating

                                    Icon(
                                        imageVector = if (isFilled)
                                            Icons.Default.Star
                                        else Icons.Default.StarOutline,
                                        contentDescription = "Star ${index + 1}",
                                        tint = if (isFilled) SecondaryGold else FieldBorder,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable {
                                                selectedRating = starValue
                                                onRate(starValue)
                                            }
                                    )
                                }

                                if (selectedRating > 0) {
                                    Text(
                                        text = "${selectedRating.toInt()}/5",
                                        color = PrimaryOlive,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (selectedRating == 0f) {
                                Text(
                                    text = "Tap a star to rate this volunteer's performance",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Rich Empty State ──────────────────────────────────────────
// Full-screen illustrated empty state with decorative shapes
@Composable
fun RichEmptyState(filter: String, searchQuery: String) {
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
            // Decorative layered circles
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(LightGold.copy(alpha = 0.5f))
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
                        .background(SecondaryGold.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty())
                            Icons.Default.SearchOff
                        else Icons.Default.BookOnline,
                        contentDescription = null,
                        tint = PrimaryOlive,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = when {
                    searchQuery.isNotEmpty() ->
                        "No results for \"$searchQuery\""
                    filter != "All" ->
                        "No $filter bookings yet"
                    else ->
                        "No bookings yet"
                },
                color = TextDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = when {
                    searchQuery.isNotEmpty() ->
                        "Try a different name or opportunity title"
                    filter != "All" ->
                        "Bookings with this status will appear here"
                    else ->
                        "Once volunteers book your opportunities\n" +
                                "their bookings will appear here"
                },
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // Small decorative dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    0    -> PrimaryOlive.copy(alpha = 0.4f)
                                    1    -> AccentOrange.copy(alpha = 0.6f)
                                    else -> SecondaryGold.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
    }
}

// ── Header Stat Box ───────────────────────────────────────────
// Each stat in the 4-box header strip
@Composable
fun HeaderStatBox(
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
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Info Pill ─────────────────────────────────────────────────
// Small icon + label used in the booking card's info strip
@Composable
fun InfoPill(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Action Button ─────────────────────────────────────────────
// Colored outlined button for the booking actions row
@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(40.dp),
        shape    = RoundedCornerShape(10.dp),
        border   = androidx.compose.foundation.BorderStroke(
            1.5.dp, color.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Decorative dot pattern for header background ──────────────
// Draws small semi-transparent circles across the header
// giving it a subtle textured feel without being distracting
fun drawDecorativeDots(scope: DrawScope) {
    val dotColor = Color.White.copy(alpha = 0.04f)
    val positions = listOf(
        Offset(scope.size.width * 0.8f, scope.size.height * 0.2f) to 60f,
        Offset(scope.size.width * 0.1f, scope.size.height * 0.6f) to 40f,
        Offset(scope.size.width * 0.9f, scope.size.height * 0.8f) to 80f,
        Offset(scope.size.width * 0.3f, scope.size.height * 0.1f) to 30f,
        Offset(scope.size.width * 0.6f, scope.size.height * 0.9f) to 50f,
        Offset(scope.size.width * 0.05f, scope.size.height * 0.9f) to 70f,
    )
    positions.forEach { (offset, radius) ->
        scope.drawCircle(
            color  = dotColor,
            radius = radius,
            center = offset
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ManageBookingsScreenPreview() {
    ManageBookingsScreen(rememberNavController())
}