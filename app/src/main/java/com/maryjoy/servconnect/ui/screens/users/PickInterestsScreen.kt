package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.maryjoy.servconnect.ui.theme.*

// ─── Data model ────────────────────────────────────────────────────────────────

data class InterestCategory(
    val id: String,
    val label: String,
    val emoji: String,
    val gradient: List<Color>
)

// ─── Color palette (add to your Color.kt or keep here) ─────────────────────────

private val GreenPrimary   = PrimaryColor
private val GreenLight     = PrimaryColor.copy(alpha = 0.8f)
private val GreenAccent    = SecondaryColor
private val GoldAccent     = AccentColor
private val SurfaceWhite   = White
private val CardSurface    = White
private val TextPrimary    = DarkGray
private val TextSecondary  = Gray
private val UnselectedBg   = LightGray

// ─── Interest data ──────────────────────────────────────────────────────────────

private val allInterests = listOf(
    InterestCategory("children",  "Children",       "🧒", listOf(Color(0xFFFF8A65), Color(0xFFFFB74D))),
    InterestCategory("elderly",   "Elderly Care",   "👴", listOf(Color(0xFF7986CB), Color(0xFF9FA8DA))),
    InterestCategory("health",    "Health & Care",  "❤️", listOf(Color(0xFFEF5350), Color(0xFFEF9A9A))),
    InterestCategory("education", "Education",      "📚", listOf(Color(0xFF42A5F5), Color(0xFF90CAF9))),
    InterestCategory("animals",   "Animal Welfare", "🐾", listOf(Color(0xFF66BB6A), Color(0xFFA5D6A7))),
    InterestCategory("hunger",    "Food & Hunger",  "🍽️", listOf(Color(0xFFFFCA28), Color(0xFFFFE082))),
    InterestCategory("environ",   "Environment",    "🌿", listOf(Color(0xFF26A69A), Color(0xFF80CBC4))),
    InterestCategory("disabled",  "Special Needs",  "♿", listOf(Color(0xFFAB47BC), Color(0xFFCE93D8))),
    InterestCategory("homeless",  "Homelessness",   "🏠", listOf(Color(0xFF8D6E63), Color(0xFFBCAAA4))),
    InterestCategory("women",     "Women & Girls",  "👩", listOf(Color(0xFFEC407A), Color(0xFFF48FB1))),
    InterestCategory("youth",     "Youth Empow.",   "✊", listOf(Color(0xFF5C6BC0), Color(0xFF9FA8DA))),
    InterestCategory("arts",      "Arts & Culture", "🎨", listOf(Color(0xFFFF7043), Color(0xFFFFAB91)))
)

// ─── Main Screen ────────────────────────────────────────────────────────────────

@Composable
fun PickInterestScreen(onNavigateToHome: () -> Unit) {
    val selected = remember { mutableStateListOf<String>() }
    val scrollState = rememberScrollState()

    // Animate the CTA button appearing
    val buttonVisible by remember { derivedStateOf { selected.size >= 1 } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
    ) {
        // ── Background decorative blobs ──
        BackgroundBlobs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Header ──
            HeaderSection()

            Spacer(Modifier.height(32.dp))

            // ── Interest Grid ──
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 2000.dp)  // allow full expansion inside scroll
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                userScrollEnabled = false     // outer scroll handles it
            ) {
                itemsIndexed(allInterests) { index, interest ->
                    InterestCard(
                        interest = interest,
                        isSelected = selected.contains(interest.id),
                        animIndex = index,
                        onClick = {
                            if (selected.contains(interest.id)) {
                                selected.remove(interest.id)
                            } else {
                                selected.add(interest.id)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Selection count hint ──
            if (selected.size > 0) {
                SelectionBadge(count = selected.size)
            }
        }

        // ── Floating CTA Button ──
        AnimatedVisibility(
            visible = buttonVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ContinueButton(
                selectedCount = selected.size,
                onClick = onNavigateToHome
            )
        }

        // ── Skip button top right ──
        TextButton(
            onClick = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
        ) {
            Text(
                "Skip",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Background decorative blobs ────────────────────────────────────────────────

@Composable
private fun BackgroundBlobs() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GreenAccent.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.08f),
                radius = size.width * 0.45f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.08f),
            radius = size.width * 0.45f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(GoldAccent.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.88f),
                radius = size.width * 0.4f
            ),
            center = Offset(size.width * 0.1f, size.height * 0.88f),
            radius = size.width * 0.4f
        )
    }
}

// ─── Header ─────────────────────────────────────────────────────────────────────

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 28.dp)
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GreenPrimary, GreenAccent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🌍", fontSize = 34.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "What's your\ncalling?",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Select the causes you care about.\nWe'll find the right opportunities for you.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(Modifier.height(8.dp))

        // Decorative divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(180.dp)
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = GreenAccent.copy(alpha = 0.35f),
                thickness = 1.dp
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(6.dp)
                    .background(GreenAccent, CircleShape)
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = GreenAccent.copy(alpha = 0.35f),
                thickness = 1.dp
            )
        }
    }
}

// ─── Interest Card ───────────────────────────────────────────────────────────────

@Composable
private fun InterestCard(
    interest: InterestCategory,
    isSelected: Boolean,
    animIndex: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Scale animation on selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    // Entry animation stagger
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animIndex * 55L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = tween(300, easing = EaseOutBack)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .scale(scale)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isSelected)
                        Brush.linearGradient(
                            colors = interest.gradient,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    else
                        Brush.linearGradient(
                            colors = listOf(CardSurface, CardSurface)
                        )
                )
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFFDCEAE3),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .shadow(
                    elevation = if (isSelected) 8.dp else 2.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isSelected) interest.gradient.first().copy(alpha = 0.4f)
                    else Color.Black.copy(alpha = 0.06f)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = interest.emoji,
                        fontSize = 28.sp
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Text(
                    text = interest.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TextPrimary,
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ─── Selection Badge ─────────────────────────────────────────────────────────────

@Composable
private fun SelectionBadge(count: Int) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = GreenPrimary.copy(alpha = 0.10f),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(GreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (count == 1) "interest selected" else "interests selected",
                color = GreenPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Continue Button ─────────────────────────────────────────────────────────────

@Composable
private fun ContinueButton(selectedCount: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = GreenPrimary.copy(alpha = 0.35f),
                spotColor = GreenPrimary.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GreenPrimary, GreenLight)
                    ),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Explore Opportunities",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )
                Text("→", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PickInterestScreenPreview(){

    PickInterestScreen {}


}