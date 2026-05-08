package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ─────────────────────────────────────────────
//  COLORS
// ─────────────────────────────────────────────
private val PrimaryGreen    = Color(0xFF676B2C)
private val SecondaryGold   = Color(0xFFF3DF90)
private val AccentOrange    = Color(0xFFFF5722)
private val SurfaceWhite    = Color(0xFFFFFFFF)
private val SurfaceOffWhite = Color(0xFFFAF9F4)
private val TextPrimary     = Color(0xFF1A1C0A)
private val TextSecondary   = Color(0xFF5A5D30)
private val TextHint        = Color(0xFF9E9E9E)
private val DividerColor    = Color(0xFFEEECE0)
private val StatusOpen      = Color(0xFF4CAF50)

// ─────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────
enum class NotifType {
    NEW_BOOKING,
    CANCELLATION,
    CHECK_IN,
    CERTIFICATE_REQUEST,
    NEW_MESSAGE,
    VOLUNTEER_RATED,
    SLOT_ALERT,
    SYSTEM
}

data class OrgNotification(
    val id: String,
    val type: NotifType,
    val title: String,
    val body: String,
    val timestamp: String,
    val isRead: Boolean,
    val actionLabel: String? = null,
    val avatarInitials: String? = null,
    val avatarColor: Color = PrimaryGreen
)

// ─────────────────────────────────────────────
//  SAMPLE DATA
// ─────────────────────────────────────────────
private val sampleNotifications = listOf(
    OrgNotification(
        id             = "1",
        type           = NotifType.NEW_BOOKING,
        title          = "New Booking",
        body           = "Amara Osei just reserved a spot for Children's Home Visit on Sat, Jun 7.",
        timestamp      = "2 min ago",
        isRead         = false,
        actionLabel    = "View Booking",
        avatarInitials = "AO",
        avatarColor    = Color(0xFF4CAF50)
    ),
    OrgNotification(
        id             = "2",
        type           = NotifType.NEW_MESSAGE,
        title          = "New Message",
        body           = "James Otieno sent you a message: \"We are a group of 8, can we all join?\"",
        timestamp      = "15 min ago",
        isRead         = false,
        actionLabel    = "Reply",
        avatarInitials = "JO",
        avatarColor    = Color(0xFF00BCD4)
    ),
    OrgNotification(
        id             = "3",
        type           = NotifType.CHECK_IN,
        title          = "Volunteer Checked In",
        body           = "Lena Waweru has checked in at Community Garden Cleanup via QR code. Timer started.",
        timestamp      = "1 hr ago",
        isRead         = false,
        actionLabel    = "View Activity",
        avatarInitials = "LW",
        avatarColor    = Color(0xFF2196F3)
    ),
    OrgNotification(
        id             = "4",
        type           = NotifType.CERTIFICATE_REQUEST,
        title          = "Certificate Ready to Issue",
        body           = "David Mwangi completed Elder Care Program (6 hrs). Issue their certificate now.",
        timestamp      = "3 hrs ago",
        isRead         = false,
        actionLabel    = "Issue Certificate",
        avatarInitials = "DM",
        avatarColor    = Color(0xFFFF9800)
    ),
    OrgNotification(
        id             = "5",
        type           = NotifType.SLOT_ALERT,
        title          = "Opportunity Almost Full",
        body           = "Children's Home Visit only has 3 spots left out of 15. Consider increasing capacity.",
        timestamp      = "5 hrs ago",
        isRead         = true,
        actionLabel    = "Edit Opportunity"
    ),
    OrgNotification(
        id             = "6",
        type           = NotifType.VOLUNTEER_RATED,
        title          = "New Rating Received",
        body           = "Zara Ahmed rated your organization ★★★★★ after completing Food Bank Drive.",
        timestamp      = "Yesterday",
        isRead         = true,
        avatarInitials = "ZA",
        avatarColor    = Color(0xFF9C27B0)
    ),
    OrgNotification(
        id             = "7",
        type           = NotifType.CANCELLATION,
        title          = "Booking Cancelled",
        body           = "Brian Kamau has cancelled their booking for Community Garden Cleanup on Jun 8.",
        timestamp      = "Yesterday",
        isRead         = true,
        avatarInitials = "BK",
        avatarColor    = Color(0xFF607D8B)
    ),
    OrgNotification(
        id             = "8",
        type           = NotifType.NEW_BOOKING,
        title          = "New Group Booking",
        body           = "A group of 5 from \"Nairobi Youth Corps\" booked Beach Clean-Up on Jun 14.",
        timestamp      = "2 days ago",
        isRead         = true,
        actionLabel    = "View Booking",
        avatarInitials = "NY",
        avatarColor    = AccentOrange
    ),
    OrgNotification(
        id             = "9",
        type           = NotifType.SYSTEM,
        title          = "Profile Verified",
        body           = "Congratulations! Your organization profile has been verified by ServConnect. You can now post opportunities.",
        timestamp      = "3 days ago",
        isRead         = true
    )
)

