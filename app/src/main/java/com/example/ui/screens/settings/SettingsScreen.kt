package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val satellites by viewModel.satellites.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val isAudioMuted by viewModel.isAudioMuted.collectAsState()
    val currentCity by viewModel.selectedCity.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تنظیمات و اطلاعات برنامه",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpaceNavyDark)
            )
        },
        containerColor = SpaceNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // App Branding & Version
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(SignalCyanContainer, shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛰️", fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ماهواره‌یار | Satellite Pro",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalCyan
                            )
                            Text(
                                text = "نسخه ۱.۰.۰ • دستیار جامع نصابان و تعمیرکاران رسیور",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Audio & Feedback Settings
            item {
                Text(
                    text = "تنظیمات بازخورد صوتی و سنسورها:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "صدای بیپ سیگنال‌متر (Tone Generator)", fontSize = 13.sp, color = Color.White)
                            Text(text = "افزایش فرکانس بوق هنگام قفل روی فرکانس", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = !isAudioMuted,
                            onCheckedChange = { viewModel.toggleAudioMute() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SpaceNavyDark,
                                checkedTrackColor = SignalGreen,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = SpaceNavyDark
                            )
                        )
                    }
                }
            }

            // Database Statistics
            item {
                Text(
                    text = "وضعیت پایگاه داده محلی (Offline SQLite Room):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                GlassCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatRow(title = "تعداد ماهواره‌های ذخیره‌شده", value = satellites.size.toString())
                        StatRow(title = "تعداد مشتریان در دفترچه", value = customers.size.toString())
                        StatRow(title = "تعداد مأموریت‌های ثبت‌شده", value = missions.size.toString())
                        StatRow(title = "تعداد اقلام انبار تجهیزات", value = inventory.size.toString())
                        StatRow(title = "موقعیت جغرافیایی پیش‌فرض", value = currentCity.name)
                    }
                }
            }

            // Safety Guidelines for Rooftop & High Voltage
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2B1D0C),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldLock.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = GoldLock, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "راهنمای ایمنی کار در ارتفاع و تعمیرات برق:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLock
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "۱. هنگام نصب دیش روی پشت‌بام و لبه ارتفاع، همیشه از کمربند ایمنی و کفش مناسب استفاده کنید.\n" +
                                    "۲. در شرایط وزش باد شدید یا باران از بالا رفتن از نردبان خودداری نمایید.\n" +
                                    "۳. خازن بزرگ ۴۰۰ ولت در مدار اولیه پاور رسیورها تا دقایقی پس از کشیدن دوشاخه برق شارژ است؛ همواره قبل از لمس با مقاومت تخلیه شود.\n" +
                                    "۴. قبل از بستن فیش‌های F کواکسیال، رسیور را خاموش کنید تا اتصال کوتاه ولتاژ ۱۳/۱۸ ولت تیونر رخ ندهد.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFECB3),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 12.sp, color = Color.LightGray)
        Text(
            text = PersianDateUtil.toPersianDigits(value),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SignalCyan
        )
    }
}
