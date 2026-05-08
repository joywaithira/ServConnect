package com.maryjoy.servconnect.ui.screens.organization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.maryjoy.servconnect.ui.screens.users.MessagesScreen
import com.maryjoy.servconnect.ui.theme.AccentColor
import com.maryjoy.servconnect.ui.theme.PrimaryColor

// --- DATA MODELS ---
data class VolunteerConversation(
    val id: String,
    val volunteerName: String,
    val volunteerImageUrl: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val time: String
)

// --- MAIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgMessagesScreen(
    onNavigateBack: () -> Unit,
    onConversationClick: (String) -> Unit
) {
    val conversations = listOf(
        VolunteerConversation("1", "Jane Doe", "https://i.pravatar.cc/150?img=32", "I'll be there by 9 AM tomorrow.", "10:30 AM", 2),
        VolunteerConversation("2", "John Smith", "https://i.pravatar.cc/150?img=12", "Thank you for the opportunity!", "Yesterday", 0),
        VolunteerConversation("3", "Alice W.", "https://i.pravatar.cc/150?img=45", "Can I bring a friend along?", "Monday", 0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Volunteer Messages", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search volunteers...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations) { conversation ->
                    VolunteerConversationItem(conversation, onClick = { onConversationClick(conversation.id) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun VolunteerConversationItem(conversation: VolunteerConversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = conversation.volunteerImageUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(conversation.volunteerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(conversation.time, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = if (conversation.unreadCount > 0) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )

                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier.size(20.dp).background(AccentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- CHAT DETAIL SCREEN (Also in the same file) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgChatDetailScreen(
    volunteerName: String,
    onNavigateBack: () -> Unit
) {
    val messages = listOf(
        ChatMessage("1", "Hello! I'm interested in the tutoring session.", false, "09:00 AM"),
        ChatMessage("2", "Hi Jane! We'd love to have you. Have you tutored before?", true, "09:05 AM"),
        ChatMessage("3", "Yes, I have experience with primary school kids.", false, "09:10 AM"),
        ChatMessage("4", "That's perfect. I'll be there by 9 AM tomorrow.", false, "10:30 AM")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://i.pravatar.cc/150?img=32",
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(volunteerName, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryColor)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    OrgChatBubble(message)
                }
            }

            // Message Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Attach */ }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryColor)
                    }

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { /* Send */ },
                        modifier = Modifier.background(PrimaryColor, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun OrgChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromMe) PrimaryColor else Color.White
    val textColor = if (message.isFromMe) Color.White else Color.Black
    val shape = if (message.isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = textColor,
                fontSize = 14.sp
            )
        }
        Text(
            text = message.time,
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrgMessagesScreenPreview() {
    OrgMessagesScreen(onNavigateBack = {}, onConversationClick = {})
}