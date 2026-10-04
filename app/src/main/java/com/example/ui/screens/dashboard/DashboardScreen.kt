package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToFinder: () -> Unit,
    onNavigateToSignal: () -> Unit,
    onNavigateToFrequency: () -> Unit,
    onNavigateToRepair: () -> Unit,
    onNavigateToCalculators: () -> Unit,
    onNavigateToBusiness: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    val alignment by viewModel.alignmentResult.collectAsState()
    val city by viewModel.selectedCity.collectAsState()
    val missions by viewModel.missions.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val hasSensors by viewModel.hasSensors.collectAsState()

    val pendingMissionsCount = missions.count { it.status != "تکمیل‌شده" && it.status != "لغوشده" }
    val lowStockItems = inventory.filter { it.quantity <= it.minStockThreshold }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceNavy)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Persian Date & Location status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ماهواره‌یار | Satellite Pro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalCyan
                    )
                    Text(
                        text = viewModel.currentDatePersian.toFullPersianString(),
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SpaceNavySurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "تنظیمات",
                        tint = SignalCyan
                    )
                }
            }
        }

        // Status Card: GPS & Sensor Banner
        item {
            GlassCard(
                borderColor = if (hasSensors) SignalCyan.copy(alpha = 0.3f) else SignalRed.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (hasSensors) SignalGreen else SignalRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "موقعیت: ${city.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "مختصات: ${PersianDateUtil.toPersianDigits(String.format("%.2f", city.lat))}°N, ${PersianDateUtil.toPersianDigits(String.format("%.2f", city.lng))}°E",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBadge(
                        label = "ماهواره فعال",
                        value = selectedSat?.orbitalPositionDeg?.let { "${it}°E" } ?: "۵۲.۵°E",
                        color = GoldLock,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MetricBadge(
                        label = "آزیموت هدف",
                        value = "${alignment.trueAzimuthDeg}°",
                        color = SignalCyan,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MetricBadge(
                        label = "زاویه ارتفاع",
                        value = "${alignment.elevationDeg}°",
                        color = SignalGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Launch Grid of Core Modules
        item {
            Text(
                text = "ابزارهای تخصصی نصاب و تعمیرکار",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolItem(
                        title = "ماهواره‌یاب و قطب‌نما",
                        subtitle = "تنظیم دیش، زاویه و اسکیو",
                        icon = Icons.Default.Explore,
                        accentColor = SignalCyan,
                        onClick = onNavigateToFinder,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolItem(
                        title = "سیگنال‌متر دیجیتال",
                        subtitle = "سخت‌افزار و راهنمای سنسور",
                        icon = Icons.Default.Sensors,
                        accentColor = SignalGreen,
                        onClick = onNavigateToSignal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolItem(
                        title = "بانک فرکانس‌ها",
                        subtitle = "ترانسپوندرها و کانال‌ها",
                        icon = Icons.Default.List,
                        accentColor = GoldLock,
                        onClick = onNavigateToFrequency,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolItem(
                        title = "تعمیرگاه رسیور",
                        subtitle = "عیب‌یابی برد، پاور و بوت",
                        icon = Icons.Default.Build,
                        accentColor = Color(0xFFFF7043),
                        onClick = onNavigateToRepair,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolItem(
                        title = "محاسبات فنی دیش",
                        subtitle = "قیچی، افت کابل، دایسک",
                        icon = Icons.Default.Calculate,
                        accentColor = Color(0xFFAB47BC),
                        onClick = onNavigateToCalculators,
                        modifier = Modifier.weight(1f)
                    )
                    QuickToolItem(
                        title = "مشتریان و مأموریت‌ها",
                        subtitle = "نصب، فاکتور و انبار قطعات",
                        icon = Icons.Default.Assignment,
                        accentColor = Color(0xFF29B6F6),
                        onClick = onNavigateToBusiness,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Assistant AI Card
        item {
            GlassCard(
                borderColor = Color(0xFF8A2BE2).copy(alpha = 0.4f),
                backgroundColor = Color(0xFF1E1035).copy(alpha = 0.85f),
                modifier = Modifier.clickable { onNavigateToAi() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF8A2BE2), SignalCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "هوش مصنوعی",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "دستیار هوش مصنوعی ماهواره‌یار",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "پاسخ به سوالات فنی، تحلیل عکس برد رسیور",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "باز کردن",
                        tint = SignalCyan
                    )
                }
            }
        }

        // Mission & Stock Alerts
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (pendingMissionsCount > 0) OrangeFlame.copy(alpha = 0.5f) else SignalCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "مأموریت‌های امروز",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${PersianDateUtil.toPersianDigits(pendingMissionsCount.toString())} مورد فعال",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingMissionsCount > 0) OrangeFlame else SignalGreen
                    )
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (lowStockItems.isNotEmpty()) SignalRed.copy(alpha = 0.5f) else SignalCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "هشدار انبار قطعات",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lowStockItems.isNotEmpty()) "${PersianDateUtil.toPersianDigits(lowStockItems.size.toString())} قلم کسری" else "موجودی کافی",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStockItems.isNotEmpty()) SignalRed else SignalGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickToolItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable { onClick() },
        borderColor = accentColor.copy(alpha = 0.3f),
        backgroundColor = SpaceNavySurface
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = Color.Gray,
            maxLines = 1
        )
    }
}
