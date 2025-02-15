package com.app.coinsage.presentations

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coinsage.composeapp.generated.resources.Res
import coinsage.composeapp.generated.resources.camera
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
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
import org.jetbrains.compose.resources.painterResource

class Main {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
            }
        }
    }
    LaunchedEffect(messageParts) {
        if (messageParts.isNotEmpty()) {
            println("Final message received: ${messageParts.joinToString(" ")}")
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text(text = "Profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Info") },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {  }
                )
            }
        }
    ) {
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
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            BadgedBox(
                                badge = {
                                    //Badge()
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2D1B6B))
                )
            },
            floatingActionButton = { FloatingActionButton(onClick = {},
                content = {Icon(
                    painter = painterResource(Res.drawable.camera),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )}) },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color(0xFF2D1B6B)
        ) { paddingValues ->
            bytes?.let { fetchImageResponse(it) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                state = listState,  // Important: Use the listState
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    AnimatedContent(
                        targetState = messageParts.isNotEmpty(),
                        label = "content"
                    ) { hasMessages ->
                        if (hasMessages) {
                            DynamicTextRenderer(message = messageParts.joinToString(""))
                        } else {
                            Column(
                                modifier = Modifier.width(150.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = { launcher.launch() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (showOutput) "Finish uploaded" else "Upload",
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                /*Button(
                                    onClick = { fetchResponse() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Chat", modifier = Modifier.padding(8.dp))
                                }*/
                            }
                        }
                    }
                }
            }
        }
    }
}