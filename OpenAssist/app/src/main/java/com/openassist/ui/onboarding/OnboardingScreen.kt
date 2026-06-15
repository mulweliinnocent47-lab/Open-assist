package com.openassist.ui.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openassist.ui.navigation.PremiumButton
import com.openassist.ui.navigation.PremiumCard
import com.openassist.ui.navigation.PremiumPage
import com.openassist.ui.navigation.PremiumPill
import com.openassist.ui.navigation.premiumMutedTextColor
import com.openassist.ui.navigation.premiumTextColor

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    PremiumPage("Onboarding", "Set up your OpenAssist in under two minutes.") {
        PremiumCard {
            PremiumCard { Text("Choose your model", color = premiumTextColor(), fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(12.dp))
            Text("OpenAssist Pro setup", color = premiumTextColor(), fontWeight = FontWeight.ExtraBold)
            Text("Fast, secure, and designed for advanced workflows.", color = premiumMutedTextColor())
            listOf("Multiple models", "MCP servers", "Tool approval", "Privacy controls").forEach {
                Spacer(Modifier.height(8.dp))
                PremiumPill(it)
            }
        }
        Spacer(Modifier.weight(1f))
        PremiumButton("Continue", Modifier.fillMaxWidth(), onContinue)
        Text("By continuing, you agree to the premium experience.", color = premiumMutedTextColor())
    }
}
