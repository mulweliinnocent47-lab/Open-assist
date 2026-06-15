package com.openassist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openassist.data.local.SecureStorage
import com.openassist.ui.about.AboutScreen
import com.openassist.ui.aimode.AiModeSelectorScreen
import com.openassist.ui.chat.ChatScreen
import com.openassist.ui.downloads.DownloadManagerScreen
import com.openassist.ui.downloads.ModelDownloadScreen
import com.openassist.ui.history.ConversationHistoryScreen
import com.openassist.ui.localmodels.LocalModelsScreen
import com.openassist.ui.mcp.MCPServerScreen
import com.openassist.ui.model.ModelSelectionScreen
import com.openassist.ui.navigation.OpenAssistDestination
import com.openassist.ui.onboarding.OnboardingScreen
import com.openassist.ui.permissions.PermissionScreen
import com.openassist.ui.settings.SettingsScreen
import com.openassist.ui.splash.SplashScreen
import com.openassist.ui.storage.StorageManagerScreen
import com.openassist.ui.tools.ToolApprovalScreen
import com.openassist.viewmodel.ChatViewModel
import com.openassist.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as OpenAssistApp

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            app.permissionManager.onResult(results)
        }
        app.permissionManager.attachLauncher(permissionLauncher)

        val storage = SecureStorage(this)

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val settings: SettingsViewModel = viewModel(factory = settingsFactory(storage))
                val chat: ChatViewModel = viewModel(factory = chatFactory(storage, app))
                OpenAssistNavigation(settings = settings, chat = chat)
            }
        }
    }
}

@Composable
private fun OpenAssistNavigation(settings: SettingsViewModel, chat: ChatViewModel) {
    val apiKey by settings.apiKey.collectAsState()
    var destination by remember { mutableStateOf(OpenAssistDestination.Splash) }

    fun openChatOrOnboarding() {
        destination = if (apiKey.isBlank()) OpenAssistDestination.Onboarding else OpenAssistDestination.Chat
    }

    when (destination) {
        OpenAssistDestination.Splash -> SplashScreen(onContinue = ::openChatOrOnboarding)
        OpenAssistDestination.Onboarding -> OnboardingScreen(onContinue = { destination = OpenAssistDestination.AiModeSelector })
        OpenAssistDestination.Chat -> ChatScreen(
            chatViewModel = chat,
            onSettings = { destination = OpenAssistDestination.Settings },
            onModels = { destination = OpenAssistDestination.AiModeSelector },
            onPermissions = { destination = OpenAssistDestination.Permissions },
            onToolApproval = { destination = OpenAssistDestination.ToolApproval },
            onMcpServers = { destination = OpenAssistDestination.MCPServers },
            onHistory = { destination = OpenAssistDestination.ConversationHistory },
            onAbout = { destination = OpenAssistDestination.About },
        )
        OpenAssistDestination.Settings -> SettingsScreen(
            viewModel = settings,
            onBack = { destination = OpenAssistDestination.Chat },
            onModelSelection = { destination = OpenAssistDestination.ModelSelection },
            onAiMode = { destination = OpenAssistDestination.AiModeSelector },
        )
        OpenAssistDestination.ModelSelection -> ModelSelectionScreen(
            viewModel = settings,
            onBack = { destination = OpenAssistDestination.Settings },
        )
        OpenAssistDestination.Permissions -> PermissionScreen(onBack = { destination = OpenAssistDestination.Chat })
        OpenAssistDestination.ToolApproval -> ToolApprovalScreen(onBack = { destination = OpenAssistDestination.Chat })
        OpenAssistDestination.MCPServers -> MCPServerScreen(onBack = { destination = OpenAssistDestination.Chat })
        OpenAssistDestination.ConversationHistory -> ConversationHistoryScreen(
            onBack = { destination = OpenAssistDestination.Chat },
            onNewChat = { destination = OpenAssistDestination.Chat },
        )
        OpenAssistDestination.About -> AboutScreen(onBack = { destination = OpenAssistDestination.Chat })
        OpenAssistDestination.AiModeSelector -> AiModeSelectorScreen(
            viewModel = settings,
            onBack = { destination = OpenAssistDestination.Chat },
            onLocalModels = { destination = OpenAssistDestination.LocalModels },
        )
        OpenAssistDestination.LocalModels -> LocalModelsScreen(
            viewModel = settings,
            onBack = { destination = OpenAssistDestination.AiModeSelector },
            onDownloadModels = { destination = OpenAssistDestination.ModelDownload },
            onStorage = { destination = OpenAssistDestination.StorageManager },
        )
        OpenAssistDestination.ModelDownload -> ModelDownloadScreen(
            onBack = { destination = OpenAssistDestination.LocalModels },
            onDownloads = { destination = OpenAssistDestination.DownloadManager },
        )
        OpenAssistDestination.DownloadManager -> DownloadManagerScreen(onBack = { destination = OpenAssistDestination.ModelDownload })
        OpenAssistDestination.StorageManager -> StorageManagerScreen(onBack = { destination = OpenAssistDestination.LocalModels })
    }
}

private fun settingsFactory(storage: SecureStorage) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(storage) as T
    }

private fun chatFactory(storage: SecureStorage, app: OpenAssistApp) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ChatViewModel(storage, app.toolEngine) as T
    }
