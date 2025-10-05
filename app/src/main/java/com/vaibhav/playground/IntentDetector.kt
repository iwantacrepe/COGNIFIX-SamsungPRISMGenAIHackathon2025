package com.vaibhav.playground

object IntentDetector {

    //  Keywords for different visual intent categories
    private val visualFixKeywords = listOf(
        "fix", "repair", "problem", "issue", "broken", "restore", "make better",
        "improve", "adjust", "clean", "remove", "correct", "straighten", "enhance"
    )

    private val visualExplainKeywords = listOf(
        "explain", "highlight", "show", "mark", "point out", "identify", "circle",
        "indicate", "draw", "outline", "label", "describe", "where is", "which part"
    )

    private val visualCompareKeywords = listOf(
        "difference", "compare", "what changed", "before", "after", "spot the difference"
    )

    private val visualFeedbackKeywords = listOf(
        "feedback", "review", "analyze", "evaluate", "what’s wrong", "find error",
        "check", "inspect", "assessment", "mistake"
    )

    private val creativeEditKeywords = listOf(
        "edit", "annotate", "draw on", "add text", "mark this", "make illustration",
        "make tutorial", "make guide", "make infographic", "explain visually"
    )

    /**
     *  Decides whether the input should trigger NanoBananaHandler (image editing + visual markup)
     */
    fun shouldUseNanoBanana(inputItems: List<ChatItem>): Boolean {
        val hasImage = inputItems.any { it is ChatItem.Image }
        if (!hasImage) return false

        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text.lowercase() }

        // Combine all keyword lists
        val allKeywords = visualFixKeywords +
                visualExplainKeywords +
                visualCompareKeywords +
                visualFeedbackKeywords +
                creativeEditKeywords

        // Detect presence of any relevant phrase
        val hasIntent = allKeywords.any { kw ->
            text.contains(kw)
        }

        //  Contextual rules (smart heuristics)
        val isHowToQuestion = text.startsWith("how") || text.contains("how to")
        val mentionsShow = text.contains("show") || text.contains("demonstrate")
        val mentionsHighlight = text.contains("highlight") || text.contains("mark")

        return hasImage && (hasIntent || isHowToQuestion || mentionsShow || mentionsHighlight)
    }

    /**
     * Optional: returns the type of NanoBanana intent — fix, explain, or creative.
     */
    fun getNanoMode(inputItems: List<ChatItem>): String {
        val text = inputItems.filterIsInstance<ChatItem.Text>()
            .joinToString(" ") { it.text.lowercase() }

        return when {
            visualFixKeywords.any { text.contains(it) } -> "fix"
            visualExplainKeywords.any { text.contains(it) } -> "explain"
            creativeEditKeywords.any { text.contains(it) } -> "creative"
            visualCompareKeywords.any { text.contains(it) } -> "compare"
            visualFeedbackKeywords.any { text.contains(it) } -> "feedback"
            else -> "general"
        }
    }
}