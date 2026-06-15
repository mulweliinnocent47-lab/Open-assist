# OpenAssist

OpenAssist is a native Android AI assistant prototype built with Kotlin, Jetpack Compose, MVVM, Retrofit, OkHttp, Coroutines, StateFlow, EncryptedSharedPreferences, and Material 3.

## Principles

- Bring your own OpenRouter API key.
- No backend, Firebase, Supabase, user accounts, cloud database, or subscription system.
- Store API keys, selected model, and preferences locally on the device.
- Route chat requests directly from Android to OpenRouter.
- Keep a small extensible tool layer for future Android actions and MCP server support.

## Implemented v1 Skeleton

- Onboarding screen for explaining OpenRouter costs and saving the API key locally.
- Settings screen for changing the API key and selected OpenRouter model.
- Chat screen with conversation history, loading state, and error display.
- OpenRouter repository using the Chat Completions endpoint.
- Tool interfaces plus initial device/app tools and confirmation-aware sensitive tool handling.

## OpenAssist v2 AI Modes

OpenAssist supports three AI modes:

- Cloud AI: uses OpenRouter with a user-supplied API key and OpenRouter model selection.
- Local AI: runs downloaded Hugging Face GGUF models offline on device through the planned llama.cpp Android provider.
- Hybrid AI: routes simple prompts to the local provider and complex prompts to OpenRouter based on user-configurable behavior.

Local model files are organized under app storage using this structure:

```text
OpenAssist/
├── models/
│   ├── gemma-2b.gguf
│   ├── smollm2.gguf
│   └── phi4-mini.gguf
├── cache/
├── downloads/
└── chat_history/
```
