package com.openassist.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openassist.ui.navigation.OpenAssistDestination
import com.openassist.ui.navigation.PremiumButton
import com.openassist.ui.navigation.PremiumCard
import com.openassist.ui.navigation.PremiumPage
import com.openassist.ui.navigation.PremiumPill
import com.openassist.ui.navigation.premiumMutedTextColor
import com.openassist.ui.navigation.premiumTextColor
import com.openassist.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    onSettings: () -> Unit,
    onModels: () -> Unit,
    onPermissions: () -> Unit,
    onToolApproval: () -> Unit,
    onMcpServers: () -> Unit,
    onHistory: () -> Unit,
    onAbout: () -> Unit,
) {
    val state by chatViewModel.state.collectAsState()
    var input by remember { mutableStateOf("") }
    val navigate: (OpenAssistDestination) -> Unit = {
        when (it) {
            OpenAssistDestination.Chat -> Unit
            OpenAssistDestination.ConversationHistory -> onHistory()
            OpenAssistDestination.MCPServers -> onMcpServers()
            OpenAssistDestination.Settings -> onSettings()
            else -> Unit
        }
    }

    PremiumPage("OpenAssist\nChat", "A premium assistant built for chat, code, and tool use.", OpenAssistDestination.Chat, navigate, action = { PremiumPill("GPT-5.4 Thinking", onClick = onModels) }) {
        LazyColumn(Modifier.weight(1f)) {
            item {
                PremiumCard(Modifier.fillMaxWidth(0.58f)) {
                    Text("Assistant", color = premiumMutedTextColor(), fontWeight = FontWeight.Bold)
                    Text("I can help with UI, code, app structure, and MCP setup.", color = premiumTextColor())
                }
                Spacer(Modifier.height(12.dp))
                PremiumCard(Modifier.fillMaxWidth(0.72f).padding(start = 90.dp), selected = true) {
                    Text("You", color = premiumMutedTextColor(), fontWeight = FontWeight.Bold)
                    Text("Make this app feel premium and ready for production.", color = premiumTextColor())
                }
                Spacer(Modifier.height(12.dp))
                PremiumCard(Modifier.fillMaxWidth(0.72f).padding(start = 150.dp)) {
                    Text("Tool result", color = premiumMutedTextColor(), fontWeight = FontWeight.Bold)
                    Text("OpenAssist mockups updated successfully.", color = premiumTextColor())
                }
                Spacer(Modifier.height(12.dp))
            }
            items(state.messages) { message ->
                PremiumCard(Modifier.padding(vertical = 4.dp)) {
                    Text(message.role, color = premiumMutedTextColor(), fontWeight = FontWeight.Bold)
                    Text(message.content, color = premiumTextColor())
                }
            }
        }
        PremiumCard {
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Type a message...") })
                Spacer(Modifier.width(12.dp))
                PremiumButton("Send") { chatViewModel.send(input); input = "" }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            PremiumPill("Permissions", onClick = onPermissions)
            Spacer(Modifier.width(8.dp))
            PremiumPill("Tool Approval", onClick = onToolApproval)
            Spacer(Modifier.width(8.dp))
            PremiumPill("About", onClick = onAbout)
        }
    }
}
