package com.app.coinsage.presentations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.model.ModelId
import com.app.coinsage.presentations.components.Constants.Companion.BASEURL
import com.app.coinsage.presentations.components.Constants.Companion.GROQ_API
import com.app.coinsage.presentations.components.openai.createOpenAIClient
import com.app.coinsage.presentations.components.openai.prompt
import com.app.coinsage.presentations.components.render.DynamicTextRenderer
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.ktor.util.encodeBase64
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class Main {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard() {
    var loadingState by remember { mutableStateOf(false) }

    // Custom animation values
    val backgroundAnimation = rememberInfiniteTransition()
    val gradientOffset = backgroundAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Shimmer effect for loading
    val shimmerEffect = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF2D1B6B),
                Color(0xFF3D2B7B),
                Color(0xFF2D1B6B)
            ),
            start = Offset(gradientOffset.value - 200f, gradientOffset.value - 200f),
            end = Offset(gradientOffset.value + 200f, gradientOffset.value + 200f)
        )
    }
    val nonShimmer = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(0.1f),
            )
        )
    }
    val openAI = remember {
        createOpenAIClient(
            baseUrl = BASEURL,
            apiKey = GROQ_API
        )
    }

    var error by remember { mutableStateOf<String?>(null) }
    val messageParts = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    var showOutput by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    //var forceRecompose by remember { mutableStateOf(false) }  // Force recompose manually
    var bytes by remember { mutableStateOf<ByteArray?>(null) }
    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Single,
        title = "Upload Photo/Screenshots"
    ) { platformFile ->
        platformFile?.let { file ->
            scope.launch {
                loadingState = true
                bytes = if (file.supportsStreams()) {
                    val size = file.getSize()
                    if (size != null && size > 0L) {
                        val buffer = ByteArray(size.toInt())
                        val tmpBuffer = ByteArray(1000)
                        var totalBytesRead = 0
                        file.getStream().use {
                            while (it.hasBytesAvailable()) {
                                val numRead = it.readInto(tmpBuffer, 1000)
                                tmpBuffer.copyInto(buffer, destinationOffset = totalBytesRead, endIndex = numRead)
                                totalBytesRead += numRead
                            }
                        }
                        buffer
                    } else {
                        file.readBytes()
                    }
                } else {
                    file.readBytes()
                }
                //forceRecompose = !forceRecompose // This forces recomposition
            }
        }
    }


    fun fetchImageResponse(img: ByteArray) {
        loadingState = true
        scope.launch {
            try {
                error = null  // Reset error
                val chatCompletionRequest = ChatCompletionRequest(
                    model = ModelId("llama-3.2-90b-vision-preview"),
                    messages = listOf(
                        //ChatMessage(ChatRole.System, "You are an expert in trading markets"),
                        ChatMessage.User(
                            listOf(
                                TextPart(prompt),
                                ImagePart( "data:image/jpeg;base64," + img.encodeBase64()),
                            ), "trading chart image"
                        )
                    ),
                    /*tools = listOf(
                        Tool(
                            type = ToolType.Function,
                            function = FunctionTool(
                                name = "Trading AI Analysis",
                                description = "Advanced AI trader analyzing the chart image...",
                                parameters = Parameters.fromJsonString(jsonString)
                            )
                        )
                    )*/
                )

                var collectedResponse = ""  // Collect the response into a single string

                openAI.chatCompletions(chatCompletionRequest)
                    .onEach { chunk ->
                        val newText = chunk.choices.first().delta?.content.orEmpty()
                        if (newText.isNotBlank()) {
                            collectedResponse += newText  // Append new text to collected response
                        }
                    }
                    .onCompletion {
                        loadingState = false
                        println("Streaming completed.")
                        if (collectedResponse.isNotBlank()) {
                            messageParts.add(collectedResponse) // Store final response
                        }
                        println(messageParts.toList()) // Print after all messages are collected
                        showOutput = true
                    }
                    .launchIn(scope)
            } catch (e: Exception) {
                error = "Failed to fetch response: ${e.message}"
                loadingState = false
            }
        }
    }
    LaunchedEffect(messageParts) {
        if (messageParts.isNotEmpty()) {
            println("Final message received: ${messageParts.joinToString(" ")}")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Animated background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2D1B6B),
                        Color(0xFF3D2B7B),
                        Color(0xFF4D3B8B),
                        Color(0xFFBBB3E5)
                    ),
                    startY = gradientOffset.value
                )
            )
        }
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2D1B6B), Color(0xFFBBB3E5))
                    )
                )
            //.blur(16.dp) // Applying blur effect
            ,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Coin")
                            }
                            append(" Sage")
                        }, color = Color.White, textAlign = TextAlign.Center)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2D1B6B))
                )
            },
            containerColor = Color(0xFF2D1B6B)
        ) { paddingValues ->
            bytes?.let { fetchImageResponse(it) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        error?.let {
                            Text(
                                text = it,
                                color = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = messageParts.isNotEmpty(),
                        label = "content",
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it }).togetherWith(fadeOut() + slideOutVertically { -it })
                        }
                    ) { hasMessages ->
                        if (hasMessages) {
                            DynamicTextRenderer(
                                message = messageParts.joinToString(""),
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .width(200.dp)
                                    .background(brush = if(!loadingState) shimmerEffect else nonShimmer, RoundedCornerShape(16.dp) )
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { launcher.launch() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                    ),
                                    enabled = !loadingState
                                ) {
                                    AnimatedContent(
                                        targetState = if (showOutput) "Finish uploaded" else "Upload",
                                        label = "button_text"
                                    ) { text ->
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}