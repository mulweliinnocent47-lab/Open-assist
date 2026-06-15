package com.openassist.data.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// ── Tool definitions (sent to the API) ──────────────────────────────────────

@Serializable
data class ToolFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
data class ToolDefinition(
    val type: String = "function",
    val function: ToolFunction,
)

// ── Tool-call response fields (received from the API) ────────────────────────

@Serializable
data class ToolCallFunction(
    val name: String,
    val arguments: String, // raw JSON string, e.g. {"hour":7,"minute":30}
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String = "function",
    val function: ToolCallFunction,
)

// ── Messages ─────────────────────────────────────────────────────────────────

/** Unified message type for all roles: user, assistant, system, tool. */
@Serializable
data class OpenRouterMessage(
    val role: String,
    /** Null when the assistant message contains only tool_calls. */
    val content: String? = null,
    /** Present on assistant messages that invoke one or more tools. */
    @SerialName("tool_calls") val toolCalls: List<ToolCall>? = null,
    /** Present on role="tool" messages; matches ToolCall.id. */
    @SerialName("tool_call_id") val toolCallId: String? = null,
)

// ── Request / Response ───────────────────────────────────────────────────────

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    /** Omitted (null) when there are no tools so the API behaves normally. */
    val tools: List<ToolDefinition>? = null,
)

@Serializable
data class ChatResponse(val choices: List<Choice> = emptyList())

@Serializable
data class Choice(
    val message: OpenRouterMessage,
    @SerialName("finish_reason") val finishReason: String? = null,
)

// ── Model catalogue ──────────────────────────────────────────────────────────

@Serializable
data class ModelsResponse(val data: List<ModelInfo> = emptyList())

@Serializable
data class ModelInfo(
    val id: String,
    val name: String? = null,
    @SerialName("context_length") val contextLength: Int? = null,
)
