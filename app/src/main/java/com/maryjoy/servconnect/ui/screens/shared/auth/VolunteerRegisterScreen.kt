package com.maryjoy.servconnect.ui.screens.auth

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_HOME
import com.maryjoy.servconnect.ui.screens.navigation.ROUT_LOGIN
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

@Composable
fun VolunteerRegisterScreen(navController: NavController) {

    // ── Form State ────────────────────────────────────────────
    // These hold whatever the user types into each field
    var fullName             by remember { mutableStateOf("") }
    var email                by remember { mutableStateOf("") }
    var password             by remember { mutableStateOf("") }
    var confirmPassword      by remember { mutableStateOf("") }
    var passwordVisible      by remember { mutableStateOf(false) }
    var confirmPassVisible   by remember { mutableStateOf(false) }

    // ── Error State ───────────────────────────────────────────
    // Each field has its own error shown below it when invalid
    var fullNameError        by remember { mutableStateOf("") }
    var emailError           by remember { mutableStateOf("") }
    var passwordError        by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var firebaseError        by remember { mutableStateOf("") }
    var isLoading            by remember { mutableStateOf(false) }

    // ── Entrance Animations ───────────────────────────────────
    // Fade-in and slide-up effect when the screen first opens
    val headerAlpha = remember { Animatable(0f) }
    val formAlpha   = remember { Animatable(0f) }
    val formSlide   = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { formAlpha.animateTo(1f, tween(700, delayMillis = 300)) }
        launch { formSlide.animateTo(0f, tween(700, delayMillis = 300, easing = FastOutSlowInEasing)) }
    }

    // ── Validation ────────────────────────────────────────────
    // Runs before Firebase is called to catch empty or
    // incorrectly filled fields early without wasting an API call
    fun validate(): Boolean {
        var valid = true

        fullNameError = when {
            fullName.isBlank()         -> { valid = false; "Full name is required" }
            fullName.trim().length < 3 -> { valid = false; "Name must be at least 3 characters" }
            else -> ""
        }

        emailError = when {
            email.isBlank() -> { valid = false; "Email address is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            { valid = false; "Enter a valid email address" }
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
    // This function is only called after validate() returns true
    fun registerVolunteer() {
        isLoading = true
        firebaseError = ""

        // Step 1: Create the auth account with email and password.
        // Firebase Auth stores the login credentials securely.
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->

                // Step 2: Get the unique ID Firebase assigned
                // to this new account. Every user has one uid.
                val uid = authResult.user?.uid ?: run {
                    isLoading = false
                    firebaseError = "Something went wrong. Please try again."
                    return@addOnSuccessListener
                }

                // Step 3: Build the volunteer profile map.
                // This is the data that will be saved in Firestore
                // and used across the app (name on certificates,
                // portfolio, bookings etc.)
                val volunteerProfile = hashMapOf(
                    "uid"         to uid,
                    "fullName"    to fullName.trim(),
                    "email"       to email.trim(),
                    "role"        to "volunteer",

                    // These fields start empty and get filled
                    // as the volunteer uses the app
                    "interests"   to emptyList<String>(),
                    "skills"      to emptyList<String>(),
                    "location"    to "",
                    "totalHours"  to 0,
                    "createdAt"   to System.currentTimeMillis()
                )

                // Step 4: Save the profile to the "users" collection
                // in Firestore. The document ID is the uid so we
                // can always find this volunteer by their auth uid.
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(volunteerProfile)
                    .addOnSuccessListener {

                        // Step 5: Everything worked.
                        // Navigate to the home screen and clear
                        // the back stack so the user cannot press
                        // back and return to the registration form.
                        isLoading = false
                        navController.navigate(ROUT_HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Firestore save failed even though the
                        // auth account was created. Show the error.
                        isLoading = false
                        firebaseError = "Failed to save your profile. ${e.message}"
                    }
            }
            .addOnFailureListener { e ->
                // Firebase Auth account creation failed.
                // Map Firebase error messages to friendly ones.
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

        // Olive gradient header band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkOlive, PrimaryOlive)
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
            // Takes the user back to the role picker screen
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
                        imageVector = Icons.Default.ArrowBack,
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
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Volunteer Registration",
                    color = SecondaryGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Green badge — distinguishes from org flow
                // which uses an orange badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SecondaryGold.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🤝  Joining as a Volunteer",
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
                        ambientColor = PrimaryOlive.copy(alpha = 0.12f),
                        spotColor = PrimaryOlive.copy(alpha = 0.12f)
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

                    // ── Section 1: Personal Information ────────
                    SectionLabel(
                        title = "Personal Information",
                        color = PrimaryOlive
                    )

                    // Full Name field
                    // Collected here then saved to Firestore as
                    // "fullName" — used on certificates & portfolio
                    VolunteerField(
                        value = fullName,
                        onValueChange = { fullName = it; fullNameError = "" },
                        label = "Full Name",
                        placeholder = "e.g. Jane Wanjiku",
                        icon = Icons.Default.Person,
                        error = fullNameError
                    )

                    // Email field
                    // Used as the Firebase Auth login credential
                    VolunteerField(
                        value = email,
                        onValueChange = { email = it; emailError = "" },
                        label = "Email Address",
                        placeholder = "you@example.com",
                        icon = Icons.Default.Email,
                        error = emailError,
                        keyboardType = KeyboardType.Email
                    )

                    // ── Section 2: Account Security ────────────
                    SectionLabel(
                        title = "Secure Your Account",
                        color = PrimaryOlive
                    )

                    // Password field
                    // The eye icon toggles between hidden and visible
                    VolunteerField(
                        value = password,
                        onValueChange = { password = it; passwordError = "" },
                        label = "Password",
                        placeholder = "At least 8 characters",
                        icon = Icons.Default.Lock,
                        error = passwordError,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    // Strength bar appears as soon as user starts typing
                    // Weak = red, Medium = orange, Strong = green
                    if (password.isNotEmpty()) {
                        VolunteerPasswordStrength(password = password)
                    }

                    // Confirm password — must match the field above
                    VolunteerField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                        label = "Confirm Password",
                        placeholder = "Re-enter your password",
                        icon = Icons.Default.Lock,
                        error = confirmPasswordError,
                        isPassword = true,
                        passwordVisible = confirmPassVisible,
                        onTogglePassword = { confirmPassVisible = !confirmPassVisible }
                    )

                    // ── What you get info box ───────────────────
                    // Motivates the user to complete sign up by
                    // showing them the benefits of joining
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightGold)
                            .border(
                                1.dp,
                                PrimaryOlive.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "✦  What you get as a volunteer",
                                color = PrimaryOlive,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Browse & book volunteer opportunities\n" +
                                        "• Earn verified certificates of service\n" +
                                        "• Build your personal service portfolio\n" +
                                        "• Get matched by location & interest",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // ── Firebase Error Display ──────────────────
                    // Only visible when Firebase returns an error
                    // e.g. email already in use, no internet etc.
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

                    // ── Terms ───────────────────────────────────
                    Text(
                        text = "By creating an account you agree to our\nTerms of Service and Privacy Policy.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Register Button ─────────────────────────
                    // First validates locally then calls Firebase
                    Button(
                        onClick = {
                            if (validate()) {
                                registerVolunteer()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryOlive,
                            disabledContainerColor = PrimaryOlive.copy(alpha = 0.5f)
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            // Spinner shows while Firebase is working
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = SecondaryGold,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Volunteer Account",
                                color = SecondaryGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    // ── Divider ────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
                        Text(text = "  or  ", color = TextMuted, fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
                    }

                    // ── Sign in redirect ───────────────────────
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

// ── Volunteer Text Field ──────────────────────────────────────
// Reusable branded field — olive green accent color
// Border turns olive when filled, red when there's an error
@Composable
fun VolunteerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    error: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val hasError = error.isNotEmpty()
    val isFilled = value.isNotEmpty()
    val borderColor = when {
        hasError -> ErrorRed
        isFilled -> PrimaryOlive
        else     -> FieldBorder
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            // Label turns olive when the field has content
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
                Text(text = placeholder, color = TextMuted, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    // Icon also turns olive when field is filled
                    tint = if (isFilled) PrimaryOlive else TextMuted,
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
                            tint = if (isFilled) PrimaryOlive else TextMuted,
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
                // Light gold tint when focused — matches brand
                focusedContainerColor   = LightGold.copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFFF9F9F9),
                cursorColor             = PrimaryOlive,
                focusedTextColor        = TextDark,
                unfocusedTextColor      = TextDark
            )
        )

        // Error message shown below the field
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

// ── Password Strength Indicator ───────────────────────────────
// 3-segment bar that fills based on password complexity:
// 1 bar = Weak (red), 2 bars = Medium (orange), 3 bars = Strong (green)
@Composable
fun VolunteerPasswordStrength(password: String) {
    val strength = when {
        password.length >= 12 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 3

        password.length >= 8 &&
                (password.any { it.isUpperCase() } || password.any { it.isDigit() }) -> 2

        else -> 1
    }

    val (label, color) = when (strength) {
        3    -> "Strong 💪" to Color(0xFF388E3C)
        2    -> "Medium"    to AccentOrange
        else -> "Weak"      to ErrorRed
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (index < strength) color else FieldBorder)
                )
            }
        }
        Text(
            text = "Password strength: $label",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VolunteerRegisterScreenPreview() {
    VolunteerRegisterScreen(rememberNavController())
}