package com.app.coinsage.presentations.components.render

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun DynamicTextRenderer(message: String, modifier: Modifier) {
    val parsedMessage = message.parseMarkdown()

    Text(
        buildAnnotatedString {
            parsedMessage.forEach { segment ->
                when (segment) {
                    is MarkdownSegment.Bold -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(segment.text)
                        }
                    }
                    is MarkdownSegment.Text -> append(segment.text)
                }
            }
        },
        color = Color.White,
        modifier = modifier
    )
}