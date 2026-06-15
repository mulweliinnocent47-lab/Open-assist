package com.openassist.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openassist.ui.navigation.LogoMark
import com.openassist.ui.navigation.PremiumButton
import com.openassist.ui.navigation.premiumBackgroundBrush
import com.openassist.ui.navigation.premiumMutedTextColor
import com.openassist.ui.navigation.premiumSurfaceColor
import com.openassist.ui.navigation.premiumTextColor

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Box(Modifier.fillMaxSize().background(premiumBackgroundBrush()).padding(24.dp)) {
        Text("9:41", color = premiumTextColor(), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(top = 20.dp))
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.size(width = 280.dp, height = 190.dp).clip(RoundedCornerShape(28.dp)).background(premiumSurfaceColor()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LogoMark()
                Spacer(Modifier.height(18.dp))
                Text("OpenAssist", color = premiumTextColor(), fontWeight = FontWeight.ExtraBold)
                Text("Your AI. Your Models.", color = premiumMutedTextColor())
                Text("Premium multi-model assistant", color = premiumMutedTextColor())
            }
        }
        Text("Version 1.0", color = premiumMutedTextColor(), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp))
        PremiumButton("Continue", Modifier.align(Alignment.BottomCenter), onContinue)
    }
}
