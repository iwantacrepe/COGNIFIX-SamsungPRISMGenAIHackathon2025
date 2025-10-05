package com.vaibhav.playground

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@Composable
fun ChatPage(navController: NavHostController) {
    val context = LocalContext.current

    var input by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var pendingAttachments by remember { mutableStateOf(listOf<ChatItem>()) }

    // ---------- Image Picker ----------
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { pendingAttachments = pendingAttachments + ChatItem.Image(it.toString()) }
    }

    // Camera Photo Launcher
    val photoUri = remember {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingAttachments = pendingAttachments + ChatItem.Image(photoUri.toString())
        }
    }

    // Camera Video Launcher
    val videoUri = remember {
        val file = File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            pendingAttachments = pendingAttachments + ChatItem.Video(videoUri.toString())
        }
    }


    // ---------- File, Audio & Video Picker ----------
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val type = context.contentResolver.getType(uri) ?: ""
            when {
                type.startsWith("audio/") -> {
                    pendingAttachments = pendingAttachments + ChatItem.Audio(it.toString())
                }
                type.startsWith("video/") -> {
                    pendingAttachments = pendingAttachments + ChatItem.Video(it.toString())
                }
                else -> {
                    val name = uri.lastPathSegment ?: "Document"
                    pendingAttachments = pendingAttachments + ChatItem.File(it.toString(), name)
                }
            }
        }
    }

    // ---------- Speech to Text ----------
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            spokenText?.let { input = TextFieldValue(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 24.dp)
    ) {
        /* ---------- Top Bar ---------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cognifix Chat", style = MaterialTheme.typography.headlineSmall, color = Color.Black)
            TextButton(onClick = {
                messages = emptyList()
                ChatAgent.resetChat()   //  clear Gemini conversation context
            }) {
                Icon(Icons.Filled.Refresh, contentDescription = "New Chat", tint = Color.Black)
                Spacer(Modifier.width(4.dp))
                Text("New Chat", color = Color.Black)
            }
        }

        /* ---------- Welcome or Messages ---------- */
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("How can I help you?", fontSize = 20.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(messages) { msg ->
                    if (msg is ChatMessage.GroupMessage) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .background(
                                        if (msg.isUser) Color.Black else Color(0xFFF2F2F2),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // --- Attachments (Image/File/Audio/Video) ---
                                // --- Attachments (Image/File/Audio/Video) ---
                                msg.items.filter { it !is ChatItem.Text }.forEach { item ->
                                    when (item) {
                                        //  Image preview
                                        is ChatItem.Image -> AsyncImage(
                                            model = item.uri,
                                            contentDescription = "Sent image",
                                            modifier = Modifier
                                                .size(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )

                                        //  Video preview + playback
                                        is ChatItem.Video -> {
                                            AndroidView(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                factory = { ctx ->
                                                    PlayerView(ctx).apply {
                                                        useController = true
                                                        player = ExoPlayer.Builder(ctx).build().apply {
                                                            setMediaItem(MediaItem.fromUri(Uri.parse(item.uri)))
                                                            prepare()
                                                        }
                                                    }
                                                }
                                            )
                                        }

                                        //  Audio playback
                                        is ChatItem.Audio -> {
                                            var isPlaying by remember { mutableStateOf(false) }
                                            val mediaPlayer = remember {
                                                MediaPlayer().apply {
                                                    setDataSource(context, Uri.parse(item.uri))
                                                    prepare()
                                                }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.LightGray)
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🎵 Audio", modifier = Modifier.weight(1f))
                                                IconButton(onClick = {
                                                    if (isPlaying) mediaPlayer.pause() else mediaPlayer.start()
                                                    isPlaying = !isPlaying
                                                }) {
                                                    Icon(
                                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = "Play/Pause"
                                                    )
                                                }
                                            }
                                        }

                                        //  File preview (PDF/doc)
                                        is ChatItem.File -> {
                                            val uri = Uri.parse(item.uri)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.LightGray)
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("📄 ${item.fileName}", modifier = Modifier.weight(1f))
                                                TextButton(onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "application/pdf")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                }) {
                                                    Text("Open")
                                                }
                                            }
                                        }

                                        else -> {}
                                    }
                                }


                                // --- Text messages ---
