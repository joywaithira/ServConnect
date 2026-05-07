package com.maryjoy.servconnect.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maryjoy.servconnect.ui.components.*
import com.maryjoy.servconnect.ui.theme.*

// ── PickInterestsScreen ───────────────────────────────────────────────────────
// Placement: ui/screens/PickInterestsScreen.kt
// Shown once right after registration. The user picks their interests and
// relevant skills. These are saved to Firestore and used to power the
// recommendation engine. After saving, the user goes to the main home screen.

private val interestOptions = listOf(
    "Children",
    "Elderly",
    "Education",
    "Health",
    "Environment",
    "Animals",
    "Food & Hunger",
    "Disability",
    "Community Dev.",
    "Arts & Culture",
    "Sports",
    "Crisis Relief"
)

private val skillOptions = listOf(
    "Teaching",
    "Nursing / First Aid",
    "Cooking",
    "Driving",
    "Construction",
    "Counselling",
    "IT / Tech",
    "Music",
    "Sports Coaching",
    "Administration",
    "Photography",
    "Sign Language"
)

@Composable
fun PickInterestsScreen(navController: NavController) {

    val selectedInterests = remember { mutableStateListOf<String>() }
    val selectedSkills    = remember { mutableStateListOf<String>() }
    var isLoading         by remember { mutableStateOf(false) }

    val uid       = FirebaseAuth.getInstance().currentUser?.uid
    val firestore = FirebaseFirestore.getInstance()

    fun saveAndContinue() {
        if (uid == null) return
        isLoading = true
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "interests" to selectedInterests.toList(),
                    "skills"    to selectedSkills.toList()
                )
            )
            .addOnSuccessListener {
                isLoading = false
                navController.navigate("home") {
                    popUpTo("pick_interests") { inclusive = true }
                }
            }
            .addOnFailureListener {
                isLoading = false
                // Navigate anyway so user isn't stuck
                navController.navigate("home") {
                    popUpTo("pick_interests") { inclusive = true }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(56.dp))

        // Progress indicator
        LinearProgressIndicator(
            progress           = 1f,
            modifier           = Modifier.fillMaxWidth(),
            color              = Primary,
            trackColor         = Divider
        )

        Spacer(Modifier.height(4.dp))

        Text("Step 2 of 2", fontSize = 12.sp, color = TextSecondary)

        Spacer(Modifier.height(24.dp))

        Text("What are you\npassionate about?", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = OnSurface, lineHeight = 32.sp)
        Text("Pick your interests to get personalised opportunities", fontSize = 14.sp, color = TextSecondary)

        Spacer(Modifier.height(24.dp))

        // ── Interests ─────────────────────────────────────────────────────────
        SectionHeader("Interests")
        Spacer(Modifier.height(12.dp))

        // Wrap chips in rows of 3
        val interestRows = interestOptions.chunked(3)
        interestRows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { interest ->
                    InterestChip(
                        label    = interest,
                        selected = selectedInterests.contains(interest),
                        onClick  = {
                            if (selectedInterests.contains(interest))
                                selectedInterests.remove(interest)
                            else
                                selectedInterests.add(interest)
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(28.dp))

        // ── Skills ────────────────────────────────────────────────────────────
        SectionHeader("Your Skills")
        Text("We'll match opportunities where your skills are needed", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(12.dp))

        val skillRows = skillOptions.chunked(3)
        skillRows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { skill ->
                    InterestChip(
                        label    = skill,
                        selected = selectedSkills.contains(skill),
                        onClick  = {
                            if (selectedSkills.contains(skill))
                                selectedSkills.remove(skill)
                            else
                                selectedSkills.add(skill)
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(36.dp))

        ServButton(
            text      = if (selectedInterests.isEmpty()) "Skip for now" else "Continue",
            onClick   = { saveAndContinue() },
            isLoading = isLoading,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PickInterestsScreenPreview() {
    PickInterestsScreen(rememberNavController())
}