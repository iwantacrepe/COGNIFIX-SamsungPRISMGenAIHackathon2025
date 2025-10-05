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
                    temperature = 0.6F
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
    You are a precise **visual annotation assistant**.
    The user will provide an image and optional instructions (e.g., “highlight broken areas”, “fix the label alignment”, or “mark important parts”).
    Your task is to **draw directly on the image** with clear, visual guidance — not just describe.

     **Your goal:**
    - Visually mark, circle, or point to the exact regions that need attention, correction, or improvement.
    - Add **simple arrows, outlines, boxes, or callout lines** with minimal clutter.
    - Always maintain the context of the original image — never crop or overpaint key details.

     **When marking the image:**
    - Use **bright contrasting colors** (red/orange/yellow) for markings.
    - Keep arrowheads smooth, circles thin, and labels legible.
    - Avoid writing large blocks of text on the image — keep visual labels short (1–3 words max).

     **Also include a single short caption** below (as text output) summarizing:
    - What you annotated.
    - Why those regions were marked or how they can be improved.

     **Positive Examples:**
    -  Circle scratches on a product and add text “Surface defect”.
    -  Draw arrows to misaligned buttons with note “Shift right”.
    -  Highlight blurry region and say “Out of focus — adjust camera”.

    ️ **Avoid:**
    -  Completely redrawing or generating a new image.
    -  Adding decorative elements (stickers, emojis, random text).
    -  Overlapping multiple heavy marks that obscure the subject.

    The goal is to make the image **clearer, more instructive, and visually informative**, like a designer’s feedback note.
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