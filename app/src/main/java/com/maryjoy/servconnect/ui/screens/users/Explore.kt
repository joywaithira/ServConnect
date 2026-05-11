package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.FirebaseDatabase

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
private val ChipSelected  = Color(0xFF1A6B3C)
private val InputBg       = Color(0xFFEEF4F0)

// ─────────────────────────────────────────────────────────────────────────────
//  Data models
// ─────────────────────────────────────────────────────────────────────────────

data class FilterState(
    val type       : String  = "All",       // All | Volunteer | Community Service
    val groupType  : String  = "All",       // All | Individual | Group
    val location   : String  = "",
    val minHours   : Int     = 0,
    val maxHours   : Int     = 12,
    val sortBy     : String  = "Newest"     // Newest | Soonest | Most Spots
)

// Reuse OpportunityItem from HomeScreen (or move to a shared models file)
// For this file we re-declare a local alias so the file compiles standalone:
private data class ExploreOpportunity(
    val id           : String = "",
    val title           : String = "",
    val organization : String = "",
    val location     : String = "",
    val date         : String = "",
    val type         : String = "",
    val groupType    : String = "",   // "Individual" | "Group" | "Both"
    val category     : String = "",
    val emoji        : String = "",
    val spotsLeft    : Int = 0,
    val totalSpots   : Int = 0,
    val hours        : Int = 0,
    val cardGradient : List<Color> = listOf(Color(0xFF1A6B3C), Color(0xFF2E9B5C)),
    val orgRating    : Float = 0.0f,
    val isVerified   : Boolean = false
)

private val typeOptions      = listOf("All", "Volunteer", "Community Service")
private val groupOptions     = listOf("All", "Individual", "Group")
private val sortOptions      = listOf("Newest", "Soonest", "Most Spots")
private val locationSuggestions = listOf(
    "Westlands, Nairobi", "Karen, Nairobi", "Kilimani, Nairobi",
    "Kibera, Nairobi", "CBD, Nairobi", "Kasarani, Nairobi",
    "Parklands, Nairobi", "Ruaka, Nairobi", "Lang'ata, Nairobi"
)

