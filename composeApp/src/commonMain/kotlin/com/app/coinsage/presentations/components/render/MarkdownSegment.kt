package com.app.coinsage.presentations.components.render

// Markdown segment model
sealed class MarkdownSegment {
    data class Bold(val text: String) : MarkdownSegment()
    data class Text(val text: String) : MarkdownSegment()
}