package com.example.ui.screens.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()
    val isKeyConfigured by viewModel.isAiKeyConfigured.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(false) }
    var inputKeyText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val sampleQuestions = listOf(
        "چرا رسیور روی کلمه Boot گیر می‌کند؟",
        "روش تست ولتاژ ۱۳ و ۱۸ ولت تیونر با مولتی‌متر",
        "فرمول فاصله قیچی LNB روی دیش ۹۰ سانتی",
        "تعمیر برد پاور SMPS با مشکل خازن بادکرده",
        "تنظیم صفر موتور گردان برای سیستم USALS"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "دستیار هوشمند ماهواره‌یار",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isKeyConfigured) SignalGreen else GoldLock)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKeyConfigured) "متصل به سرویس هوش مصنوعی • آنلاین" else "حالت دانشنامه آفلاین • کلید تنظیم نشده",
                                fontSize = 11.sp,
                                color = if (isKeyConfigured) SignalCyan else GoldLock
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        inputKeyText = viewModel.getAiApiKey()
                        showKeyDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "تنظیمات کلید ارتباطی",
                            tint = if (isKeyConfigured) SignalCyan else GoldLock
                        )
                    }
                    IconButton(onClick = { viewModel.clearChatMessages() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "شروع گفتگوی جدید",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceNavyDark)
            )
        },
        containerColor = SpaceNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Suggested Questions Horizontal Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceNavySurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleQuestions) { q ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SignalCyanContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        onClick = { viewModel.sendAiMessage(q) }
                    ) {
                        Text(
                            text = q,
                            fontSize = 11.sp,
                            color = SignalCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg.text,
                        isUser = msg.isUser,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(msg.text))
                            Toast.makeText(context, "پاسخ فنی در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SignalCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دستیار در حال تحلیل و پردازش پاسخ مهندسی...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SpaceNavySurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "سوال فنی، ایراد مدار یا زاویه مورد نظر...",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalCyan,
                            unfocusedBorderColor = SpaceNavySurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = SpaceNavyDark,
                            unfocusedContainerColor = SpaceNavyDark
                        ),
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                val text = inputText.trim()
                                inputText = ""
                                viewModel.sendAiMessage(text)
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) SignalCyan else SpaceNavySurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال پیام",
                            tint = if (inputText.isNotBlank() && !isLoading) SpaceNavyDark else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Key Setup Dialog
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = {
                Text(
                    text = "تنظیمات کلید ارتباطی هوش مصنوعی",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "کلید API به صورت محلی و خارج از سورس ذخیره می‌شود. می‌توانید آن را در فایل .env یا در این بخش وارد نمایید.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputKeyText,
                        onValueChange = { inputKeyText = it },
                        label = { Text("کلید API محلی (Bearer Token)") },
                        placeholder = { Text("مثال: atr_live_...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalCyan,
                            unfocusedBorderColor = SpaceNavySurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveAiApiKey(inputKeyText.trim())
                        showKeyDialog = false
                        Toast.makeText(context, "کلید با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("ذخیره کلید", color = SignalCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false }) {
                    Text("انصراف", color = Color.Gray)
                }
            },
            containerColor = SpaceNavySurface
        )
    }
}

@Composable
private fun ChatBubble(
    message: String,
    isUser: Boolean,
    onCopy: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 4.dp else 16.dp,
                bottomEnd = if (isUser) 16.dp else 4.dp
            ),
            color = if (isUser) SignalCyanContainer else SpaceNavySurfaceVariant,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SignalCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ماهواره‌یار هوشمند",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalCyan
                            )
                        }

                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی پاسخ",
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
