package com.example.ui.screens.signal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.data.hardware.ConnectionStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBadge
import com.example.ui.components.SignalLevelBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalMeterScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = جهت‌یابی با گوشی, 1 = دستگاه سیگنال‌متر
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val liveSignal by viewModel.liveSignal.collectAsState()
    val connectedDevice by viewModel.connectedDeviceName.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isMuted by viewModel.isAudioMuted.collectAsState()
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    val alignment by viewModel.alignmentResult.collectAsState()
    val azimuthDiff by viewModel.azimuthDifference.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سیگنال‌متر حرفه‌ای (دو حالته)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (selectedTab == 1 && connectionStatus == ConnectionStatus.CONNECTED) {
                        IconButton(onClick = { viewModel.toggleAudioMute() }) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "بوق سیگنال",
                                tint = if (isMuted) Color.Gray else SignalGreen
                            )
                        }
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
            // Mode Selector Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceNavySurface,
                contentColor = SignalCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "۱. جهت‌یابی با گوشی (آفلاین)",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "۲. سیگنال‌متر سخت‌افزاری",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Mode 1: Phone Sensor Guidance (Without Hardware)
                PhoneOrientationGuidanceView(
                    selectedSatName = selectedSat?.name ?: "یاهست",
                    azimuthDiff = azimuthDiff,
                    targetAzimuth = alignment.magneticAzimuthDeg,
                    elevation = alignment.elevationDeg,
                    skew = alignment.lnbSkewDeg
                )
            } else {
                // Mode 2: Hardware Signal Meter Telemetry View
                HardwareSignalMeterView(
                    viewModel = viewModel,
                    connectionStatus = connectionStatus,
                    connectedDevice = connectedDevice,
                    discoveredDevices = discoveredDevices,
                    liveSignal = liveSignal,
                    isMuted = isMuted
                )
            }
        }
    }
}

