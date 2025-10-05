package com.vaibhav.playground

object IntentDetector {
    private val visualFixKeywords = listOf(
        "fix", "repair", "problem", "issue", "broken", "how to do", "help me"
    )

    fun shouldUseNanoBanana(inputItems: List<ChatItem>): Boolean {
        val hasImage = inputItems.any { it is ChatItem.Image }
        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text.lowercase() }
        val isVisualFix = visualFixKeywords.any { text.contains(it) }
        return hasImage && isVisualFix
    }
}
