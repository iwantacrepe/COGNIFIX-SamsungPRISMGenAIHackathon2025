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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive

object ChatAgent {

    private val generativeModel = Firebase.ai()
        .generativeModel(
            modelName = "gemini-2.5-flash",
            tools = FunctionDeclarations.tools
        )

    // ✅ Chat with persistent memory
    // replace this line
// private val chat = generativeModel.startChat()

    // with:
    private var _chat = generativeModel.startChat(
        history = mutableListOf(
            content(role = "model") { text("Hi! I'm Cognifix — your multimodal AI assistant.") }
        )
    )
    val chat get() = _chat


    /**
     * Stream multi-turn responses with function-calling support.
     */
    fun resetChat() {
        // clear the current chat history and start a fresh one
        _chat = generativeModel.startChat(
            history = mutableListOf(
                content(role = "model") { text("Hi! I'm Cognifix — your multimodal AI assistant.") }
            )
        )
    }
    suspend fun streamMessage(
        context: Context,
        inputItems: List<ChatItem>
    ): Flow<String> = flow {

        val builder = Content.Builder()
        val STYLE_PROMPT = "Respond concisely but naturally. Keep answers short and relevant."

        val agentType = AgentRouter.classifyAgent(inputItems)
        val systemPrompt = AgentRouter.getSystemPrompt(agentType)

        // Inject role/system prompt first
        if (systemPrompt != null) builder.part(TextPart("$systemPrompt\n\n$STYLE_PROMPT"))
        else builder.part(TextPart(STYLE_PROMPT))

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

        val userMessage = content(role = "user") {
            text(inputItems.filterIsInstance<ChatItem.Text>().joinToString(" ") { it.text })
        }

        emit("🤖 Detected ${agentType.name.lowercase().replaceFirstChar { it.uppercase() }} agent...")

        try {
            chat.history.add(userMessage)
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

                        "fetchStockData" -> {
                            val query = call.args["query"]?.jsonPrimitive?.content ?: run {
                                emit("⚠️ Missing query argument — unable to fetch stock data.")
                                continue
                            }
                            emit("💰 Fetching live price for $query via Financial Modeling Prep API...")
                            val stock = FunctionHandlers.fetchStockData(query)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("fetchStockData", stock))
                                }
                            )
                        }
                        "fetchExchangeRate" -> {
                            val base = call.args["base"]?.jsonPrimitive?.content?.uppercase() ?: "USD"
                            val target = call.args["target"]?.jsonPrimitive?.content?.uppercase() ?: "INR"
                            emit("💱 Fetching live forex rate for $base → $target ...")
                            val forex = FunctionHandlers.fetchExchangeRate(base, target)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("fetchExchangeRate", forex))
                                }
                            )
                        }

                        "fetchFlights" -> {
                            val source = call.args["source"]?.jsonPrimitive?.content ?: ""
                            val destination = call.args["destination"]?.jsonPrimitive?.content ?: ""
                            val date = call.args["date"]?.jsonPrimitive?.contentOrNull
                            emit("🛫 Searching flights from $source to $destination...")
                            val flights = FunctionHandlers.fetchFlights(source, destination, date)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("fetchFlights", flights))
                                }
                            )
                        }



                        "fetchWebSearchResults" -> {
                            val query = call.args["query"]!!.jsonPrimitive.content
                            emit("🌐 Searching web for: $query …")
                            val result = FunctionHandlers.fetchWebSearchResults(query)
                            response = chat.sendMessage(
                                content("function") {
                                    part(FunctionResponsePart("fetchWebSearchResults", result))
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

            chat.history.add(content(role = "model") { text(response.text ?: "No response from model.") })

            emit("✅ ${agentType.name.lowercase().replaceFirstChar { it.uppercase() }} agent finished reasoning.")
            emit(response.text ?: "No response from model.")
        } catch (e: Exception) {
            emit("⚠️ Error: ${e.localizedMessage}")
        }
    }.flowOn(Dispatchers.IO)
}
