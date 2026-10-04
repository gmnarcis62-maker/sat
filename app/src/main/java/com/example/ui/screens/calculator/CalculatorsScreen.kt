package com.example.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.core.util.SatelliteCalculator
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTool by remember { mutableStateOf(0) } // 0 = قیچی (Multi-LNB), 1 = افت کابل, 2 = قانون اهم و تقسیم ولتاژ

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "محاسبات مهندسی و ابزارهای نصب",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTool,
                containerColor = SpaceNavySurface,
                contentColor = SignalCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTool == 0,
                    onClick = { selectedTool = 0 },
                    text = { Text("محاسبه قیچی (Multi-LNB)", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTool == 1,
                    onClick = { selectedTool = 1 },
                    text = { Text("افت سیگنال کابل", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedTool == 2,
                    onClick = { selectedTool = 2 },
                    text = { Text("ماشین‌حساب الکترونیک", fontSize = 11.sp) }
                )
            }

            when (selectedTool) {
                0 -> MultiLnbCalculatorView()
                1 -> CableLossCalculatorView()
                2 -> ElectronicsCalculatorView()
            }
        }
    }
}

@Composable
private fun MultiLnbCalculatorView() {
    var primeSatLngText by remember { mutableStateOf("13.0") } // Hotbird
    var targetSatLngText by remember { mutableStateOf("52.5") } // Yahsat
    var dishDiameterText by remember { mutableStateOf("90") }

    val primeLng = primeSatLngText.toDoubleOrNull() ?: 13.0
    val targetLng = targetSatLngText.toDoubleOrNull() ?: 52.5
    val dishDiam = dishDiameterText.toDoubleOrNull() ?: 90.0

    val multiLnbResult = remember(primeLng, targetLng, dishDiam) {
        SatelliteCalculator.calculateMultiLnbOffset(primeLng, targetLng, dishDiam)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "محاسبه موقعیت و فاصله ال‌ان‌بی فرعی (قیچی) بر روی دیش:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            GlassCard {
                OutlinedTextField(
                    value = primeSatLngText,
                    onValueChange = { primeSatLngText = it },
                    label = { Text("موقعیت مداری LNB مرکزی (مثلاً 13 برای هاتبرد)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetSatLngText,
                    onValueChange = { targetSatLngText = it },
                    label = { Text("موقعیت مداری LNB قیچی (مثلاً 52.5 برای یاهست یا 7 برای یوتل)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dishDiameterText,
                    onValueChange = { dishDiameterText = it },
                    label = { Text("قطر کاسه دیش (سانتی‌متر، مثلاً 90)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            GlassCard(borderColor = SignalGreen.copy(alpha = 0.5f)) {
                Text(
                    text = "نتایج محاسبه نصب قیچی:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBadge(
                        label = "اختلاف مداری",
                        value = "${multiLnbResult.orbitalDifferenceDeg}°",
                        color = SignalCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "فاصله روی پایه",
                        value = "${multiLnbResult.distanceCm}",
                        unit = "سانتی‌متر",
                        color = GoldLock,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpaceNavyDark)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "• جهت قرارگیری: ${multiLnbResult.sidePosition}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "• زاویه عمودی: ${multiLnbResult.elevationGuidance}",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "• توجه مهم: هنگام ایستادن روبروی کاسه دیش، به دلیل بازتاب آینه‌ای امواج، ماهواره‌های شرقی در سمت چپ و ماهواره‌های غربی در سمت راست قرار می‌گیرند.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CableLossCalculatorView() {
    var cableType by remember { mutableStateOf("RG6") }
    var lengthText by remember { mutableStateOf("25") }
    var freqText by remember { mutableStateOf("2150") }

    val length = lengthText.toDoubleOrNull() ?: 25.0
    val freq = freqText.toDoubleOrNull() ?: 2150.0

    val totalLossDb = remember(cableType, length, freq) {
        SatelliteCalculator.calculateCableLoss(cableType, length, freq)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "محاسبه افت سیگنال کابل کواکسیال (Coaxial Attenuation):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            GlassCard {
                Text(text = "نوع کابل کواکسیال:", fontSize = 12.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("RG6", "RG11", "RG59").forEach { type ->
                        Button(
                            onClick = { cableType = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cableType == type) SignalCyan else SpaceNavyDark,
                                contentColor = if (cableType == type) SpaceNavyDark else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(type, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("طول مسیر کابل‌کشی (متر)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = freqText,
                    onValueChange = { freqText = it },
                    label = { Text("فرکانس میانی IF (مگاهرتز، پیش‌فرض 2150)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            val lossStatus = when {
                totalLossDb <= 8.0 -> "عالی (افت ناچیز و سیگنال کاملاً پایدار)"
                totalLossDb <= 15.0 -> "قابل قبول (استاندارد ساختمان‌های مسکونی)"
                else -> "هشدار: افت سیگنال شدید (نیازمند تقویت‌کننده بین‌راهی Line Amplifier یا کابل RG11)"
            }

            val statusColor = when {
                totalLossDb <= 8.0 -> SignalGreen
                totalLossDb <= 15.0 -> GoldLock
                else -> SignalRed
            }

            GlassCard(borderColor = statusColor.copy(alpha = 0.5f)) {
                Text(text = "برآورد افت توان سیگنال:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SignalCyan)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "میزان افت در کل مسیر:",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${PersianDateUtil.toPersianDigits(totalLossDb.toString())} dB-",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ارزیابی نصاب: $lossStatus",
                    fontSize = 12.sp,
                    color = statusColor,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ElectronicsCalculatorView() {
    var voltageText by remember { mutableStateOf("12") }
    var currentText by remember { mutableStateOf("2") }

    val v = voltageText.toDoubleOrNull() ?: 12.0
    val i = currentText.toDoubleOrNull() ?: 2.0
    val powerWatts = v * i
    val resistanceOhms = if (i != 0.0) v / i else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "محاسبه قانون اهم و توان مصرفی رسیور و تجهیزات:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            GlassCard {
                OutlinedTextField(
                    value = voltageText,
                    onValueChange = { voltageText = it },
                    label = { Text("ولتاژ (ولت V)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("جریان (آمپر A)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        item {
            GlassCard(borderColor = SignalCyan.copy(alpha = 0.5f)) {
                Text(text = "نتایج محاسبه:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SignalCyan)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricBadge(
                        label = "توان کل (Power)",
                        value = "${PersianDateUtil.toPersianDigits(String.format("%.1f", powerWatts))}",
                        unit = "وات (W)",
                        color = GoldLock,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        label = "مقاومت معادل بار (R)",
                        value = "${PersianDateUtil.toPersianDigits(String.format("%.1f", resistanceOhms))}",
                        unit = "اهم (Ω)",
                        color = SignalGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
