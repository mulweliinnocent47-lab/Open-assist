package com.openassist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openassist.data.ai.AiMode
import com.openassist.data.ai.HybridProvider
import com.openassist.data.ai.LocalLlmProvider
import com.openassist.data.local.SecureStorage
import com.openassist.data.openrouter.OpenRouterMessage
import com.openassist.data.openrouter.OpenRouterRepository
import com.openassist.tools.ToolEngine
import com.openassist.tools.ToolRequest
import com.openassist.ui.chat.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ChatUiState(
    /** Messages shown in the UI (user + final assistant text only). */
    val messages: List<ChatMessage> = emptyList(),
    /**
     * Full conversation as sent to the API — includes assistant tool-call
     * turns and tool-result turns that are invisible in the UI.
     */
    val apiHistory: List<OpenRouterMessage> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class ChatViewModel(
    private val storage: SecureStorage,
    private val toolEngine: ToolEngine,
    private val repository: OpenRouterRepository = OpenRouterRepository(),
    private val localProvider: LocalLlmProvider = LocalLlmProvider(),
    private val hybridProvider: HybridProvider = HybridProvider(),
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun send(content: String) {
        if (content.isBlank()) return
        val trimmed = content.trim()

        // Optimistically append the user bubble.
        val userDisplay = ChatMessage(role = "user", content = trimmed)
        val userApiMsg  = OpenRouterMessage(role = "user", content = trimmed)
        val nextMessages    = _state.value.messages + userDisplay
        val nextApiHistory  = _state.value.apiHistory + userApiMsg

        _state.value = _state.value.copy(
            messages   = nextMessages,
            apiHistory = nextApiHistory,
            loading    = true,
            error      = null,
        )

        viewModelScope.launch {
            runCatching { runAgentLoop(nextApiHistory) }
                .onSuccess { (newHistory, reply) ->
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + ChatMessage(
                            role    = "assistant",
                            content = reply.ifBlank { "(No response)" },
                        ),
                        apiHistory = newHistory,
                        loading    = false,
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(loading = false, error = err.message)
                }
        }
    }

    // ── Agent loop ────────────────────────────────────────────────────────────

    /**
     * Drives the tool-calling loop:
     *
     * 1. Send messages + tool definitions to the API.
     * 2. If the model wants to call tools → execute them, append results, repeat.
     * 3. Stop when the model emits a plain text response (no tool_calls).
     *
     * Guards against runaway loops with [maxIterations].
     */
    private suspend fun runAgentLoop(
        initialHistory: List<OpenRouterMessage>,
    ): Pair<List<OpenRouterMessage>, String> {
        when (storage.aiMode.value) {
            AiMode.Local -> {
                val prompt = initialHistory.lastOrNull { it.role == "user" }?.content.orEmpty()
                val reply = localProvider.generate(prompt)
                return Pair(initialHistory + OpenRouterMessage(role = "assistant", content = reply), reply)
            }
            AiMode.Hybrid -> {
                val prompt = initialHistory.lastOrNull { it.role == "user" }?.content.orEmpty()
                if (prompt.length <= storage.hybridThreshold.value) {
                    val reply = hybridProvider.generate(prompt)
                    return Pair(initialHistory + OpenRouterMessage(role = "assistant", content = reply), reply)
                }
            }
            AiMode.Cloud -> Unit
        }

        require(storage.apiKey.value.isNotBlank()) { "OpenRouter API key is required for Cloud AI or complex Hybrid AI requests." }

        val tools = toolEngine.toolDefinitions()
        var history = initialHistory
        var iterations = 0
        val maxIterations = 10

        while (iterations++ < maxIterations) {
            val choice = repository.chat(
                apiKey   = storage.apiKey.value,
                model    = storage.model.value,
                messages = history,
                tools    = tools,
            )
            val assistantMsg = choice.message

            if (!assistantMsg.toolCalls.isNullOrEmpty()) {
                // ── Tool-call turn ────────────────────────────────────────────
                history = history + assistantMsg  // keep assistant msg in history

                val toolResultMsgs = assistantMsg.toolCalls.map { toolCall ->
                    val args   = parseArguments(toolCall.function.arguments)
                    val result = toolEngine.execute(
                        request   = ToolRequest(toolCall.function.name, args),
                        confirmed = true,   // model intent == implicit confirmation
                    )
                    OpenRouterMessage(
                        role       = "tool",
                        content    = result.output,
                        toolCallId = toolCall.id,
                    )
                }
                history = history + toolResultMsgs
            } else {
                // ── Final text response ───────────────────────────────────────
                history = history + assistantMsg
                return Pair(history, assistantMsg.content.orEmpty())
            }
        }

        // Should only happen if the model calls tools non-stop for maxIterations.
        return Pair(history, "Agent reached the maximum number of tool-call iterations.")
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Parses the JSON argument string emitted by the model into a flat
     * [Map<String, String>].  Non-primitive values are serialised to their
     * JSON representation so tools always receive a usable string.
     */
    private fun parseArguments(jsonString: String): Map<String, String> = try {
        Json.decodeFromString<JsonObject>(jsonString).mapValues { (_, v) ->
            try { v.jsonPrimitive.content } catch (_: Exception) { v.toString() }
        }
    } catch (_: Exception) {
        emptyMap()
    }
}
