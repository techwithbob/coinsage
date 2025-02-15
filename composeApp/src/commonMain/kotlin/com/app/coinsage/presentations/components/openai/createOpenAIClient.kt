package com.app.coinsage.presentations.components.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.app.coinsage.presentations.components.Constants.Companion.BASEURL2
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.util.Platform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun createOpenAIClient(baseUrl: String, apiKey: String): OpenAI {
    return OpenAI(
        OpenAIConfig (
            token = apiKey,
            host = OpenAIHost(baseUrl),
            timeout = Timeout(socket = 60.seconds),
            /*httpClientConfig = {
                engine { Platform.Js(jsPlatform = Platform.JsPlatform.Browser) }
                defaultRequest {
                    // Add required headers for OpenRouter
                    headers.append(HttpHeaders.Authorization, "Bearer $apiKey")
                    headers.append("HTTP-Referer", "http://localhost:8080") // Your local domain
                    headers.append("X-Title", "CoinSage") // Your app name
                    // Remove cache-control header as it's causing CORS issues
                    headers.remove("cache-control")
                    headers.remove("Connection")
                }
            }*/
        )
    )
}
fun fetchResponse() {
    scope.launch {
        try {
            error = null
            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("llama-3.3-70b-versatile"),
                messages = listOf(
                    ChatMessage(ChatRole.System, "You are an expert in trading markets"),
                    ChatMessage(ChatRole.User, inputText),
                )
            )

            openAI.chatCompletions(chatCompletionRequest)
                .onEach { chunk ->
                    val newText = chunk.choices.first().delta?.content.orEmpty()
                    if (newText.isNotBlank()) {
                        messageParts.add(newText)
                    }
                }
                .onCompletion {
                    println("Streaming completed.")
                    inputText = ""
                    showOutput = true
                }
                .launchIn(scope)
        } catch (e: Exception) {
            error = "Failed to fetch response: ${e.message}"
            println("Error: ${e.message}")  // Add this for debugging
            e.printStackTrace()  // Add this for more detailed error information
        }
    }
}