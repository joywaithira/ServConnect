package com.maryjoy.servconnect.ui.screens.organization

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
fun QRCodeScreen(
    navController: NavController,
    opportunityId: String
) {
    val scope = rememberCoroutineScope()

    // ── State ─────────────────────────────────────────────────
    var opportunityTitle by remember { mutableStateOf("") }
    var opportunityDate  by remember { mutableStateOf("") }
    var orgName          by remember { mutableStateOf("") }
    var slotsBooked      by remember { mutableIntStateOf(0) }
    var slotsTotal       by remember { mutableIntStateOf(0) }
    var qrBitmap         by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading        by remember { mutableStateOf(true) }
    var isGenerating     by remember { mutableStateOf(false) }
    var firebaseError    by remember { mutableStateOf("") }
    var showCopiedMsg    by remember { mutableStateOf(false) }

    // ── Entrance animations ───────────────────────────────────
    val headerAlpha  = remember { Animatable(0f) }
    val cardScale    = remember { Animatable(0.85f) }
    val cardAlpha    = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            headerAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            delay(300)
            cardAlpha.animateTo(1f, tween(500))
            cardScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            )
        }
    }

    // ── Decorative rotation animation ─────────────────────────
    // The decorative ring around the QR code rotates slowly
    val ringRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        ringRotation.animateTo(
            targetValue    = 360f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // ── Scan pulse animation ──────────────────────────────────
    // A pulsing ring around the QR card to draw attention
    val pulseAlpha = remember { Animatable(0.6f) }
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            launch { pulseAlpha.animateTo(0f, tween(1200)) }
            pulseScale.animateTo(1.3f, tween(1200))
            pulseAlpha.snapTo(0.6f)
            pulseScale.snapTo(1f)
        }
    }

    // ── Fetch opportunity details ─────────────────────────────
    // We pull the opportunity's title, date, org name and
    // slot counts to display on the QR code screen alongside
    // the code itself. This context helps staff at the venue
    // know which event the QR code belongs to.
    LaunchedEffect(opportunityId) {
        FirebaseFirestore.getInstance()
            .collection("opportunities")
            .document(opportunityId)
            .get()
            .addOnSuccessListener { doc ->
                opportunityTitle = doc.getString("title")   ?: "Opportunity"
                opportunityDate  = doc.getString("date")    ?: ""
                orgName          = doc.getString("orgName") ?: ""
                slotsBooked      = (doc.getLong("slotsBooked") ?: 0).toInt()
                slotsTotal       = (doc.getLong("slotsTotal")  ?: 0).toInt()

                // Generate the QR code as soon as we have the data
                // The QR code encodes a JSON-like string with the
                // opportunityId so the volunteer app knows exactly
                // which event to check them into when they scan.
                scope.launch {
                    isGenerating = true
                    qrBitmap = generateQRCode(
                        content = buildQRContent(opportunityId, opportunityTitle)
                    )
                    isGenerating = false
                    isLoading    = false
                }
            }
            .addOnFailureListener { e ->
                firebaseError = "Failed to load: ${e.message}"
                isLoading     = false
            }
    }

    // ── UI ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkOlive,
                        PrimaryOlive,
                        Color(0xFF8B8F3D),
                        Color(0xFFF2F4EE)
                    ),
                    startY = 0f,
                    endY   = 1800f
                )
            )
            // Decorative dot pattern on background
            .drawBehind {
                val dotColor = Color.White.copy(alpha = 0.03f)
                listOf(
                    Offset(size.width * 0.85f, size.height * 0.08f) to 90f,
                    Offset(size.width * 0.1f,  size.height * 0.15f) to 50f,
                    Offset(size.width * 0.9f,  size.height * 0.35f) to 40f,
                    Offset(size.width * 0.05f, size.height * 0.45f) to 70f,
                    Offset(size.width * 0.7f,  size.height * 0.55f) to 30f,
                ).forEach { (pos, r) ->
                    drawCircle(color = dotColor, radius = r, center = pos)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp, end = 20.dp,
                        top = 48.dp, bottom = 0.dp
                    )
                    .alpha(headerAlpha.value)
            ) {
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
                            text = "QR Check-In Code",
                            color = SecondaryGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Display this at your venue entrance",
                            color = SecondaryGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    // QR icon badge in header
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(WhiteBg.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = SecondaryGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Opportunity info strip ─────────────────────────
            // Shows event context above the QR code so staff
            // know which event the code belongs to
            if (!isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .alpha(headerAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = opportunityTitle,
                        color = SecondaryGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = orgName,
                        color = SecondaryGold.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = SecondaryGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = opportunityDate,
                            color = SecondaryGold.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(SecondaryGold.copy(alpha = 0.4f))
                        )
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = SecondaryGold.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "$slotsBooked/$slotsTotal volunteers",
                            color = SecondaryGold.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── QR Code Card ───────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(cardScale.value)
                    .alpha(cardAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .scale(pulseScale.value)
                        .alpha(pulseAlpha.value)
                        .clip(CircleShape)
                        .background(SecondaryGold.copy(alpha = 0.15f))
                )

                // Slowly rotating dashed decorative ring
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .rotate(ringRotation.value)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    SecondaryGold.copy(alpha = 0.6f),
                                    SecondaryGold.copy(alpha = 0.1f),
                                    SecondaryGold.copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // QR card itself
                Card(
                    modifier = Modifier.size(240.dp),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(containerColor = WhiteBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoading || isGenerating -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = PrimaryOlive,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Generating QR...",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            qrBitmap != null -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // The actual QR code image
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier
                                            .size(170.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )

                                    // ServConnect branding below QR
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryOlive),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "S",
                                                color = SecondaryGold,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Text(
                                            text = "ServConnect",
                                            color = PrimaryOlive,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            else -> {
                                // Error state inside QR card
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = AccentOrange,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Failed to generate",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Scan instruction label ─────────────────────────
            if (qrBitmap != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(WhiteBg.copy(alpha = 0.15f))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = SecondaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Volunteers scan this on arrival",
                        color = SecondaryGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // ── Info cards ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // How it works card
                QRInfoCard(
                    icon       = Icons.Default.Info,
                    iconColor  = PrimaryOlive,
                    bgColor    = LightGold,
                    title      = "How check-in works",
                    content    = {
                        QRStepRow(
                            number = "1",
                            text   = "Volunteer opens the ServConnect app"
                        )
                        QRStepRow(
                            number = "2",
                            text   = "Taps \"Scan QR\" on their booked activity"
                        )
                        QRStepRow(
                            number = "3",
                            text   = "Points camera at this code — check-in is instant"
                        )
                        QRStepRow(
                            number = "4",
                            text   = "Their session timer starts automatically"
                        )
                        QRStepRow(
                            number = "5",
                            text   = "Hours are logged when they scan out"
                        )
                    }
                )

                // Tips card
                QRInfoCard(
                    icon      = Icons.Default.Lightbulb,
                    iconColor = AccentOrange,
                    bgColor   = Color(0xFFFFF3EE),
                    title     = "Tips for smooth check-in",
                    content   = {
                        QRBulletRow(
                            icon  = Icons.Default.Brightness5,
                            text  = "Keep screen brightness high for easy scanning",
                            color = AccentOrange
                        )
                        QRBulletRow(
                            icon  = Icons.Default.Wifi,
                            text  = "Volunteers need internet to complete check-in",
                            color = AccentOrange
                        )
                        QRBulletRow(
                            icon  = Icons.Default.PrintDisabled,
                            text  = "Print this code or display it at the entrance",
                            color = AccentOrange
                        )
                        QRBulletRow(
                            icon  = Icons.Default.Refresh,
                            text  = "Each opportunity has its own unique QR code",
                            color = AccentOrange
                        )
                    }
                )

                // What gets recorded card
                QRInfoCard(
                    icon      = Icons.Default.Assignment,
                    iconColor = SuccessGreen,
                    bgColor   = Color(0xFFE8F5E9),
                    title     = "What gets recorded automatically",
                    content   = {
                        QRBulletRow(
                            icon  = Icons.Default.CheckCircle,
                            text  = "Check-in time and date",
                            color = SuccessGreen
                        )
                        QRBulletRow(
                            icon  = Icons.Default.CheckCircle,
                            text  = "Hours volunteered",
                            color = SuccessGreen
                        )
                        QRBulletRow(
                            icon  = Icons.Default.CheckCircle,
                            text  = "Proof of attendance for certificate",
                            color = SuccessGreen
                        )
                        QRBulletRow(
                            icon  = Icons.Default.CheckCircle,
                            text  = "Activity added to volunteer's portfolio",
                            color = SuccessGreen
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── QR Code Generator ─────────────────────────────────────────
// Uses the ZXing library to generate a QR bitmap from a string.
// Runs on the IO dispatcher so it doesn't block the UI thread.
// The QR code content is a structured string that the volunteer
// app parses to know which opportunity to check into.
suspend fun generateQRCode(content: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val hints = mapOf(
                EncodeHintType.MARGIN         to 1,
                EncodeHintType.CHARACTER_SET  to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to
                        com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H
            )
            val writer  = QRCodeWriter()
            val bitMatrix = writer.encode(
                content, BarcodeFormat.QR_CODE, 512, 512, hints
            )
            val width  = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Dark pixels → olive green (brand color)
                    // Light pixels → white background
                    bitmap.setPixel(
                        x, y,
                        if (bitMatrix[x, y])
                            PrimaryOlive.toArgb()
                        else
                            AndroidColor.WHITE
                    )
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}

// ── QR Content Builder ────────────────────────────────────────
// Builds the string encoded into the QR code.
// The volunteer app reads this string when they scan and
// uses the opportunityId to record their check-in in Firestore.
fun buildQRContent(opportunityId: String, title: String): String {
    return "SERVCONNECT_CHECKIN|opportunityId=$opportunityId|title=$title|" +
            "timestamp=${System.currentTimeMillis()}"
}

// ── QR Info Card ──────────────────────────────────────────────
// Reusable colored card used for the how-it-works
// tips and what-gets-recorded sections below the QR code
@Composable
fun QRInfoCard(
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = WhiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
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

// ── QR Step Row ───────────────────────────────────────────────
// Numbered step used in the "How it works" card
@Composable
fun QRStepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Numbered circle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PrimaryOlive),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = SecondaryGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = text,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── QR Bullet Row ─────────────────────────────────────────────
// Icon + text bullet used in tips and what-gets-recorded cards
@Composable
fun QRBulletRow(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QRCodeScreenPreview() {
    QRCodeScreen(
        navController  = rememberNavController(),
        opportunityId  = "preview_id"
    )
}