//                                msg.items.filterIsInstance<ChatItem.Text>().forEach { textItem ->
//                                    Text(
//                                        textItem.text,
//                                        fontSize = 16.sp,
//                                        color = if (msg.isUser) Color.White else Color.Black
//                                    )
//                                }

                                msg.items.forEach { item ->
                                    when (item) {
                                        is ChatItem.Text -> Text(
                                            item.text,
                                            fontSize = 16.sp,
                                            color = if (msg.isUser) Color.White else Color.Black
                                        )
                                        is ChatItem.Markdown -> {
                                            // Use Markdown composable from mikepenz library
                                            com.mikepenz.markdown.m3.Markdown(
                                                content = item.content,
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            )
                                        }
                                        else -> {}
                                    }
                                }



                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        /* ---------- Pending Attachments Preview ---------- */
        if (pendingAttachments.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pendingAttachments.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier.size(60.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        when (item) {
                            is ChatItem.Image -> AsyncImage(
                                model = item.uri,
                                contentDescription = "Pending image",
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            is ChatItem.File -> Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) { Text("📎", fontSize = 20.sp) }

                            is ChatItem.Audio -> Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) { Text("🎵") }

                            is ChatItem.Video -> Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) { Text("🎬") }

                            else -> {}
                        }

                        //  Remove button
                        IconButton(
                            onClick = {
                                pendingAttachments = pendingAttachments.toMutableList().also { it.removeAt(index) }
                            },
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.Black, CircleShape)
                        ) {
                            Text("✕", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }


        /* ---------- Input Bar ---------- */
        val coroutineScope = rememberCoroutineScope()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F7F7), shape = RoundedCornerShape(25.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var showImageOptions by remember { mutableStateOf(false) }

            IconButton(onClick = { showImageOptions = true }) {
                Icon(Icons.Filled.Image, contentDescription = "Image/Camera", tint = Color.Gray)
            }

            if (showImageOptions) {
                AlertDialog(
                    onDismissRequest = { showImageOptions = false },
                    title = { Text("Choose option") },
                    confirmButton = {},
                    text = {
                        Column {
                            TextButton(onClick = {
                                imagePickerLauncher.launch("image/*")
                                showImageOptions = false
                            }) { Text("📁 Pick from Gallery") }

                            TextButton(onClick = {
                                cameraLauncher.launch(photoUri)
                                showImageOptions = false
                            }) { Text("📷 Take Photo") }

                            TextButton(onClick = {
                                videoLauncher.launch(videoUri)
                                showImageOptions = false
                            }) { Text("🎥 Record Video") }
                        }
                    }
                )
            }


            // File + Audio + Video
            IconButton(onClick = {
                filePickerLauncher.launch(arrayOf("application/pdf", "text/plain", "audio/*", "video/*"))
            }) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach", tint = Color.Gray)
            }

            // Text input
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 10.dp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (input.text.isEmpty()) {
                        Text("Ask anything", color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )

            // Mic
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
                }
                speechLauncher.launch(intent)
            }) {
                Icon(Icons.Filled.Mic, contentDescription = "Mic", tint = Color.Gray)
            }

            // Send button
            IconButton(
                onClick = {
                    if (input.text.isNotBlank() || pendingAttachments.isNotEmpty()) {
                        val items = mutableListOf<ChatItem>()
                        if (input.text.isNotBlank()) items.add(ChatItem.Text(input.text))
                        items.addAll(pendingAttachments)

                        messages = messages + ChatMessage.GroupMessage(items, true)

                        val toSend = items.toList()
                        input = TextFieldValue("")
                        pendingAttachments = emptyList()

                        coroutineScope.launch {
                            // Start with an empty AI bubble
                            var aiText = ""
                            val aiBubbleIndex = messages.size



                            // Add placeholder bubble for streaming
                            messages = messages + ChatMessage.GroupMessage(listOf(ChatItem.Text("...")), false)

                            // Collect stream chunks
                            ChatAgent.streamMessage(context, toSend).collect { chunk ->
                                when {
                                    // 🖼️ Image output from Nano Banana
                                    chunk.startsWith("🖼️NANO_IMAGE_URI:") -> {
                                        val uri = chunk.removePrefix("🖼️NANO_IMAGE_URI:")
                                        messages = messages + ChatMessage.GroupMessage(
                                            listOf(ChatItem.Image(uri)), // show the generated annotated image
                                            isUser = false
                                        )
                                    }

                                    // 🎨 or 🖼️ status markers (Nano Banana / visual edit updates)
                                    chunk.startsWith("🎨") || chunk.startsWith("🖼️") -> {
                                        messages = messages + ChatMessage.GroupMessage(
                                            listOf(ChatItem.Text(chunk)),
                                            isUser = false
                                        )
                                    }

                                    // 🤖, ⚙️, 🌦️, 💰, 💱, etc. — status/thinking updates from other agents
                                    chunk.startsWith("🤖") ||
                                            chunk.startsWith("⚙️") ||
                                            chunk.startsWith("📍") ||
                                            chunk.startsWith("🌦️") ||
                                            chunk.startsWith("💰") ||
                                            chunk.startsWith("🌐") ||
                                            chunk.startsWith("💱") ||
                                            chunk.startsWith("✅") -> {
                                        messages = messages + ChatMessage.GroupMessage(
                                            listOf(ChatItem.Text(chunk)),
                                            isUser = false
                                        )
                                    }

                                    // 🧠 Normal Gemini streaming text chunks (markdown or plain)
                                    else -> {
                                        aiText += chunk
                                        messages = messages.toMutableList().apply {
                                            this[aiBubbleIndex] = ChatMessage.GroupMessage(
                                                listOf(ChatItem.Markdown(aiText)),
                                                isUser = false
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Black)
            ) {
                Text("▶", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}