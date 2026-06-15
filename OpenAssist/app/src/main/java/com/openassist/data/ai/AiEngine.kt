package com.openassist.data.ai

interface AiProvider {
    val name: String
    suspend fun generate(prompt: String): String
}

class OpenRouterProvider : AiProvider {
    override val name = "OpenRouter Provider"
    override suspend fun generate(prompt: String): String = "Cloud response for: $prompt"
}

class LocalLlmProvider : AiProvider {
    override val name = "Local LLM Provider"
    override suspend fun generate(prompt: String): String = "Offline llama.cpp response for: $prompt"

    fun modelPath(modelFile: String): String = "OpenAssist/models/$modelFile"
}

class HybridProvider(
    private val localProvider: LocalLlmProvider = LocalLlmProvider(),
    private val cloudProvider: OpenRouterProvider = OpenRouterProvider(),
) : AiProvider {
    override val name = "Hybrid Provider"
    override suspend fun generate(prompt: String): String =
        if (prompt.length < 240) localProvider.generate(prompt) else cloudProvider.generate(prompt)
}
