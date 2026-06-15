package com.openassist.tools

import com.openassist.PermissionManager
import com.openassist.data.openrouter.ToolDefinition
import com.openassist.data.openrouter.ToolFunction

class ToolEngine(
    tools: List<Tool>,
    private val permissionManager: PermissionManager,
) {
    private val registry: Map<String, Tool> = tools.associateBy { it.name }

    /**
     * Runs a tool by name, enforcing two gates in order:
     *
     * 1. **Runtime permissions** — any permission listed in
     *    [Tool.requiredPermissions] that isn't already granted is requested
     *    now (suspending until the user responds).  If the user denies any
     *    permission the call returns a descriptive [ToolResult] without
     *    calling [Tool.run].
     *
     * 2. **Sensitive-action confirmation** — if [Tool.sensitive] is `true`
     *    and [confirmed] is `false`, returns a [ToolResult] with
     *    [ToolResult.requiresConfirmation] set so the caller can ask the
     *    user before retrying with `confirmed = true`.
     */
    suspend fun execute(request: ToolRequest, confirmed: Boolean = false): ToolResult {
        val tool = registry[request.name]
            ?: return ToolResult(request.name, "Unknown tool: '${request.name}'.")

        // ── Gate 1: runtime permissions ───────────────────────────────────────
        val needed = tool.requiredPermissions
        if (needed.isNotEmpty()) {
            val granted = permissionManager.require(*needed.toTypedArray())
            if (!granted) {
                val denied = needed
                    .filter { !permissionManager.hasPermission(it) }
                    .map { it.substringAfterLast('.') }
                return ToolResult(
                    name   = tool.name,
                    output = "Permission denied: ${denied.joinToString()}. " +
                             "Grant it in Settings → Apps → OpenAssist → Permissions and try again.",
                )
            }
        }

        // ── Gate 2: sensitive-action confirmation ─────────────────────────────
        if (tool.sensitive && !confirmed) {
            return ToolResult(
                name                  = tool.name,
                output                = "Confirmation required before running '${tool.name}'.",
                requiresConfirmation  = true,
            )
        }

        return tool.run(request.arguments)
    }

    fun availableTools(): List<Tool> = registry.values.toList()

    /** Converts the tool registry into the API's tool-definition format. */
    fun toolDefinitions(): List<ToolDefinition> = registry.values.map { tool ->
        ToolDefinition(
            function = ToolFunction(
                name        = tool.name,
                description = tool.description,
                parameters  = tool.parameterSchema,
            ),
        )
    }
}