@Composable
private fun PhoneOrientationGuidanceView(
    selectedSatName: String,
    azimuthDiff: Float,
    targetAzimuth: Double,
    elevation: Double,
    skew: Double
) {
    val isClose = kotlin.math.abs(azimuthDiff) <= 3.0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Warning Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E2207),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLock.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = GoldLock,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "در این حالت سیگنال واقعی رادیویی سنجیده نمی‌شود؛ زیرا گوشی فاقد تیونر DVB-S2 است. این بخش راهنمای گام‌به‌گام چرخش دیش بر اساس سنسور ژیروسکوپ و قطب‌نما می‌باشد.",
                        fontSize = 11.sp,
                        color = Color(0xFFFFD54F),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Live Alignment Compass Tracker
        item {
            GlassCard(
                borderColor = if (isClose) SignalGreen.copy(alpha = 0.6f) else SignalCyan.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "وضعیت انحراف از ماهواره $selectedSatName",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalCyan
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isClose) "وضعیت: هم‌راستا با ماهواره" else "وضعیت: نیازمند چرخش",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClose) SignalGreen else GoldLock
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (azimuthDiff > 0) "به سمت راست بچرخانید" else "به سمت چپ بچرخانید",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isClose) SignalGreen.copy(alpha = 0.2f) else OrangeFlame.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${PersianDateUtil.toPersianDigits(String.format("%.1f", kotlin.math.abs(azimuthDiff)))}°",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isClose) SignalGreen else OrangeFlame
                        )
                    }
                }
            }
        }

        // Target Settings Cards
        item {
            Text(
                text = "تنظیمات فیزیکی دیش:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricBadge(
                    label = "آزیموت قطب‌نما",
                    value = "${targetAzimuth}°",
                    color = SignalCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "زاویه شیب (ارتفاع)",
                    value = "${elevation}°",
                    color = SignalGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricBadge(
                    label = "چرخش LNB",
                    value = "${skew}°",
                    color = GoldLock,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Step by Step Installation Guide
        item {
            GlassCard {
                Text(
                    text = "مراحل گام‌به‌گام تنظیم سریع:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                StepItem(number = "۱", text = "ابتدا پایه دکل دیش را با تراز کاملاً ۹۰ درجه عمود بر زمین محکم کنید.")
                StepItem(number = "۲", text = "زاویه خط‌کش پشت دیش را دقیقاً روی عدد ${PersianDateUtil.toPersianDigits(elevation.toString())}° تنظیم و پیچ‌ها را نیمه‌محکم کنید.")
                StepItem(number = "۳", text = "ساعت LNB را روی ${PersianDateUtil.toPersianDigits(skew.toString())}° تنظیم کنید.")
                StepItem(number = "۴", text = "دیش را بسیار آهسته در راستای آزیموت ${PersianDateUtil.toPersianDigits(targetAzimuth.toString())}° حرکت دهید تا خط سبز تراز فعال شود.")
            }
        }
    }
}

@Composable
private fun StepItem(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(SignalCyanContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = PersianDateUtil.toPersianDigits(number),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SignalCyan
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.LightGray,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun HardwareSignalMeterView(
    viewModel: MainViewModel,
    connectionStatus: ConnectionStatus,
    connectedDevice: String?,
    discoveredDevices: List<com.example.data.hardware.DiscoveredDevice>,
    liveSignal: com.example.data.hardware.SignalData,
    isMuted: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Connection Bar
        item {
            GlassCard(
                borderColor = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> SignalGreen.copy(alpha = 0.5f)
                    ConnectionStatus.CONNECTING, ConnectionStatus.SCANNING -> GoldLock.copy(alpha = 0.5f)
                    else -> SpaceNavySurfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (connectionStatus) {
                                        ConnectionStatus.CONNECTED -> SignalGreen
                                        ConnectionStatus.CONNECTING, ConnectionStatus.SCANNING -> GoldLock
                                        else -> Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "متصل به: ${connectedDevice ?: "دستگاه سیگنال‌متر"}"
                                    ConnectionStatus.CONNECTING -> "در حال اتصال به دستگاه..."
                                    ConnectionStatus.SCANNING -> "در حال جستجوی بلوتوث / وای‌فای..."
                                    else -> "عدم اتصال به سخت‌افزار"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (connectionStatus == ConnectionStatus.CONNECTED) "پروتکل DVB-S2 فعال • تله‌متری زنده" else "Bluetooth BLE / Wi-Fi / USB OTG",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (connectionStatus == ConnectionStatus.CONNECTED) {
                        OutlinedButton(
                            onClick = { viewModel.disconnectHardwareDevice() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalRed)
                        ) {
                            Text("قطع", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startHardwareScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                        ) {
                            Text("اسکن دستگاه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Discovered Devices list (if scanning or disconnected)
        if (connectionStatus != ConnectionStatus.CONNECTED && discoveredDevices.isNotEmpty()) {
            item {
                Text(
                    text = "دستگاه‌های یافت‌شده سازگار:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(discoveredDevices) { dev ->
                GlassCard(
                    modifier = Modifier.clickable { viewModel.connectHardwareDevice(dev.id) },
                    borderColor = SignalCyan.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = dev.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "${dev.connectionType} • ${dev.id}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { viewModel.connectHardwareDevice(dev.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalCyanContainer, contentColor = SignalCyan)
                        ) {
                            Text("اتصال", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live RF Telemetry Gauges (Active when connected)
        if (connectionStatus == ConnectionStatus.CONNECTED) {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سنجش دیجیتال ترانسپوندر ${PersianDateUtil.toPersianDigits(liveSignal.frequencyMHz.toString())} ${liveSignal.polarization}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalCyan
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (liveSignal.isLocked) SignalGreen.copy(alpha = 0.2f) else SignalRed.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (liveSignal.isLocked) "🔒 قفل روی فرکانس (LOCKED)" else "در حال جستجو (SEARCHING)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (liveSignal.isLocked) SignalGreen else SignalRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SignalLevelBar(
                        title = "کیفیت سیگنال (Signal Quality)",
                        percentage = liveSignal.qualityPercent,
                        color = if (liveSignal.qualityPercent > 65) SignalGreen else GoldLock
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SignalLevelBar(
                        title = "قدرت سیگنال (Signal Strength)",
                        percentage = liveSignal.strengthPercent,
                        color = SignalCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "نسبت SNR",
                            value = "${liveSignal.snrDb}",
                            unit = "dB",
                            color = SignalGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "شاخص MER",
                            value = "${liveSignal.merDb}",
                            unit = "dB",
                            color = SignalCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "نرخ خطا BER",
                            value = liveSignal.berRate,
                            color = GoldLock,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "ولتاژ LNB",
                            value = "${liveSignal.lnbVoltageVolts}V",
                            color = Color(0xFFFF8A65),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