// ─────────────────────────────────────────────────────────────────────────────
//  Root composable
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit,
    onOpportunityClick: (String) -> Unit
) {
    val database = remember { FirebaseDatabase.getInstance() }
    var allOpportunities by remember { mutableStateOf<List<ExploreOpportunity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        database.getReference("opportunities").get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<ExploreOpportunity>()
            for (child in snapshot.children) {
                list.add(ExploreOpportunity(
                    id = child.child("id").value?.toString() ?: "",
                    title = child.child("title").value?.toString() ?: "",
                    organization = child.child("organization").value?.toString() ?: "",
                    location = child.child("location").value?.toString() ?: "",
                    date = child.child("date").value?.toString() ?: "",
                    type = child.child("type").value?.toString() ?: "Volunteer",
                    groupType = child.child("groupType").value?.toString() ?: "Individual",
                    category = child.child("category").value?.toString() ?: "General",
                    emoji = child.child("emoji").value?.toString() ?: "👋",
                    spotsLeft = (child.child("spotsLeft").value as? Long)?.toInt() ?: 0,
                    totalSpots = (child.child("totalSpots").value as? Long)?.toInt() ?: 0,
                    hours = (child.child("hours").value as? Long)?.toInt() ?: 0,
                    orgRating = (child.child("orgRating").value as? Double)?.toFloat() ?: 0.0f,
                    isVerified = child.child("isVerified").value as? Boolean ?: false
                ))
            }
            allOpportunities = list
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }

    var searchQuery     by remember { mutableStateOf("") }
    var filterState     by remember { mutableStateOf(FilterState()) }
    var showFilters     by remember { mutableStateOf(false) }
    var showLocationSug by remember { mutableStateOf(false) }
    val bookmarked       = remember { mutableStateListOf<String>() }
    val focusManager     = LocalFocusManager.current
    val listState        = rememberLazyListState()

    // Active filter count badge
    val activeFilters = listOf(
        filterState.type != "All",
        filterState.groupType != "All",
        filterState.location.isNotBlank(),
        filterState.sortBy != "Newest"
    ).count { it }

    // Filtered + searched results
    val results = remember(searchQuery, filterState) {
        allOpportunities
            .filter { opp ->
                val q = searchQuery.lowercase()
                (q.isBlank() || opp.title.lowercase().contains(q) ||
                        opp.organization.lowercase().contains(q) ||
                        opp.category.lowercase().contains(q) ||
                        opp.location.lowercase().contains(q))
            }
            .filter { opp ->
                filterState.type == "All" || opp.type == filterState.type
            }
            .filter { opp ->
                filterState.groupType == "All" ||
                        opp.groupType == filterState.groupType ||
                        opp.groupType == "Both"
            }
            .filter { opp ->
                filterState.location.isBlank() ||
                        opp.location.lowercase().contains(filterState.location.lowercase())
            }
            .filter { opp ->
                opp.hours in filterState.minHours..filterState.maxHours
            }
            .let { list ->
                when (filterState.sortBy) {
                    "Most Spots" -> list.sortedByDescending { it.spotsLeft }
                    else         -> list
                }
            }
    }

    Scaffold(
        containerColor = SurfaceBg,
        bottomBar = {
            // No bottom bar here since we are using callbacks or we can keep it if we pass it
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state         = listState,
                modifier      = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // ── Top bar ──
                item {
                    ExploreTopBar(onBack = onNavigateBack)
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GreenPrimary)
                        }
                    }
                }

                // ── Search bar ──
                item {
                    SearchBar(
                        query          = searchQuery,
                        onQueryChange  = { searchQuery = it; showLocationSug = false },
                        onSearch       = { focusManager.clearFocus(); showLocationSug = false },
                        activeFilters  = activeFilters,
                        onFilterClick  = { showFilters = true; focusManager.clearFocus() }
                    )
                }

                // ── Location search suggestions dropdown ──
                if (showLocationSug && searchQuery.isBlank()) {
                    item {
                        LocationSuggestionsCard(
                            suggestions = locationSuggestions,
                            onPick = { loc ->
                                filterState = filterState.copy(location = loc)
                                showLocationSug = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }

                // ── Active filter chips (when filters applied) ──
                if (activeFilters > 0) {
                    item {
                        ActiveFilterChips(
                            filterState = filterState,
                            onClearType       = { filterState = filterState.copy(type = "All") },
                            onClearGroup      = { filterState = filterState.copy(groupType = "All") },
                            onClearLocation   = { filterState = filterState.copy(location = "") },
                            onClearSort       = { filterState = filterState.copy(sortBy = "Newest") },
                            onClearAll        = { filterState = FilterState() }
                        )
                    }
                }

                // ── Results header ──
                item {
                    ResultsHeader(count = results.size, query = searchQuery)
                }

                // ── No results state ──
                if (results.isEmpty()) {
                    item { EmptyState(onReset = { searchQuery = ""; filterState = FilterState() }) }
                }

                // ── Result cards ──
                itemsIndexed(results) { index, opp ->
                    ExploreOpportunityCard(
                        opp          = opp,
                        animIndex    = index,
                        isBookmarked = bookmarked.contains(opp.id),
                        onBookmark   = {
                            if (bookmarked.contains(opp.id)) bookmarked.remove(opp.id)
                            else bookmarked.add(opp.id)
                        },
                        onClick = { onOpportunityClick(opp.id) }
                    )
                }
            }

            // ── Filter bottom sheet overlay ──
            if (showFilters) {
                FilterBottomSheet(
                    current  = filterState,
                    onApply  = { newState -> filterState = newState; showFilters = false },
                    onDismiss = { showFilters = false }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Top bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Explore",
                fontSize     = 26.sp,
                fontWeight   = FontWeight.ExtraBold,
                color        = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                "Find your next opportunity",
                fontSize = 13.sp,
                color    = TextSecondary
            )
        }
        // Map view button
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(CardWhite, CircleShape)
                .border(1.dp, DividerColor, CircleShape)
                .clickable { /* future: map view */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Map,
                contentDescription = "Map View",
                tint     = GreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Search bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query         : String,
    onQueryChange : (String) -> Unit,
    onSearch      : () -> Unit,
    activeFilters : Int,
    onFilterClick : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Search input
        Row(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .background(InputBg, RoundedCornerShape(14.dp))
                .border(1.dp, if (query.isNotBlank()) GreenPrimary.copy(alpha = 0.4f)
                else Color.Transparent, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint     = if (query.isNotBlank()) GreenPrimary else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            androidx.compose.foundation.text.BasicTextField(
                value         = query,
                onValueChange = onQueryChange,
                modifier      = Modifier.weight(1f),
                singleLine    = true,
                textStyle     = androidx.compose.ui.text.TextStyle(
                    fontSize   = 14.sp,
                    color      = TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox   = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            "Search by title, org or skill…",
                            fontSize = 14.sp,
                            color    = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                    inner()
                }
            )
            if (query.isNotBlank()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Clear",
                    tint     = TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }

        // Filter button
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    if (activeFilters > 0) GreenPrimary else CardWhite,
                    RoundedCornerShape(14.dp)
                )
                .border(
                    1.dp,
                    if (activeFilters > 0) Color.Transparent else DividerColor,
                    RoundedCornerShape(14.dp)
                )
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = "Filters",
                tint     = if (activeFilters > 0) Color.White else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            // Badge
            if (activeFilters > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(GoldAccent, CircleShape)
                        .border(1.5.dp, GreenPrimary, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        activeFilters.toString(),
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Location suggestions dropdown
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LocationSuggestionsCard(
    suggestions : List<String>,
    onPick      : (String) -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                "  📍 Nearby areas",
                fontSize   = 11.sp,
                color      = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            suggestions.forEach { loc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(loc) }
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.LocationOn, null,
                        tint = GreenAccent, modifier = Modifier.size(15.dp))
                    Text(loc, fontSize = 13.sp, color = TextPrimary)
                }
                if (loc != suggestions.last()) {
                    Divider(color = DividerColor, thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Active filter chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveFilterChips(
    filterState     : FilterState,
    onClearType     : () -> Unit,
    onClearGroup    : () -> Unit,
    onClearLocation : () -> Unit,
    onClearSort     : () -> Unit,
    onClearAll      : () -> Unit
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Clear all
        item {
            FilterChipPill(
                label     = "Clear all",
                icon      = "✕",
                highlight = true,
                onClick   = onClearAll
            )
        }
        if (filterState.type != "All") item {
            FilterChipPill(label = filterState.type, icon = "✕", onClick = onClearType)
        }
        if (filterState.groupType != "All") item {
            FilterChipPill(label = filterState.groupType, icon = "✕", onClick = onClearGroup)
        }
        if (filterState.location.isNotBlank()) item {
            FilterChipPill(label = filterState.location.substringBefore(","), icon = "✕", onClick = onClearLocation)
        }
        if (filterState.sortBy != "Newest") item {
            FilterChipPill(label = "Sort: ${filterState.sortBy}", icon = "✕", onClick = onClearSort)
        }
    }
}

@Composable
private fun FilterChipPill(
    label     : String,
    icon      : String,
    highlight : Boolean = false,
    onClick   : () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (highlight) GreenPrimary else GreenPrimary.copy(alpha = 0.08f))
            .border(1.dp, if (highlight) Color.Transparent else GreenPrimary.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (highlight) Color.White else GreenPrimary
        )
        Text(
            icon,
            fontSize = 11.sp,
            color    = if (highlight) Color.White.copy(alpha = 0.8f) else GreenPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Results header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ResultsHeader(count: Int, query: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            if (query.isNotBlank()) "\"$query\" · $count found"
            else "$count opportunities",
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary
        )
        // View toggle (list only for now, grid in future)
        Box(
            modifier = Modifier
                .background(InputBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                Icons.Outlined.ViewList,
                contentDescription = null,
                tint     = GreenPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Explore opportunity card  (richer than home list card)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreOpportunityCard(
    opp          : ExploreOpportunity,
    animIndex    : Int,
    isBookmarked : Boolean,
    onBookmark   : () -> Unit,
    onClick      : () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animIndex * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(280)) + slideInVertically(
            initialOffsetY = { 50 },
            animationSpec  = tween(280, easing = EaseOutCubic)
        )
    ) {
        Card(
            onClick   = onClick,
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp, pressedElevation = 5.dp)
        ) {
            Column {
                // ── Coloured header strip ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            Brush.linearGradient(
                                colors = opp.cardGradient,
                                start  = Offset(0f, 0f),
                                end    = Offset(Float.POSITIVE_INFINITY, 0f)
                            )
                        )
                ) {
                    // Decorative dot pattern
                    Canvas(Modifier.matchParentSize()) {
                        repeat(5) { i ->
                            drawCircle(
                                color  = Color.White.copy(alpha = 0.06f),
                                radius = (30 + i * 20).dp.toPx(),
                                center = Offset(size.width * 0.75f + i * 10, size.height * 0.5f)
                            )
                        }
                    }
                    Row(
                        modifier              = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(opp.emoji, fontSize = 30.sp)
                            Column {
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        opp.organization,
                                        fontSize   = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color.White
                                    )
                                    if (opp.isVerified) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.22f), CircleShape)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("✓ Verified", fontSize = 9.sp,
                                                color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                // Star rating
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { star ->
                                        Text(
                                            if (star < opp.orgRating.toInt()) "★" else "☆",
                                            fontSize = 11.sp,
                                            color    = if (star < opp.orgRating.toInt()) GoldAccent
                                            else Color.White.copy(alpha = 0.4f)
                                        )
                                    }
                                    Text(
                                        " ${opp.orgRating}",
                                        fontSize = 10.sp,
                                        color    = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        // Bookmark
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable { onBookmark() },
                            contentAlignment = Alignment.Center
                        ) {
                            val tint by animateColorAsState(
                                if (isBookmarked) GoldAccent else Color.White.copy(alpha = 0.7f),
                                label = "bm"
                            )
                            Icon(
                                if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                null, tint = tint, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── Card body ──
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        opp.title,
                        fontSize    = 15.sp,
                        fontWeight  = FontWeight.ExtraBold,
                        color       = TextPrimary,
                        maxLines    = 2,
                        overflow    = TextOverflow.Ellipsis,
                        lineHeight  = 20.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    // Meta grid
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExploreMetaPill("📍", opp.location.substringBefore(","),
                            Modifier.weight(1f))
                        ExploreMetaPill("📅", opp.date, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExploreMetaPill("⏱", "${opp.hours} hrs", Modifier.weight(1f))
                        ExploreMetaPill(
                            if (opp.groupType == "Group") "👥" else if (opp.groupType == "Individual") "👤" else "👥",
                            opp.groupType, Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Divider(color = DividerColor, thickness = 0.8.dp)
                    Spacer(Modifier.height(12.dp))

                    // Bottom row
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Type badge
                        ExploreTypeBadge(type = opp.type)

                        // Spots + CTA
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val spotsColor = when {
                                opp.spotsLeft <= 2 -> Color(0xFFE53935)
                                opp.spotsLeft <= 5 -> GoldAccent
                                else               -> GreenPrimary
                            }
                            Text(
                                "${opp.spotsLeft} spots left",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = spotsColor
                            )
                            Box(
                                modifier = Modifier
                                    .background(GreenPrimary, RoundedCornerShape(10.dp))
                                    .clickable(onClick = onClick)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "View",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreMetaPill(icon: String, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier
            .background(InputBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 11.sp)
        Text(
            text,
            fontSize   = 11.sp,
            color      = TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExploreTypeBadge(type: String) {
    val (bg, fg, icon) = when (type) {
        "Volunteer"         -> Triple(Color(0xFFE8F5E9), GreenPrimary, "🙋")
        "Community Service" -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "🤝")
        else                -> Triple(Color(0xFFEEEEEE), TextSecondary, "📌")
    }
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 11.sp)
        Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(onReset: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 52.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "No results found",
            fontSize   = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Try adjusting your search or clearing your filters.",
            fontSize  = 13.sp,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onReset,
            shape   = RoundedCornerShape(12.dp),
            colors  = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text("Reset filters", fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Filter bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FilterBottomSheet(
    current  : FilterState,
    onApply  : (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(current) }

    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                indication           = null,
                interactionSource    = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick              = onDismiss
            )
    )

    // Sheet
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(bottom = 32.dp)
                .clickable(
                    indication        = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick           = {}
                )
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerColor, CircleShape)
            )

            // Header
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Filters",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = TextPrimary
                )
                TextButton(onClick = { draft = FilterState() }) {
                    Text("Reset", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            Divider(color = DividerColor)

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Type
                FilterSection(title = "Opportunity Type") {
                    FilterOptionRow(
                        options  = typeOptions,
                        selected = draft.type,
                        onSelect = { draft = draft.copy(type = it) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Group type
                FilterSection(title = "Participation") {
                    FilterOptionRow(
                        options  = groupOptions,
                        selected = draft.groupType,
                        onSelect = { draft = draft.copy(groupType = it) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Location
                FilterSection(title = "Location") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.LocationOn, null,
                            tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value         = draft.location,
                            onValueChange = { draft = draft.copy(location = it) },
                            singleLine    = true,
                            textStyle     = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp, color = TextPrimary
                            ),
                            decorationBox = { inner ->
                                if (draft.location.isEmpty()) Text("e.g. Westlands, Nairobi",
                                    fontSize = 14.sp, color = TextSecondary)
                                inner()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Sort by
                FilterSection(title = "Sort By") {
                    FilterOptionRow(
                        options  = sortOptions,
                        selected = draft.sortBy,
                        onSelect = { draft = draft.copy(sortBy = it) }
                    )
                }

                Spacer(Modifier.height(28.dp))

                // Apply button
                Button(
                    onClick  = { onApply(draft) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        "Apply Filters",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        fontSize   = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color      = TextPrimary
    )
    Spacer(Modifier.height(10.dp))
    content()
}

@Composable
private fun FilterOptionRow(
    options  : List<String>,
    selected : String,
    onSelect : (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val isSel = opt == selected
            val bg by animateColorAsState(
                if (isSel) GreenPrimary else InputBg, label = "opt_bg")
            val fg by animateColorAsState(
                if (isSel) Color.White else TextSecondary, label = "opt_fg")
            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(10.dp))
                    .border(1.dp, if (isSel) Color.Transparent else DividerColor, RoundedCornerShape(10.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(opt, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Bottom nav (index 1 = Explore active)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreBottomNav(navController: NavController) {
    Surface(
        shadowElevation = 12.dp,
        color           = CardWhite,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data class NI(val label: String, val icon: ImageVector, val route: String)
            val items = listOf(
                NI("Home",      Icons.Outlined.Home,               "volunteer_home"),
                NI("Explore",   Icons.Filled.Search,               "explore"),
                NI("Bookings",  Icons.Outlined.DateRange,          "my_bookings"),
                NI("Portfolio", Icons.Outlined.WorkspacePremium,   "service_portfolio"),
                NI("Profile",   Icons.Outlined.Person,             "my_profile")
            )
            items.forEachIndexed { i, item ->
                val isSelected = i == 1  // Explore tab
                val iconColor by animateColorAsState(
                    if (isSelected) GreenPrimary else TextSecondary.copy(alpha = 0.5f),
                    label = "nav_ic"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) GreenPrimary.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
                            if (i != 1) navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(item.icon, item.label, tint = iconColor, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(item.label, fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = iconColor)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview(){

    ExploreScreen(onNavigateBack = {}, onOpportunityClick = {})


}
