// NanoBananaHandler.kt
package com.vaibhav.playground

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NanoBananaHandler {

    suspend fun processVisualFix(context: Context, inputItems: List<ChatItem>): Pair<String?, android.graphics.Bitmap?> =
        withContext(Dispatchers.IO) {

            val model = Firebase.ai().generativeModel(
                modelName = "gemini-2.5-flash-image",
                generationConfig = generationConfig {
                    responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                }
            )

            // Build content: image + instruction text
            val builder = content {
                inputItems.forEach { item ->
                    when (item) {
                        is ChatItem.Image -> {
                            context.contentResolver.openInputStream(Uri.parse(item.uri))?.use { s ->
                                val bytes = s.readBytes()
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                image(bmp)
                            }
                        }
                        is ChatItem.Text -> text(item.text)
                        else -> {}
                    }
                }
                text(
                    """
                    Help the user by visually marking or drawing over the parts
                    of the image that need fixing or attention. Also write one short
                    explanatory line about what was changed.
                    """.trimIndent()
                )
            }

            val response = model.generateContent(builder)
            val candidate = response.candidates.firstOrNull()?.content

            val note = candidate?.parts?.filterIsInstance<com.google.firebase.ai.type.TextPart>()
                ?.joinToString("\n") { it.text }
            val imageBitmap = candidate?.parts
                ?.filterIsInstance<com.google.firebase.ai.type.ImagePart>()
                ?.firstOrNull()?.image
            return@withContext Pair(note, imageBitmap)
        }
}
