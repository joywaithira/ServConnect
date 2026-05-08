package com.maryjoy.servconnect.ui.screens.users

import com.maryjoy.servconnect.ui.theme.PrimaryColor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { SectionHeader("Account") }
            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { /* TODO */ }
                )
            }

            item { SectionHeader("Preferences") }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.MyLocation,
                    title = "Location Services",
                    checked = locationEnabled,
                    onCheckedChange = { locationEnabled = it }
                )
            }

            item { SectionHeader("Support") }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "Help Center",
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About ServConnect",
                    onClick = { /* TODO */ }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    elevation = null
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryColor
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.Gray) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryColor)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onNavigateBack = {},
        onLogout = {}
    )
}


