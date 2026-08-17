package com.fabio.eagleeyes.global

object GeminiConfig {
    private val geminiApiKey = "AQ.Ab8RN6IyK7AqD43nhhz8LQ9lm9Tf_vD1vm9FZfpVnUiXqGRUDQ"
    private val geminiBaseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    private val backendUrl = "https://gemini-render-backend.onrender.com/chat"

    fun getGeminiApiKey(): String = geminiApiKey
    fun getGeminiBaseUrl(): String = geminiBaseUrl
    fun getBackendUrl(): String = backendUrl
}