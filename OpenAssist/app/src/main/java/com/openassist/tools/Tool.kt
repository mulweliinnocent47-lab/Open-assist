package com.openassist.tools

import kotlinx.serialization.json.JsonObject

data class ToolRequest(val name: String, val arguments: Map<String, String> = emptyMap())

data class ToolResult(
    val name: String,
    val output: String,
    val requiresConfirmation: Boolean = false,
)

interface Tool {
    val name: String
    val description: String
    val sensitive: Boolean
    val parameterSchema: JsonObject

    /**
     * Android permission strings this tool needs at runtime.
     * [ToolEngine] checks and requests these before calling [run].
     * Empty list = no dangerous permissions required.
     */
    val requiredPermissions: List<String> get() = emptyList()

    suspend fun run(arguments: Map<String, String>): ToolResult
}
