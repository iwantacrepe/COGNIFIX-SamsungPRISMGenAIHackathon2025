package com.vaibhav.playground

/* -------------------- Chat Items -------------------- */
sealed class ChatItem {
    class Text(val text: String) : ChatItem()
    class Image(val uri: String) : ChatItem()
    class File(val uri: String, val fileName: String) : ChatItem()
    class Audio(val uri: String) : ChatItem()
    class Video(val uri: String) : ChatItem()

    data class Markdown(val content: String) : ChatItem()
}

/* -------------------- Agent Responses -------------------- */
sealed class AgentResponse {
    class Text(val text: String) : AgentResponse()
    class Image(val uri: String) : AgentResponse()
    class File(val uri: String, val fileName: String) : AgentResponse()
    class Audio(val uri: String) : AgentResponse()
    class Video(val uri: String) : AgentResponse()
    class Link(val url: String) : AgentResponse()
}

/* -------------------- Chat Messages (for UI) -------------------- */
sealed class ChatMessage(val isUser: Boolean) {
    class GroupMessage(val items: List<ChatItem>, isUser: Boolean) : ChatMessage(isUser)
}
