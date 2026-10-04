package com.example.ui.screens.frequency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.data.local.entity.SatelliteEntity
import com.example.data.local.entity.TransponderEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyBankScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val satellites by viewModel.satellites.collectAsState()
    var selectedSatellite by remember { mutableStateOf<SatelliteEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // When satellites load, select first if none selected
    LaunchedEffect(satellites) {
        if (selectedSatellite == null && satellites.isNotEmpty()) {
            selectedSatellite = satellites.first()
        }
    }

    val currentSat = selectedSatellite
    val transpondersFlow = remember(currentSat) {
        if (currentSat != null) {
            viewModel.repository.getTranspondersForSatellite(currentSat.id)
        } else {
            viewModel.repository.satelliteDao.getAllTransponders()
        }
    }
    val transponders by transpondersFlow.collectAsState(initial = emptyList())

    val filteredTransponders = remember(transponders, searchQuery) {
        if (searchQuery.isBlank()) {
            transponders
        } else {
            transponders.filter {
                it.frequencyMHz.toString().contains(searchQuery) ||
                        it.channels.contains(searchQuery, ignoreCase = true) ||
                        it.polarization.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "بانک ماهواره‌ها و فرکانس‌ها",
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن فرکانس",
                            tint = SignalCyan
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجوی فرکانس یا کانال...", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SignalCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SignalCyan,
                    unfocusedBorderColor = SpaceNavySurfaceVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = SpaceNavySurface,
                    unfocusedContainerColor = SpaceNavySurface
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Satellite Horizontal Tabs / Selector
            Text(
                text = "ماهواره انتخابی:",
                fontSize = 12.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            ScrollableTabRow(
                selectedTabIndex = satellites.indexOfFirst { it.id == selectedSatellite?.id }.coerceAtLeast(0),
                containerColor = SpaceNavySurface,
                contentColor = SignalCyan,
                edgePadding = 0.dp,
                divider = {}
            ) {
                satellites.forEach { sat ->
                    Tab(
                        selected = selectedSatellite?.id == sat.id,
                        onClick = { selectedSatellite = sat },
                        text = {
                            Text(
                                text = "${sat.name.split(" ")[0]} (${sat.orbitalPositionDeg}°E)",
                                fontSize = 12.sp,
                                fontWeight = if (selectedSatellite?.id == sat.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Satellite Specs Summary Card
            selectedSatellite?.let { sat ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sat.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "اپراتور: ${sat.operator} • پوشش: ${sat.coverage}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                        MetricBadge(
                            label = "موقعیت",
                            value = "${sat.orbitalPositionDeg}°",
                            color = GoldLock
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "فهرست ترانسپوندرها (${PersianDateUtil.toPersianDigits(filteredTransponders.size.toString())} فرکانس فعال):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Transponders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                if (filteredTransponders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "ترانسپوندری یافت نشد.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }

                items(filteredTransponders) { tp ->
                    GlassCard(
                        borderColor = if (tp.isStrong) SignalGreen.copy(alpha = 0.4f) else SpaceNavySurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (tp.polarization == "V") SignalCyanContainer else Color(0xFF3E2723),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tp.polarization,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (tp.polarization == "V") SignalCyan else Color(0xFFFFB74D)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = PersianDateUtil.toPersianDigits(tp.frequencyMHz.toString()),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${PersianDateUtil.toPersianDigits(tp.symbolRate.toString())} • ${tp.fec}",
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tp.channels,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                            }

                            if (tp.isStrong) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SignalGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "فرکانس مادر",
                                        fontSize = 10.sp,
                                        color = SignalGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Transponder Dialog
    if (showAddDialog) {
        var freqText by remember { mutableStateOf("") }
        var srText by remember { mutableStateOf("27500") }
        var polText by remember { mutableStateOf("V") }
        var fecText by remember { mutableStateOf("3/4") }
        var channelsText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("افزودن فرکانس جدید", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = freqText,
                        onValueChange = { freqText = it },
                        label = { Text("فرکانس (MHz)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = srText,
                        onValueChange = { srText = it },
                        label = { Text("سیمبل ریت (Symbol Rate)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { polText = "V" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (polText == "V") SignalCyan else SpaceNavySurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("عمودی (V)")
                        }
                        Button(
                            onClick = { polText = "H" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (polText == "H") SignalCyan else SpaceNavySurface
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("افقی (H)")
                        }
                    }
                    OutlinedTextField(
                        value = channelsText,
                        onValueChange = { channelsText = it },
                        label = { Text("نام کانال‌ها یا پکیج") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val freq = freqText.toIntOrNull() ?: 11000
                        val sr = srText.toIntOrNull() ?: 27500
                        val satId = selectedSatellite?.id ?: 1L
                        coroutineScope.launch {
                            viewModel.repository.addTransponder(
                                TransponderEntity(
                                    satelliteId = satId,
                                    frequencyMHz = freq,
                                    polarization = polText,
                                    symbolRate = sr,
                                    fec = fecText,
                                    standard = "DVB-S2",
                                    channels = channelsText.ifBlank { "کانال‌های عمومی" },
                                    isStrong = false
                                )
                            )
                        }
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف", color = Color.Gray)
                }
            },
            containerColor = SpaceNavySurface
        )
    }
}
