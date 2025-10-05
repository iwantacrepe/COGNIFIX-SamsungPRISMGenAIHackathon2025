package com.vaibhav.playground

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object ChatAgent {
    private val generativeModel = Firebase.ai()
        .generativeModel("gemini-2.5-flash")

    // Single chat session to maintain conversation history
    private val chat = generativeModel.startChat()

    /**
     * Stream multi-turn responses (chat + memory).
     */
    suspend fun streamMessage(
        context: Context,
        inputItems: List<ChatItem>
    ): Flow<String> = flow {
        val builder = Content.Builder()

        // 🔎 Classify agent
        val agentType = AgentRouter.classifyAgent(inputItems)
        val systemPrompt = AgentRouter.getSystemPrompt(agentType)

        // ✅ Inject system role first if needed
        if (systemPrompt != null) {
            builder.part(TextPart(systemPrompt))
        }

        // Build prompt with multimodal input
        inputItems.forEach { item ->
            when (item) {
                is ChatItem.Text -> builder.part(TextPart(item.text))
                is ChatItem.Image -> {
                    context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { s ->
                        val bytes = s.readBytes()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        builder.image(bitmap)
                    }
                }
                is ChatItem.File -> {
                    context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { s ->
                        val mime = context.contentResolver.getType(Uri.parse(item.uri)) ?: "application/pdf"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }
                is ChatItem.Audio -> {
                    val uri = Uri.parse(item.uri)
                    context.contentResolver.openInputStream(uri)?.use { s ->
                        val mime = context.contentResolver.getType(uri) ?: "audio/wav"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }
                is ChatItem.Video -> {
                    val uri = Uri.parse(item.uri)
                    context.contentResolver.openInputStream(uri)?.use { s ->
                        val mime = context.contentResolver.getType(uri) ?: "video/mp4"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }

                else-> {}
            }
        }

        try {
            chat.sendMessageStream(builder.build()).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("⚠️ Error: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)
}
