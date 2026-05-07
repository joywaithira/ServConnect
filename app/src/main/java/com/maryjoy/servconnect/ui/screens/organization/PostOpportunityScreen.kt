package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_MY_OPPORTUNITIES
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
private val FieldBorder   = Color(0xFFE0E0E0)
private val ErrorRed      = Color(0xFFD32F2F)
private val OrangeBg      = Color(0xFFFFF3EE)
private val SuccessGreen  = Color(0xFF388E3C)

@Composable
fun PostOpportunityScreen(navController: NavController) {

    // ── Form State ────────────────────────────────────────────
    // Every field the org fills in for their opportunity
    var title           by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var date            by remember { mutableStateOf("") }
    var startTime       by remember { mutableStateOf("") }
    var endTime         by remember { mutableStateOf("") }
    var slots           by remember { mutableStateOf("") }
    var requirements    by remember { mutableStateOf("") }

    // Type: volunteer or community service
    // Controls which chip is selected
    var selectedType    by remember { mutableStateOf("volunteer") }

    // Individual vs Group opportunity
    var selectedMode    by remember { mutableStateOf("individual") }

    // Interest/category tags the org selects
    // These are used to match volunteers by interest
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }

    // ── Error State ───────────────────────────────────────────
    var titleError       by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var locationError    by remember { mutableStateOf("") }
    var dateError        by remember { mutableStateOf("") }
    var startTimeError   by remember { mutableStateOf("") }
    var endTimeError     by remember { mutableStateOf("") }
    var slotsError       by remember { mutableStateOf("") }
    var firebaseError    by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(false) }
    var isSuccess        by remember { mutableStateOf(false) }

    // ── Available interest tags ───────────────────────────────
    // These are the categories volunteers filter by.
    // The org picks which ones apply to their opportunity
    // so it shows up in the right volunteer searches.
    val interestTags = listOf(
        "Children 👦", "Elderly 👴", "Animals 🐾",
        "Environment 🌿", "Education 📚", "Health 🏥",
        "Food & Hunger 🍽", "Disability Support ♿",
        "Arts & Culture 🎨", "Sports 🏆",
        "Disaster Relief 🆘", "General 🤝"
    )

    // ── Entrance Animation ────────────────────────────────────
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { contentAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { contentSlide.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
    }

    // ── Validation ────────────────────────────────────────────
    // Checks all required fields before calling Firebase.
    // Returns true only if everything is valid.
    fun validate(): Boolean {
        var valid = true

        titleError = when {
            title.isBlank()        -> { valid = false; "Title is required" }
            title.trim().length < 5 -> { valid = false; "Title must be at least 5 characters" }
            else -> ""
        }

        descriptionError = when {
            description.isBlank()         -> { valid = false; "Description is required" }
            description.trim().length < 20 -> { valid = false; "Description must be at least 20 characters" }
            else -> ""
        }

        locationError = when {
            location.isBlank() -> { valid = false; "Location is required" }
            else -> ""
        }

        dateError = when {
            date.isBlank() -> { valid = false; "Date is required" }
            else -> ""
        }

        startTimeError = when {
            startTime.isBlank() -> { valid = false; "Start time is required" }
            else -> ""
        }

        endTimeError = when {
            endTime.isBlank() -> { valid = false; "End time is required" }
            else -> ""
        }

        slotsError = when {
            slots.isBlank()                 -> { valid = false; "Number of slots is required" }
            slots.toIntOrNull() == null     -> { valid = false; "Enter a valid number" }
            (slots.toIntOrNull() ?: 0) < 1  -> { valid = false; "Must have at least 1 slot" }
            else -> ""
        }

        return valid
    }

    // ── Post to Firebase ──────────────────────────────────────
    // Called after validate() returns true.
    // Saves the full opportunity to the "opportunities"
    // collection in Firestore with the org's uid attached
    // so we can filter by org later on the dashboard.
    fun postOpportunity() {
        isLoading = true
        firebaseError = ""

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            isLoading = false
            firebaseError = "Session expired. Please log in again."
            return
        }

        // Fetch the org's name to store with the opportunity
        // so volunteers can see who posted it without
        // needing a separate lookup every time
        FirebaseFirestore.getInstance()
            .collection("organizations")
            .document(uid)
            .get()
            .addOnSuccessListener { orgDoc ->

                val orgName = orgDoc.getString("orgName") ?: "Unknown Organization"

                // Build the opportunity document
                val opportunity = hashMapOf(
                    // Who posted it
                    "orgId"          to uid,
                    "orgName"        to orgName,

                    // Core details
                    "title"          to title.trim(),
                    "description"    to description.trim(),
                    "location"       to location.trim(),
                    "date"           to date.trim(),
                    "startTime"      to startTime.trim(),
                    "endTime"        to endTime.trim(),
                    "requirements"   to requirements.trim(),

                    // Type and mode
                    // type: "volunteer" or "community"
                    // mode: "individual" or "group"
                    "type"           to selectedType,
                    "mode"           to selectedMode,

                    // Interest tags for volunteer matching
                    "interests"      to selectedInterests.toList(),

                    // Slot management
                    // slotsBooked starts at 0 and increases
                    // as volunteers book. When slotsBooked
                    // equals slotsTotal status becomes "full"
                    "slotsTotal"     to (slots.toIntOrNull() ?: 0),
                    "slotsBooked"    to 0,

                    // Status starts as "open".
                    // Changes to "full" when all slots are taken.
                    // Changes to "completed" after the event date passes.
                    "status"         to "open",

                    "createdAt"      to System.currentTimeMillis()
                )

                // Save to Firestore under the "opportunities" collection.
                // Firestore auto-generates the document ID.
                FirebaseFirestore.getInstance()
                    .collection("opportunities")
                    .add(opportunity)
                    .addOnSuccessListener {
                        isLoading = false
                        isSuccess = true
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        firebaseError = "Failed to post opportunity: ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                isLoading = false
                firebaseError = "Failed to fetch org details: ${e.message}"
            }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // ── Top Header ─────────────────────────────────────
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back button
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = SecondaryGold
                        )
                    }

                    Column {
                        Text(
                            text = "Post an Opportunity",
                            color = SecondaryGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fill in the details below",
                            color = SecondaryGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Success State ──────────────────────────────────
            // Replaces the form once the post is successful.
            // Shows a confirmation and lets the org navigate
            // to their opportunities list or post another one.
            if (isSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Opportunity Posted!",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your opportunity is now live.\nVolunteers can find and book it.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.navigate(ROUT_MY_OPPORTUNITIES) {
                                popUpTo(ROUT_MY_OPPORTUNITIES) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryOlive
                        )
                    ) {
                        Text(
                            text = "View My Opportunities",
                            color = SecondaryGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            // Reset all fields to post another one
                            title = ""; description = ""; location = ""
                            date = ""; startTime = ""; endTime = ""
                            slots = ""; requirements = ""
                            selectedType = "volunteer"
                            selectedMode = "individual"
                            selectedInterests = emptySet()
                            isSuccess = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, AccentOrange
                        )
                    ) {
                        Text(
                            text = "Post Another Opportunity",
                            color = AccentOrange,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else {

                // ── Form ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .alpha(contentAlpha.value)
                        .offset(y = contentSlide.value.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Section 1: Basic Details ───────────────
                    FormCard {
                        SectionLabel("Basic Details", PrimaryOlive)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Title
                        PostField(
                            value = title,
                            onValueChange = { title = it; titleError = "" },
                            label = "Opportunity Title",
                            placeholder = "e.g. Teaching Assistant at Sunshine Primary",
                            icon = Icons.Default.Title,
                            error = titleError
                        )

                        // Description — multi-line
                        PostMultilineField(
                            value = description,
                            onValueChange = { description = it; descriptionError = "" },
                            label = "Description",
                            placeholder = "Describe what volunteers will be doing, " +
                                    "what impact they will make and what to expect...",
                            error = descriptionError,
                            charCount = description.length,
                            minChars = 20
                        )

                        // Requirements — optional
                        PostMultilineField(
                            value = requirements,
                            onValueChange = { requirements = it },
                            label = "Requirements (Optional)",
                            placeholder = "Any skills, age requirements, " +
                                    "dress code or items to bring...",
                            error = "",
                            charCount = null,
                            minChars = 0
                        )
                    }

                    // ── Section 2: Type & Mode ─────────────────
                    FormCard {
                        SectionLabel("Opportunity Type", PrimaryOlive)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Volunteer vs Community Service toggle
                        Text(
                            text = "What kind of opportunity is this?",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TypeChip(
                                label = "🤝 Volunteer",
                                selected = selectedType == "volunteer",
                                selectedColor = PrimaryOlive,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = "volunteer" }
                            )
                            TypeChip(
                                label = "🏘 Community Service",
                                selected = selectedType == "community",
                                selectedColor = AccentOrange,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = "community" }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Individual vs Group toggle
                        Text(
                            text = "Who can join?",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TypeChip(
                                label = "👤 Individual",
                                selected = selectedMode == "individual",
                                selectedColor = PrimaryOlive,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = "individual" }
                            )
                            TypeChip(
                                label = "👥 Group",
                                selected = selectedMode == "group",
                                selectedColor = PrimaryOlive,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = "group" }
                            )
                        }
                    }

                    // ── Section 3: Location & Schedule ────────
                    FormCard {
                        SectionLabel("Location & Schedule", PrimaryOlive)

                        Spacer(modifier = Modifier.height(4.dp))

                        PostField(
                            value = location,
                            onValueChange = { location = it; locationError = "" },
                            label = "Location",
                            placeholder = "e.g. Kibera, Nairobi",
                            icon = Icons.Default.LocationOn,
                            error = locationError
                        )

                        // Date field
                        // In a real app this would open a date picker.
                        // For now it's a text field with a calendar icon.
                        PostField(
                            value = date,
                            onValueChange = { date = it; dateError = "" },
                            label = "Date",
                            placeholder = "e.g. 25 June 2025",
                            icon = Icons.Default.CalendarToday,
                            error = dateError
                        )

                        // Start and end time side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PostField(
                                value = startTime,
                                onValueChange = { startTime = it; startTimeError = "" },
                                label = "Start Time",
                                placeholder = "e.g. 9:00 AM",
                                icon = Icons.Default.AccessTime,
                                error = startTimeError,
                                modifier = Modifier.weight(1f)
                            )
                            PostField(
                                value = endTime,
                                onValueChange = { endTime = it; endTimeError = "" },
                                label = "End Time",
                                placeholder = "e.g. 1:00 PM",
                                icon = Icons.Default.AccessTime,
                                error = endTimeError,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Section 4: Slots ───────────────────────
                    FormCard {
                        SectionLabel("Volunteer Slots", PrimaryOlive)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "How many volunteers do you need?",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        PostField(
                            value = slots,
                            onValueChange = { slots = it; slotsError = "" },
                            label = "Number of Slots",
                            placeholder = "e.g. 20",
                            icon = Icons.Default.Group,
                            error = slotsError,
                            keyboardType = KeyboardType.Number
                        )

                        // Info note explaining slot logic
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightGold)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "ℹ  Once all slots are filled the opportunity " +
                                        "will automatically show as \"Full\" to volunteers.",
                                color = PrimaryOlive,
                                fontSize = 11.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // ── Section 5: Interest Tags ───────────────
                    // These tags link the opportunity to volunteer
                    // interests so it shows up in their filtered search
                    FormCard {
                        SectionLabel("Interest Tags", PrimaryOlive)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Select all categories that apply " +
                                    "so volunteers can find this opportunity:",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wrap the tags in a flow-like grid
                        // using chunked rows
                        interestTags.chunked(3).forEach { rowTags ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowTags.forEach { tag ->
                                    val isSelected = tag in selectedInterests
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedInterests = if (isSelected)
                                                selectedInterests - tag
                                            else
                                                selectedInterests + tag
                                        },
                                        label = {
                                            Text(
                                                text = tag,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryOlive,
                                            selectedLabelColor = SecondaryGold,
                                            containerColor = Color(0xFFF5F5F5),
                                            labelColor = TextMuted
                                        )
                                    )
                                }
                                // Fill remaining space if row has < 3 items
                                repeat(3 - rowTags.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // ── Firebase Error Display ─────────────────
                    if (firebaseError.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    ErrorRed.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚠ $firebaseError",
                                color = ErrorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // ── Submit Button ──────────────────────────
                    Button(
                        onClick = {
                            if (validate()) postOpportunity()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentOrange,
                            disabledContainerColor = AccentOrange.copy(alpha = 0.5f)
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = WhiteBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = WhiteBg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Post Opportunity",
                                    color = WhiteBg,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── Reusable: Form Card wrapper ───────────────────────────────
// White rounded card that wraps each section of the form.
// Keeps each section visually separated and clean.
@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

// ── Reusable: Post Opportunity Text Field ─────────────────────
// Single-line branded field for this screen.
// Border and label turn olive when filled, red on error.
@Composable
fun PostField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val hasError = error.isNotEmpty()
    val isFilled = value.isNotEmpty()
    val borderColor = when {
        hasError -> ErrorRed
        isFilled -> PrimaryOlive
        else     -> FieldBorder
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            color = if (isFilled) PrimaryOlive else TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
            placeholder = {
                Text(text = placeholder, color = TextMuted, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFilled) PrimaryOlive else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = LightGold.copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFFF9F9F9),
                cursorColor             = PrimaryOlive,
                focusedTextColor        = TextDark,
                unfocusedTextColor      = TextDark
            )
        )

        if (hasError) {
            Text(
                text = "⚠ $error",
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ── Reusable: Multi-line Post Field ───────────────────────────
// Used for description and requirements.
// Shows a character count below the field.
@Composable
fun PostMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String,
    charCount: Int?,
    minChars: Int
) {
    val hasError = error.isNotEmpty()
    val isFilled = value.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            color = if (isFilled) PrimaryOlive else TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .border(
                    1.5.dp,
                    when {
                        hasError -> ErrorRed
                        isFilled -> PrimaryOlive
                        else     -> FieldBorder
                    },
                    RoundedCornerShape(12.dp)
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = LightGold.copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFFF9F9F9),
                cursorColor             = PrimaryOlive,
                focusedTextColor        = TextDark,
                unfocusedTextColor      = TextDark
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (hasError) {
                Text(
                    text = "⚠ $error",
                    color = ErrorRed,
                    fontSize = 11.sp
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (charCount != null) {
                Text(
                    text = "$charCount chars " +
                            if (charCount < minChars) "· min $minChars required"
                            else "✓",
                    color = if (charCount >= minChars) SuccessGreen else TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ── Reusable: Type / Mode Chip ────────────────────────────────
// Selectable chip for type (volunteer/community)
// and mode (individual/group) toggles.
// Selected state shows a solid colored background.
@Composable
fun TypeChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) selectedColor
                else Color(0xFFF5F5F5)
            )
            .border(
                1.5.dp,
                if (selected) selectedColor else FieldBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) WhiteBg else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostOpportunityScreenPreview() {
    PostOpportunityScreen(rememberNavController())
}