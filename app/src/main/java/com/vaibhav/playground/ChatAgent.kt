package com.vaibhav.playground

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

object ChatAgent {

    // 🔧 Initialize model with function-calling tools
    private val generativeModel = Firebase.ai()
        .generativeModel(
            modelName = "gemini-2.5-flash",
            tools = FunctionDeclarations.tools
        )

    // Maintain a persistent chat session
    private val chat = generativeModel.startChat()

    /**
     * Stream multi-turn responses with function-calling support.
     */
    suspend fun streamMessage(
        context: Context,
        inputItems: List<ChatItem>
    ): Flow<String> = flow {

        val builder = Content.Builder()
        val STYLE_PROMPT =
            "Respond clearly, concisely, and avoid long paragraphs. Use short bullet points where possible."

        // 🧠 Agent detection
        val agentType = AgentRouter.classifyAgent(inputItems)
        val systemPrompt = AgentRouter.getSystemPrompt(agentType)

        // Inject role/system prompt first
        if (systemPrompt != null) {
            builder.part(TextPart("$systemPrompt\n\n$STYLE_PROMPT"))
        } else {
            builder.part(TextPart(STYLE_PROMPT))
        }

        // 🖼️ Add multimodal input (text, image, file, etc.)
        inputItems.forEach { item ->
            when (item) {
                is ChatItem.Text -> builder.part(TextPart(item.text))
                is ChatItem.Image -> {
                    context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { s ->
                        val bytes = s.readBytes()
                        val bitmap =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        builder.image(bitmap)
                    }
                }

                is ChatItem.File -> {
                    context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { s ->
                        val mime =
                            context.contentResolver.getType(Uri.parse(item.uri))
                                ?: "application/pdf"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }

                is ChatItem.Audio -> {
                    val uri = Uri.parse(item.uri)
                    context.contentResolver.openInputStream(uri)?.use { s ->
                        val mime =
                            context.contentResolver.getType(uri) ?: "audio/wav"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }

                is ChatItem.Video -> {
                    val uri = Uri.parse(item.uri)
                    context.contentResolver.openInputStream(uri)?.use { s ->
                        val mime =
                            context.contentResolver.getType(uri) ?: "video/mp4"
                        builder.inlineData(s.readBytes(), mime)
                    }
                }

                else -> {}
            }
        }

        emit("🤖 Detected ${agentType.name.lowercase().replaceFirstChar { it.uppercase() }} agent...")

        try {
            var response = chat.sendMessage(builder.build())
            val calls = response.functionCalls

            if (calls.isNotEmpty()) {
                for (call in calls) {
                    emit("⚙️ ${agentType.name.lowercase().replaceFirstChar { it.uppercase() }} agent invoking ${call.name}…")

                    when (call.name) {
                        "getCoordinates" -> {
                            val city = call.args["city"]!!.jsonPrimitive.content
                            emit("📍 Fetching coordinates for $city…")
                            val coords = FunctionHandlers.getCoordinates(city)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("getCoordinates", coords))
                                }
                            )
                        }

                        "fetchWeather" -> {
                            val lat = call.args["lat"]!!.jsonPrimitive.double
                            val lon = call.args["lon"]!!.jsonPrimitive.double
                            emit("🌦️ Getting real-time weather data from OpenWeather API…")
                            val weather = FunctionHandlers.fetchWeather(lat, lon)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("fetchWeather", weather))
                                }
                            )
                        }

                        // Future: add finance, news, etc.
                    }
                }

                // 🔁 Ask Gemini to summarize based on fetched function data
                emit("🧩 Composing final summary from fetched data…")
                response = chat.sendMessage(
                    content("user") {
                        part(
                            TextPart(
                                "Now summarize the results above briefly and naturally for the user in one paragraph."
                            )
                        )
                    }
                )
            }

            emit("✅ ${agentType.name.lowercase().replaceFirstChar { it.uppercase() }} agent finished reasoning.")
            emit(response.text ?: "No response from model.")
        } catch (e: Exception) {
            emit("⚠️ Error: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)
}
