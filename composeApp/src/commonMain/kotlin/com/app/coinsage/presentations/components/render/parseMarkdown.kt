package com.app.coinsage.presentations.components.render

// Helper: Parses markdown-like text (e.g., **Bold Text**) into segments
fun String.parseMarkdown(): List<MarkdownSegment> {
    val regex = Regex("\\*\\*(.*?)\\*\\*")
    val segments = mutableListOf<MarkdownSegment>()
    var lastIndex = 0
    regex.findAll(this).forEach { match ->
        val textBefore = substring(lastIndex, match.range.first)
        if (textBefore.isNotEmpty()) {
            segments.add(MarkdownSegment.Text(textBefore))
        }
        segments.add(MarkdownSegment.Bold(match.groups[1]?.value.orEmpty()))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < length) {
        segments.add(MarkdownSegment.Text(substring(lastIndex)))
    }
    return segments
}