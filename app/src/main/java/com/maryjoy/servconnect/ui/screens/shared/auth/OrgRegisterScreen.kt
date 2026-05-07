package com.maryjoy.servconnect.ui.screens.shared.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.R
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
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

@Composable
fun OrgRegisterScreen(navController: NavController) {

    // ── Form State ────────────────────────────────────────────
    // These variables hold whatever the user types into each field
    var orgName         by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var location        by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible     by remember { mutableStateOf(false) }
    var confirmPassVisible  by remember { mutableStateOf(false) }

    // ── Error State ───────────────────────────────────────────
    // Each field has its own error message, shown below the field
    var orgNameError        by remember { mutableStateOf("") }
    var emailError          by remember { mutableStateOf("") }
    var phoneError          by remember { mutableStateOf("") }
    var locationError       by remember { mutableStateOf("") }
    var descriptionError    by remember { mutableStateOf("") }
    var passwordError       by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var firebaseError       by remember { mutableStateOf("") }
    var isLoading           by remember { mutableStateOf(false) }

    // ── Entrance Animations ───────────────────────────────────
    // Controls the fade-in and slide-up effect when screen opens
    val headerAlpha = remember { Animatable(0f) }
    val formAlpha   = remember { Animatable(0f) }
    val formSlide   = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { formAlpha.animateTo(1f, tween(700, delayMillis = 300)) }
        launch { formSlide.animateTo(0f, tween(700, delayMillis = 300, easing = FastOutSlowInEasing)) }
    }

    // ── Validation ────────────────────────────────────────────
    // Checks all fields before allowing Firebase registration
    fun validate(): Boolean {
        var valid = true

        orgNameError = when {
            orgName.isBlank()        -> { valid = false; "Organization name is required" }
            orgName.trim().length < 3 -> { valid = false; "Name must be at least 3 characters" }
            else -> ""
        }

        emailError = when {
            email.isBlank() -> { valid = false; "Email address is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            { valid = false; "Enter a valid email address" }
            else -> ""
        }

        phoneError = when {
            phone.isBlank()      -> { valid = false; "Phone number is required" }
            phone.length < 10    -> { valid = false; "Enter a valid phone number" }
            else -> ""
        }

        locationError = when {
            location.isBlank() -> { valid = false; "Physical location is required" }
            else -> ""
        }

        descriptionError = when {
            description.isBlank()        -> { valid = false; "Please describe your organization" }
            description.trim().length < 20 -> { valid = false; "Description must be at least 20 characters" }
            else -> ""
        }

        passwordError = when {
            password.isBlank()  -> { valid = false; "Password is required" }
            password.length < 8 -> { valid = false; "Password must be at least 8 characters" }
            else -> ""
        }

        confirmPasswordError = when {
            confirmPassword.isBlank()   -> { valid = false; "Please confirm your password" }
            confirmPassword != password -> { valid = false; "Passwords do not match" }
            else -> ""
        }

        return valid
    }

    // ── Firebase Registration ─────────────────────────────────
    // This is called when the form is valid and button is pressed
    fun registerOrganization() {
        isLoading = true
        firebaseError = ""

        // Step 1: Create the auth account using email + password
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                // Step 2: Get the unique ID Firebase gave this account
                val uid = authResult.user?.uid ?: run {
                    isLoading = false
                    firebaseError = "Something went wrong. Please try again."
                    return@addOnSuccessListener
                }

                // Step 3: Build the organization profile to save in Firestore
                // This is the data that will appear on their profile in the app
                val orgProfile = hashMapOf(
                    "uid"            to uid,
                    "orgName"        to orgName.trim(),
                    "email"          to email.trim(),
                    "phone"          to phone.trim(),
                    "location"       to location.trim(),
                    "description"    to description.trim(),
                    "role"           to "organization",

                    // Organizations start as "pending" — Admin must verify
                    // before they can post opportunities
                    "status"         to "pending",
                    "isVerified"     to false,
                    "createdAt"      to System.currentTimeMillis()
                )

                // Step 4: Save the profile to the "organizations" collection
                // in Firestore. The document ID is the uid so we can
                // easily look up any org by their auth uid later
                FirebaseFirestore.getInstance()
                    .collection("organizations")
                    .document(uid)
                    .set(orgProfile)
                    .addOnSuccessListener {

                        // Step 5: Registration complete!
                        // Navigate to a "Pending Verification" screen
                        // so the org knows they need to wait for admin approval
                        isLoading = false
                        navController.navigate("pending_verification") {
                            // Clear the back stack so they can't
                            // go back to the registration form
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Firestore save failed
                        isLoading = false
                        firebaseError = "Failed to save organization profile. ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                // Firebase Auth account creation failed
                isLoading = false
                firebaseError = when {
                    e.message?.contains("email address is already in use") == true ->
                        "An account with this email already exists"
                    e.message?.contains("network") == true ->
                        "No internet connection. Please try again"
                    else ->
                        e.message ?: "Registration failed. Please try again"
                }
            }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {

        // Orange-tinted olive gradient header for organizations
        // (slightly different from volunteer's pure olive —
        // this visually distinguishes the two flows)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, Color(0xFF5C4A1E))
                    )
                )
        )

        // White curved cutout at bottom of header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(WhiteBg)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Back Button ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                    .alpha(headerAlpha.value),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WhiteBg.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SecondaryGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Header ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(headerAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_servconnect),
                    contentDescription = "ServConnect Logo",
                    modifier = Modifier.size(95.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Organization Registration",
                    color = SecondaryGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Orange badge to distinguish from volunteer flow
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🏢  Joining as an Organization",
                        color = SecondaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Form Card ──────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = formSlide.value.dp)
                    .alpha(formAlpha.value)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = AccentOrange.copy(alpha = 0.10f),
                        spotColor = AccentOrange.copy(alpha = 0.10f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    // ── Section 1: Organization Details ────────
                    SectionLabel(
                        title = "Organization Details",
                        color = AccentOrange
                    )

                    OrgField(
                        value = orgName,
                        onValueChange = { orgName = it; orgNameError = "" },
                        label = "Organization Name",
                        placeholder = "e.g. Kenya Red Cross",
                        icon = Icons.Default.Business,
                        error = orgNameError,
                        accentColor = AccentOrange
                    )

                    OrgField(
                        value = email,
                        onValueChange = { email = it; emailError = "" },
                        label = "Official Email Address",
                        placeholder = "contact@organization.org",
                        icon = Icons.Default.Email,
                        error = emailError,
                        keyboardType = KeyboardType.Email,
                        accentColor = AccentOrange
                    )

                    OrgField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = "" },
                        label = "Contact Phone Number",
                        placeholder = "e.g. 0712 345 678",
                        icon = Icons.Default.Phone,
                        error = phoneError,
                        keyboardType = KeyboardType.Phone,
                        accentColor = AccentOrange
                    )

                    OrgField(
                        value = location,
                        onValueChange = { location = it; locationError = "" },
                        label = "Physical Location",
                        placeholder = "e.g. Westlands, Nairobi",
                        icon = Icons.Default.LocationOn,
                        error = locationError,
                        accentColor = AccentOrange
                    )

                    // ── Section 2: About Your Organization ─────
                    SectionLabel(
                        title = "About Your Organization",
                        color = AccentOrange
                    )

                    // Multi-line description field
                    // This is different from the other fields —
                    // it allows multiple lines of text
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "What does your organization do?",
                            color = if (description.isNotEmpty()) AccentOrange else TextDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                descriptionError = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(
                                    1.5.dp,
                                    if (descriptionError.isNotEmpty()) ErrorRed
                                    else if (description.isNotEmpty()) AccentOrange
                                    else FieldBorder,
                                    RoundedCornerShape(12.dp)
                                ),
                            placeholder = {
                                Text(
                                    text = "Briefly describe your organization, " +
                                            "the community you serve and the " +
                                            "kind of volunteers you need...",
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
                                focusedContainerColor   = OrangeBg,
                                unfocusedContainerColor = Color(0xFFF9F9F9),
                                cursorColor             = AccentOrange,
                                focusedTextColor        = TextDark,
                                unfocusedTextColor      = TextDark
                            )
                        )

                        if (descriptionError.isNotEmpty()) {
                            Text(
                                text = "⚠ $descriptionError",
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // Character count hint
                        Text(
                            text = "${description.length} characters " +
                                    if (description.length < 20) "· min 20 required" else "✓",
                            color = if (description.length >= 20) AccentOrange else TextMuted,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // ── Section 3: Account Security ────────────
                    SectionLabel(
                        title = "Account Security",
                        color = AccentOrange
                    )

                    OrgField(
                        value = password,
                        onValueChange = { password = it; passwordError = "" },
                        label = "Password",
                        placeholder = "At least 8 characters",
                        icon = Icons.Default.Lock,
                        error = passwordError,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        accentColor = AccentOrange
                    )

                    if (password.isNotEmpty()) {
                        OrgPasswordStrength(password = password)
                    }

                    OrgField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                        label = "Confirm Password",
                        placeholder = "Re-enter your password",
                        icon = Icons.Default.Lock,
                        error = confirmPasswordError,
                        isPassword = true,
                        passwordVisible = confirmPassVisible,
                        onTogglePassword = { confirmPassVisible = !confirmPassVisible },
                        accentColor = AccentOrange
                    )

                    // ── Verification Notice Box ─────────────────
                    // This tells the org what happens after sign up
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangeBg)
                            .border(1.dp, AccentOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Verification Required",
                                    color = AccentOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "After submitting your registration your " +
                                        "account will be reviewed by our admin team. " +
                                        "You will not be able to post opportunities " +
                                        "until your organization is verified. " +
                                        "This usually takes 1–2 business days.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            // What they need to have ready
                            Text(
                                text = "✦  What we verify:\n" +
                                        "• Organization name & contact details\n" +
                                        "• Physical location\n" +
                                        "• Description of activities",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // ── Firebase Error Display ──────────────────
                    // Shows errors that come back from Firebase
                    // e.g. email already in use, no internet etc.
                    if (firebaseError.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.08f))
                                .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
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

                    // ── Terms ───────────────────────────────────
                    Text(
                        text = "By registering your organization you agree to our\nTerms of Service and Privacy Policy.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Submit Button ───────────────────────────
                    Button(
                        onClick = {
                            // First validate all fields locally,
                            // then call Firebase only if valid
                            if (validate()) {
                                registerOrganization()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
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
                            Text(
                                text = "Submit for Verification",
                                color = WhiteBg,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    // ── Login redirect ──────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sign In",
                            color = AccentOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigate(ROUT_LOGIN)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ── Section Label with colored left border ────────────────────
// Reusable section header used across the form
@Composable
fun SectionLabel(title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Organization Text Field ───────────────────────────────────
// Same as VolunteerField but uses AccentOrange as the active color
@Composable
fun OrgField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    accentColor: Color = AccentOrange
) {
    val hasError = error.isNotEmpty()
    val isFilled = value.isNotEmpty()
    val borderColor = when {
        hasError -> ErrorRed
        isFilled -> accentColor
        else     -> FieldBorder
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            color = if (isFilled) accentColor else TextDark,
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
                Text(text = placeholder, color = TextMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isFilled) accentColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onTogglePassword?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (isFilled) accentColor else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = OrangeBg,
                unfocusedContainerColor = Color(0xFFF9F9F9),
                cursorColor             = accentColor,
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

// ── Password Strength for Org ─────────────────────────────────
@Composable
fun OrgPasswordStrength(password: String) {
    val strength = when {
        password.length >= 12 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 3
        password.length >= 8 &&
                (password.any { it.isUpperCase() } || password.any { it.isDigit() }) -> 2
        else -> 1
    }

    val (label, strengthColor) = when (strength) {
        3    -> "Strong 💪" to Color(0xFF388E3C)
        2    -> "Medium"    to AccentOrange
        else -> "Weak"      to ErrorRed
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { strength / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = strengthColor,
            trackColor = FieldBorder
        )
        Text(
            text = "Password Strength: $label",
            color = strengthColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

    // ── Preview ──────────────────────────────────────────────────
    @Preview(showBackground = true)
    @Composable
    fun OrgRegisterPreview() {
        OrgRegisterScreen(rememberNavController())
    }