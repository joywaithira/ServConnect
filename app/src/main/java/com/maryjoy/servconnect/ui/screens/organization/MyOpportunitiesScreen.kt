package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_OPPORTUNITY_DETAIL
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_POST_OPPORTUNITY
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

@Composable
fun MyOpportunitiesScreen(navController: NavController) {

    // ── State ─────────────────────────────────────────────────
    var opportunities  by remember { mutableStateOf<List<OpportunityItem>>(emptyList()) }
    var isLoading      by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery    by remember { mutableStateOf("") }
    var deleteTargetId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var firebaseError  by remember { mutableStateOf("") }

    // ── Filter options ────────────────────────────────────────
    // The org can filter their opportunities by status
    // to quickly find open, full or completed ones
    val filters = listOf("All", "Open", "Full", "Completed")

    // ── Entrance animation ────────────────────────────────────
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    // ── Fetch opportunities from Firestore ────────────────────
    // Pulls all opportunities where orgId matches the
    // currently logged-in organization's uid.
    // Ordered by createdAt descending so newest appear first.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect

        FirebaseFirestore.getInstance()
            .collection("opportunities")
            .whereEqualTo("orgId", uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                opportunities = snapshot.documents.map { doc ->
                    OpportunityItem(
                        id          = doc.id,
                        title       = doc.getString("title")      ?: "",
                        date        = doc.getString("date")       ?: "",
                        location    = doc.getString("location")   ?: "",
                        slotsTotal  = (doc.getLong("slotsTotal")  ?: 0).toInt(),
                        slotsBooked = (doc.getLong("slotsBooked") ?: 0).toInt(),
                        status      = doc.getString("status")     ?: "open",
                        type        = doc.getString("type")       ?: "volunteer"
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load opportunities: ${e.message}"
                isLoading = false
            }
    }

    // ── Delete opportunity from Firestore ─────────────────────
    // Called after the org confirms the delete dialog.
    // Removes the document from the "opportunities" collection.
    // Also cleans up any bookings linked to this opportunity.
    fun deleteOpportunity(opportunityId: String) {
        val db = FirebaseFirestore.getInstance()

        // Delete the opportunity document itself
        db.collection("opportunities")
            .document(opportunityId)
            .delete()
            .addOnSuccessListener {
                // Remove it from the local list immediately
                // so the UI updates without a full reload
                opportunities = opportunities.filter { it.id != opportunityId }

                // Also delete all bookings for this opportunity
                // so volunteers don't see orphaned bookings
                db.collection("bookings")
                    .whereEqualTo("opportunityId", opportunityId)
                    .get()
                    .addOnSuccessListener { bookingSnapshot ->
                        bookingSnapshot.documents.forEach { bookingDoc ->
                            bookingDoc.reference.delete()
                        }
                    }
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to delete: ${e.message}"
            }
    }

    // ── Filtered + searched list ──────────────────────────────
    // This derived list applies both the status filter chip
    // and the search query at the same time so both work together
    val filteredOpportunities = opportunities.filter { opp ->
        val matchesFilter = when (selectedFilter) {
            "Open"      -> opp.status == "open"
            "Full"      -> opp.status == "full"
            "Completed" -> opp.status == "completed"
            else        -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                opp.title.contains(searchQuery, ignoreCase = true) ||
                opp.location.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    // ── Summary counts ────────────────────────────────────────
    // Derived from the full list — always reflects real data
    val openCount      = opportunities.count { it.status == "open" }
    val fullCount      = opportunities.count { it.status == "full" }
    val completedCount = opportunities.count { it.status == "completed" }

    // ── Delete Confirmation Dialog ────────────────────────────
    if (showDeleteDialog && deleteTargetId != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deleteTargetId = null
            },
            containerColor = WhiteBg,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Opportunity?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "This will permanently delete this opportunity " +
                            "and all its bookings. This cannot be undone.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTargetId?.let { deleteOpportunity(it) }
                        showDeleteDialog = false
                        deleteTargetId = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = WhiteBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteTargetId = null
                    },
                    shape = RoundedCornerShape(10.dp),
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
                    start = 8.dp,
                    end = 20.dp,
                    top = 48.dp,
                    bottom = 20.dp
                )
        ) {
            Column(modifier = Modifier.alpha(contentAlpha.value)) {

                // Back button + title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SecondaryGold
                        )
                    }
                    Column {
                        Text(
                            text = "My Opportunities",
                            color = SecondaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${opportunities.size} total posted",
                            color = SecondaryGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Mini stats row ─────────────────────────────
                // Quick glance at how many are open/full/completed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatChip(
                        label = "Open",
                        count = openCount,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatChip(
                        label = "Full",
                        count = fullCount,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatChip(
                        label = "Done",
                        count = completedCount,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Search bar ─────────────────────────────────
                // Lets the org search by title or location
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Search by title or location...",
                            color = SecondaryGold.copy(alpha = 0.5f),
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
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
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
                        unfocusedBorderColor    = SecondaryGold.copy(alpha = 0.3f),
                        focusedContainerColor   = WhiteBg.copy(alpha = 0.1f),
                        unfocusedContainerColor = WhiteBg.copy(alpha = 0.08f),
                        cursorColor             = SecondaryGold,
                        focusedTextColor        = SecondaryGold,
                        unfocusedTextColor      = SecondaryGold
                    )
                )
            }
        }

        // ── Filter chips ───────────────────────────────────────
        // Horizontally scrollable row of status filter chips
        // Each chip filters the list by opportunity status
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
                    "Open"      -> SuccessGreen
                    "Full"      -> Color(0xFF1976D2)
                    "Completed" -> TextMuted
                    else        -> PrimaryOlive
                }
                FilterChip(
                    selected = isSelected,
                    onClick  = { selectedFilter = filter },
                    label    = {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold
                            else FontWeight.Normal
                        )
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

        // ── Content ────────────────────────────────────────────
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
        } else if (filteredOpportunities.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = PrimaryOlive,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedFilter != "All")
                            "No opportunities match your filter"
                        else
                            "No opportunities posted yet",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedFilter != "All")
                            "Try a different filter or search term"
                        else
                            "Tap the button below to post your first opportunity",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    if (searchQuery.isEmpty() && selectedFilter == "All") {
                        Button(
                            onClick = { navController.navigate(ROUT_POST_OPPORTUNITY) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOrange
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = WhiteBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Post Opportunity",
                                color = WhiteBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(contentAlpha.value),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Results count label
                item {
                    Text(
                        text = "${filteredOpportunities.size} " +
                                if (filteredOpportunities.size == 1)
                                    "opportunity" else "opportunities",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Opportunity cards
                items(
                    items = filteredOpportunities,
                    key   = { it.id }
                ) { opp ->
                    FullOpportunityCard(
                        item = opp,
                        onClick = {
                            navController.navigate(
                                "$ROUT_OPPORTUNITY_DETAIL/${opp.id}"
                            )
                        },
                        onDelete = {
                            deleteTargetId  = opp.id
                            showDeleteDialog = true
                        },
                        onEdit = {
                            // TODO: Navigate to edit opportunity screen
                            // navController.navigate("edit_opportunity/${opp.id}")
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // ── FAB — Post New Opportunity ─────────────────────────────
    // Floating action button always visible at bottom right
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { navController.navigate(ROUT_POST_OPPORTUNITY) },
            modifier = Modifier.padding(20.dp),
            containerColor = AccentOrange,
            contentColor = WhiteBg,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Post Opportunity",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ── Full Opportunity Card ─────────────────────────────────────
// More detailed card than the dashboard preview card.
// Has edit and delete action buttons, full slot progress
// bar, type and mode chips and a QR code shortcut.
@Composable
fun FullOpportunityCard(
    item: OpportunityItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
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
    val slotProgress = if (item.slotsTotal > 0)
        item.slotsBooked.toFloat() / item.slotsTotal.toFloat()
    else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Title row + status chip ────────────────────────
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

            // ── Type chip ──────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (item.type == "volunteer")
                                PrimaryOlive.copy(alpha = 0.1f)
                            else AccentOrange.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (item.type == "volunteer")
                            "🤝 Volunteer" else "🏘 Community Service",
                        color = if (item.type == "volunteer")
                            PrimaryOlive else AccentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Date & Location ────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

            // ── Slot progress bar ──────────────────────────────
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
                        color = if (slotProgress >= 1f)
                            Color(0xFF1976D2) else PrimaryOlive,
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
                    color = if (slotProgress >= 1f)
                        Color(0xFF1976D2) else PrimaryOlive,
                    trackColor = FieldBorder
                )
            }

            HorizontalDivider(color = FieldBorder)

            // ── Action buttons ─────────────────────────────────
            // View details, Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View bookings / details
                OutlinedButton(
                    onClick = { onClick() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, PrimaryOlive.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = PrimaryOlive,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Details",
                        color = PrimaryOlive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Edit button
                OutlinedButton(
                    onClick = { onEdit() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, AccentOrange.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Edit",
                        color = AccentOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Delete button
                OutlinedButton(
                    onClick = { onDelete() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Delete",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Mini Stat Chip ────────────────────────────────────────────
// Small pill in the header showing counts per status.
// e.g. "3 Open", "1 Full", "2 Done"
@Composable
fun MiniStatChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WhiteBg.copy(alpha = 0.15f))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count.toString(),
                color = SecondaryGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = SecondaryGold.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyOpportunitiesScreenPreview() {
    MyOpportunitiesScreen(rememberNavController())
}