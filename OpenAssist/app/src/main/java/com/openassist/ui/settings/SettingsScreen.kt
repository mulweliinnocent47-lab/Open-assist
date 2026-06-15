package com.openassist.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.openassist.ui.navigation.OpenAssistDestination
import com.openassist.ui.navigation.PremiumCard
import com.openassist.ui.navigation.PremiumPage
import com.openassist.ui.navigation.PremiumPill
import com.openassist.ui.navigation.PremiumColors
import com.openassist.ui.navigation.premiumMutedTextColor
import com.openassist.ui.navigation.premiumTextColor
import com.openassist.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit, onModelSelection: () -> Unit = {}, onAiMode: () -> Unit = {}) {
    val savedModel by viewModel.model.collectAsState()
    val navigate: (OpenAssistDestination) -> Unit = {
        when (it) {
            OpenAssistDestination.Chat -> onBack()
            OpenAssistDestination.ConversationHistory -> onBack()
            OpenAssistDestination.MCPServers -> onBack()
            OpenAssistDestination.Settings -> Unit
            else -> Unit
        }
    }
    PremiumPage("Settings", "Manage your app, models, privacy, and security.", OpenAssistDestination.Settings, navigate) {
        val rows = listOf(
            Triple("API key", "Securely stored and encrypted.", "Change"),
            Triple("AI mode", "Cloud, local offline, or hybrid routing.", "Choose"),
            Triple("OpenRouter model", savedModel, "Choose"),
            Triple("Local models", "Downloaded Hugging Face GGUF models.", "Manage"),
            Triple("Theme", "Premium white Android 16 style.", "Auto"),
            Triple("History", "Saved on device", "Open"),
            Triple("Safety prompts", "Control how the assistant behaves.", "Edit"),
            Triple("Privacy", "Your data stays under your control.", "Review"),
        )
        rows.forEach { (title, detail, action) ->
            PremiumCard {
                Text(title, color = premiumTextColor(), fontWeight = FontWeight.ExtraBold)
                Text(detail, color = premiumMutedTextColor())
                Spacer(Modifier.height(8.dp))
                PremiumPill(action, onClick = if (title == "OpenRouter model") onModelSelection else if (title == "AI mode" || title == "Local models") onAiMode else null)
            }
            Spacer(Modifier.height(12.dp))
        }
        PremiumCard {
            Text("Security status", color = premiumTextColor(), fontWeight = FontWeight.ExtraBold)
            Text("Permissions and keys are protected with local encryption.", color = premiumMutedTextColor())
            Spacer(Modifier.height(12.dp))
            PremiumPill("Protected", color = PremiumColors.Success)
        }
    }
}