// ─────────────────────────────────────────────
//  NOTIFICATION VISUAL CONFIG
// ─────────────────────────────────────────────
private data class NotifStyle(
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@Composable
private fun notifStyle(type: NotifType): NotifStyle = when (type) {
    NotifType.NEW_BOOKING          -> NotifStyle(Icons.Filled.EventAvailable,   StatusOpen,             StatusOpen.copy(alpha = 0.1f))
    NotifType.CANCELLATION         -> NotifStyle(Icons.Filled.EventBusy,        AccentOrange,           AccentOrange.copy(alpha = 0.1f))
    NotifType.CHECK_IN             -> NotifStyle(Icons.Filled.QrCodeScanner,    Color(0xFF2196F3),      Color(0xFF2196F3).copy(alpha = 0.1f))
    NotifType.CERTIFICATE_REQUEST  -> NotifStyle(Icons.Filled.CardMembership,   Color(0xFF9C27B0),      Color(0xFF9C27B0).copy(alpha = 0.1f))
    NotifType.NEW_MESSAGE          -> NotifStyle(Icons.Filled.Chat,             PrimaryGreen,           PrimaryGreen.copy(alpha = 0.1f))
    NotifType.VOLUNTEER_RATED      -> NotifStyle(Icons.Filled.Star,             SecondaryGold.copy(red = 0.8f, green = 0.7f), Color(0xFFFFF8E1))
    NotifType.SLOT_ALERT           -> NotifStyle(Icons.Filled.Warning,          Color(0xFFFF9800),      Color(0xFFFF9800).copy(alpha = 0.1f))
    NotifType.SYSTEM               -> NotifStyle(Icons.Filled.Verified,         PrimaryGreen,           PrimaryGreen.copy(alpha = 0.08f))
}

// ─────────────────────────────────────────────
//  ROOT SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    var notifications by remember { mutableStateOf(sampleNotifications) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filters = listOf("All", "Unread", "Bookings", "Messages", "System")

    val filtered = when (selectedFilter) {
        "Unread"   -> notifications.filter { !it.isRead }
        "Bookings" -> notifications.filter { it.type == NotifType.NEW_BOOKING || it.type == NotifType.CANCELLATION || it.type == NotifType.SLOT_ALERT }
        "Messages" -> notifications.filter { it.type == NotifType.NEW_MESSAGE }
        "System"   -> notifications.filter { it.type == NotifType.SYSTEM || it.type == NotifType.VOLUNTEER_RATED || it.type == NotifType.CERTIFICATE_REQUEST }
        else       -> notifications
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = SurfaceOffWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Filter chips ──
            NotifFilterRow(
                filters        = filters,
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                unreadCount    = unreadCount
            )

            // ── List ──
            if (filtered.isEmpty()) {
                NotifEmptyState(filter = selectedFilter)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unread group header
                    val unread = filtered.filter { !it.isRead }
                    val read   = filtered.filter { it.isRead }

                    if (unread.isNotEmpty()) {
                        item {
                            NotifGroupHeader(title = "New", count = unread.size)
                        }
                        items(unread, key = { it.id }) { notif ->
                            NotifCard(
                                notification = notif,
                                onRead       = {
                                    notifications = notifications.map {
                                        if (it.id == notif.id) it.copy(isRead = true) else it
                                    }
                                },
                                onDismiss    = {
                                    notifications = notifications.filter { it.id != notif.id }
                                }
                            )
                        }
                    }

                    if (read.isNotEmpty()) {
                        item {
                            NotifGroupHeader(title = "Earlier", count = read.size)
                        }
                        items(read, key = { it.id }) { notif ->
                            NotifCard(
                                notification = notif,
                                onRead       = {},
                                onDismiss    = {
                                    notifications = notifications.filter { it.id != notif.id }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Clear all dialog ──
    if (showClearDialog) {
        ClearAllDialog(
            onConfirm = {
                notifications    = emptyList()
                showClearDialog  = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
//  HEADER
// ─────────────────────────────────────────────
@Composable
fun NotificationsHeader(
    unreadCount   : Int,
    onMarkAllRead : () -> Unit,
    onClearAll    : () -> Unit
) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label         = "p"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A4D1E), PrimaryGreen)
                )
            )
    ) {
        // Decorative blobs
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)) {
            drawCircle(
                color  = SecondaryGold.copy(alpha = 0.07f + pulse * 0.04f),
                radius = 180f,
                center = Offset(size.width * 0.9f, 0f)
            )
            drawCircle(
                color  = AccentOrange.copy(alpha = 0.05f),
                radius = 120f,
                center = Offset(size.width * 0.1f, size.height)
            )
        }

        Column(
            modifier = Modifier.padding(
                start  = 20.dp,
                end    = 20.dp,
                top    = 52.dp,
                bottom = 20.dp
            )
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        text       = "Notifications",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = SurfaceWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (unreadCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text     = "$unreadCount unread",
                                fontSize = 13.sp,
                                color    = SecondaryGold.copy(alpha = 0.9f)
                            )
                        }
                    } else {
                        Text(
                            text     = "All caught up!",
                            fontSize = 13.sp,
                            color    = SurfaceWhite.copy(alpha = 0.55f)
                        )
                    }
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (unreadCount > 0) {
                        // Mark all read
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceWhite.copy(alpha = 0.12f))
                                .clickable(onClick = onMarkAllRead)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.DoneAll,
                                    contentDescription = null,
                                    tint     = SecondaryGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    "Mark all read",
                                    color      = SecondaryGold,
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Clear all
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceWhite.copy(alpha = 0.1f))
                            .clickable(onClick = onClearAll),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear all",
                            tint     = SurfaceWhite.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  FILTER ROW
// ─────────────────────────────────────────────
@Composable
fun NotifFilterRow(
    filters        : List<String>,
    selectedFilter : String,
    onFilterChange : (String) -> Unit,
    unreadCount    : Int
) {
    LazyRow(
        contentPadding      = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier            = Modifier.padding(vertical = 12.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PrimaryGreen else SurfaceWhite)
                    .border(
                        width  = 1.dp,
                        color  = if (isSelected) PrimaryGreen else DividerColor,
                        shape  = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text       = filter,
                        fontSize   = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) SurfaceWhite else TextSecondary
                    )
                    // Unread badge on "All" and "Unread" chips
                    if ((filter == "All" || filter == "Unread") && unreadCount > 0 && !isSelected) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(AccentOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = if (unreadCount > 9) "9+" else "$unreadCount",
                                color    = SurfaceWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  GROUP HEADER
// ─────────────────────────────────────────────
@Composable
fun NotifGroupHeader(title: String, count: Int) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = TextSecondary
        )
        Text(
            text     = "$count",
            fontSize = 12.sp,
            color    = TextHint
        )
    }
}

// ─────────────────────────────────────────────
//  NOTIFICATION CARD
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifCard(
    notification : OrgNotification,
    onRead       : () -> Unit,
    onDismiss    : () -> Unit
) {
    val style     = notifStyle(notification.type)
    val isUnread  = !notification.isRead

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state                  = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Red swipe background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentOrange.copy(alpha = 0.15f))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint     = AccentOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Dismiss", fontSize = 10.sp, color = AccentOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (isUnread) onRead() },
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (isUnread) SurfaceWhite else SurfaceOffWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isUnread) 4.dp else 1.dp
            )
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: icon or avatar
                Box(modifier = Modifier.size(46.dp)) {
                    // Type icon background
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(style.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notification.avatarInitials != null) {
                            // Avatar initials
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(notification.avatarColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = notification.avatarInitials,
                                    color      = notification.avatarColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 12.sp
                                )
                            }
                        } else {
                            Icon(
                                imageVector        = style.icon,
                                contentDescription = null,
                                tint               = style.color,
                                modifier           = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Type icon badge (shown only when avatar is present)
                    if (notification.avatarInitials != null) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(style.color)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector        = style.icon,
                                contentDescription = null,
                                tint               = SurfaceWhite,
                                modifier           = Modifier.size(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = notification.title,
                            fontSize   = 13.sp,
                            fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color      = TextPrimary,
                            modifier   = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text     = notification.timestamp,
                                fontSize = 10.sp,
                                color    = if (isUnread) AccentOrange else TextHint
                            )
                            if (isUnread) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentOrange)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text       = notification.body,
                        fontSize   = 12.sp,
                        color      = if (isUnread) TextSecondary else TextHint,
                        lineHeight = 18.sp,
                        maxLines   = 3,
                        overflow   = TextOverflow.Ellipsis
                    )

                    // Action button
                    if (notification.actionLabel != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(style.color.copy(alpha = 0.1f))
                                .border(1.dp, style.color.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                .clickable { /* TODO: route to relevant screen */ }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text       = notification.actionLabel,
                                    fontSize   = 11.sp,
                                    color      = style.color,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint     = style.color,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Unread left accent bar
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(style.color, style.color.copy(alpha = 0f))
                            )
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────
@Composable
fun NotifEmptyState(filter: String) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                PrimaryGreen.copy(alpha = 0.12f),
                                PrimaryGreen.copy(alpha = 0.03f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint     = PrimaryGreen.copy(alpha = 0.4f),
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text       = if (filter == "All") "No notifications yet" else "No $filter notifications",
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "When volunteers book, check in, or message you, you'll see it here.",
                fontSize  = 13.sp,
                color     = TextHint,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  CLEAR ALL DIALOG
// ─────────────────────────────────────────────
@Composable
fun ClearAllDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceWhite,
        shape            = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AccentOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint     = AccentOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                "Clear All Notifications?",
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 17.sp,
                color      = TextPrimary
            )
        },
        text = {
            Text(
                text      = "This will permanently remove all notifications. This action cannot be undone.",
                fontSize  = 13.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text("Clear All", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape   = RoundedCornerShape(12.dp),
                border  = BorderStroke(1.dp, DividerColor)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(onNavigateBack = {})
}

