package com.openassist.ui.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.openassist.viewmodel.SettingsViewModel

@Composable
fun ModelSelectionScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val savedModel by viewModel.model.collectAsState()
    var selectedModel by remember(savedModel) { mutableStateOf(savedModel.ifBlank { "openai/gpt-5.4-thinking" }) }
    val models = listOf(
        "openai/gpt-5.4-thinking" to "Best for complex reasoning",
        "anthropic/claude-4.1" to "Great writing and analysis",
        "google/gemini-2.5-pro" to "Fast multimodal responses",
        "openrouter/deepseek-r1" to "Budget reasoning model",
        "xai/grok-4" to "General purpose assistant",
    )
    PremiumPage("Model Selection", "Pick a model optimized for your workflow.", OpenAssistDestination.Chat, { if (it != OpenAssistDestination.Chat) onBack() }) {
        models.forEach { (model, detail) ->
            PremiumCard(Modifier.clickable { selectedModel = model }, selected = model == selectedModel) {
                Text(model, color = premiumTextColor(), fontWeight = FontWeight.ExtraBold)
                Text(detail, color = premiumMutedTextColor())
                if (model == selectedModel) PremiumPill("Selected")
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.weight(1f))
        PremiumButton("Save Model", Modifier.fillMaxWidth()) { viewModel.saveModel(selectedModel); onBack() }
    }
}
