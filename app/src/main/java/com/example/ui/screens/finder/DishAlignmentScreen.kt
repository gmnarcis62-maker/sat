package com.example.ui.screens.finder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entity.SatelliteEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CityPreset
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishAlignmentScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val satellites by viewModel.satellites.collectAsState()
    val selectedSat by viewModel.selectedSatellite.collectAsState()
    val alignment by viewModel.alignmentResult.collectAsState()
    val currentAzimuth by viewModel.azimuth.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val roll by viewModel.roll.collectAsState()
    val azimuthDiff by viewModel.azimuthDifference.collectAsState()
    val city by viewModel.selectedCity.collectAsState()
    val cities = viewModel.cities

    var showCityDialog by remember { mutableStateOf(false) }
    val isAligned = abs(azimuthDiff) <= 2.5f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ماهواره‌یاب و تنظیم زاویه دیش",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, // RTL back arrow
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showCityDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SignalCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = city.name, color = SignalCyan, fontSize = 12.sp)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Satellite Selection Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "انتخاب ماهواره هدف:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(satellites) { sat ->
                            val isSelected = selectedSat?.id == sat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectSatellite(sat) },
                                label = {
                                    Text(
                                        text = "${sat.name.split(" ")[0]} (${sat.orbitalPositionDeg}°E)",
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SignalCyan,
                                    selectedLabelColor = SpaceNavyDark,
                                    containerColor = SpaceNavySurface,
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }

            // Interactive Compass
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InteractiveCompassCanvas(
                        currentAzimuth = currentAzimuth,
                        targetAzimuth = alignment.magneticAzimuthDeg.toFloat(),
                        isTargetAligned = isAligned
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Turn direction guidance
                    val directionNotice = when {
                        isAligned -> "✓ دیش دقیقاً در راستای ماهواره قرار دارد!"
                        azimuthDiff > 0 -> "⟳ به اندازه ${PersianDateUtil.toPersianDigits(String.format("%.1f", abs(azimuthDiff)))}° به سمت راست بچرخانید"
                        else -> "⟲ به اندازه ${PersianDateUtil.toPersianDigits(String.format("%.1f", abs(azimuthDiff)))}° به سمت چپ بچرخانید"
                    }

                    Text(
                        text = directionNotice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAligned) SignalGreen else GoldLock,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calculation Metrics Grid
            item {
                GlassCard {
                    Text(
                        text = "مشخصات نجومی محاسبه‌شده برای ${selectedSat?.name ?: "ماهواره"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "آزیموت جغرافیایی",
                            value = "${alignment.trueAzimuthDeg}°",
                            color = SignalCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "آزیموت مغناطیسی",
                            value = "${alignment.magneticAzimuthDeg}°",
                            color = GoldLock,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "زاویه ارتفاع (Elevation)",
                            value = "${alignment.elevationDeg}°",
                            color = SignalGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBadge(
                            label = "چرخش LNB (Skew)",
                            value = "${alignment.lnbSkewDeg}°",
                            color = Color(0xFFFF8A65),
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            label = "فاصله تا مدار",
                            value = "${alignment.distanceKm.toInt()}",
                            unit = "کیلومتر",
                            color = Color(0xFFBA68C8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Elevation Inclinometer & LNB Skew Clock Guide
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تراز و شیب‌سنج ارتفاع",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DigitalBubbleLevel(
                                pitchDeg = pitch,
                                rollDeg = roll,
                                targetElevationDeg = alignment.elevationDeg.toFloat()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "شیب فعلی: ${PersianDateUtil.toPersianDigits(String.format("%.1f", pitch))}° (هدف: ${alignment.elevationDeg}°)",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "راهنمای ساعت LNB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val clockPosition = when {
                            alignment.lnbSkewDeg > 20 -> "ساعت ۵:۰۰ (به چپ)"
                            alignment.lnbSkewDeg > 5 -> "ساعت ۵:۳۰"
                            alignment.lnbSkewDeg > -5 -> "ساعت ۶:۰۰ (عمود)"
                            alignment.lnbSkewDeg > -20 -> "ساعت ۶:۳۰"
                            else -> "ساعت ۷:۰۰ (به راست)"
                        }
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(SpaceNavyDark)
                                .border(2.dp, GoldLock, CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = clockPosition.split(" ")[0] + " " + clockPosition.split(" ")[1],
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLock,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "خروجی کابل LNB را روبروی دیش روی $clockPosition تنظیم کنید.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Mandatory Real-World Disclaimer
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2E260A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldLock.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GoldLock,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "توجه: این ابزار زاویه جهت‌یابی هندسی دیش را محاسبه می‌کند. گوشی فرکانس‌های رادیویی ماهواره را دریافت نمی‌کند. برای اندازه‌گیری سطح سیگنال، از منوی سیگنال‌متر به دستگاه دیجیتال متصل شوید.",
                            fontSize = 11.sp,
                            color = Color(0xFFFFE082),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }

    // City Selection Dialog
    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "انتخاب شهر محل نصب",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalCyan
                )
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(cities) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCity(c)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = c.name, color = Color.White, fontSize = 14.sp)
                            Text(
                                text = "${PersianDateUtil.toPersianDigits(c.lat.toString())}, ${PersianDateUtil.toPersianDigits(c.lng.toString())}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Divider(color = SpaceNavySurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text(text = "بستن", color = SignalCyan)
                }
            },
            containerColor = SpaceNavySurface
        )
    }
}
