package com.example.ui.screens.repair

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.PersianDateUtil
import com.example.data.local.entity.ReceiverModelEntity
import com.example.data.local.entity.RepairCaseEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

data class TroubleshootingFlow(
    val title: String,
    val initialSymptom: String,
    val steps: List<DiagnosticNode>
)

data class DiagnosticNode(
    val stepTitle: String,
    val instruction: String,
    val testPoint: String,
    val expectedValue: String,
    val tool: String,
    val suspectPart: String,
    val solution: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairLabScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = درخت عیب‌یابی, 1 = مدل‌های رسیور, 2 = دفترچه پرونده‌ها
    val receivers by viewModel.receivers.collectAsState()
    val repairs by viewModel.repairs.collectAsState()

    var showAddRepairDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تعمیرگاه هوشمند و عیب‌یابی رسیور",
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
                    if (selectedTab == 2) {
                        IconButton(onClick = { showAddRepairDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "ثبت تعمیر جدید", tint = SignalCyan)
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceNavySurface,
                contentColor = SignalCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("درخت عیب‌یابی تعاملی", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("بانک مدل‌های رسیور", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("پرونده‌های تعمیرات", fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> InteractiveTroubleshootingView()
                1 -> ReceiverModelsView(receivers)
                2 -> RepairCasesView(repairs, onAddCase = { showAddRepairDialog = true })
            }
        }
    }

    if (showAddRepairDialog) {
        var customerName by remember { mutableStateOf("") }
        var modelName by remember { mutableStateOf("") }
        var symptom by remember { mutableStateOf("") }
        var partsReplaced by remember { mutableStateOf("") }
        var totalCostText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRepairDialog = false },
            title = { Text("ثبت پرونده تعمیر رسیور", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("نام مشتری") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("مدل رسیور (مثلاً StarSat 2000)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = symptom,
                        onValueChange = { symptom = it },
                        label = { Text("شرح ایراد اولیه (مثلاً بدون سیگنال / خاموش)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = partsReplaced,
                        onValueChange = { partsReplaced = it },
                        label = { Text("قطعات تعویض‌شده (مثلاً خازن 1000uF + آیسی تیونر)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalCostText,
                        onValueChange = { totalCostText = it },
                        label = { Text("هزینه کل (تومان)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = totalCostText.toLongOrNull() ?: 0L
                        viewModel.addRepairCase(
                            RepairCaseEntity(
                                customerName = customerName.ifBlank { "مشتری حضوری" },
                                receiverModel = modelName.ifBlank { "رسیور ناشناخته" },
                                symptom = symptom.ifBlank { "تعمیر عمومی" },
                                diagnosis = "بررسی برد و ولتاژگیری",
                                partsReplaced = partsReplaced,
                                laborCost = (cost * 0.6).toLong(),
                                partsCost = (cost * 0.4).toLong(),
                                totalCost = cost,
                                status = "تکمیل‌شده",
                                dateJalali = viewModel.currentDatePersian.format()
                            )
                        )
                        showAddRepairDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("ثبت پرونده")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRepairDialog = false }) {
                    Text("انصراف", color = Color.Gray)
                }
            },
            containerColor = SpaceNavySurface
        )
    }
}

@Composable
private fun InteractiveTroubleshootingView() {
    val flows = remember {
        listOf(
            TroubleshootingFlow(
                title = "دستگاه کاملاً خاموش است (بدون هیچ چراغی)",
                initialSymptom = "دستگاه با زدن به برق هیچ واکنشی نشان نمی‌دهد و حتی چراغ پاور خاموش است.",
                steps = listOf(
                    DiagnosticNode(
                        stepTitle = "مرحله ۱: تست ولتاژ ورودی تغذیه",
                        instruction = "با مولتی‌متر در حالت ولتاژ DC، خروجی آداپتور یا خروجی ثانویه پاور را تست کنید.",
                        testPoint = "سوکت ورودی 12V آداپتور یا خروجی 5V/12V سوئیچینگ",
                        expectedValue = "12.0V ولت با تلورانس 0.5V",
                        tool = "مولتی‌متر دیجیتال (تست ولتاژ DC)",
                        suspectPart = "آداپتور، فیوز ورودی 2A، ترانسفورماتور، کابل برق",
                        solution = "در صورت صفر بودن، آداپتور یا فیوز برد تغذیه را تعویض نمایید."
                    ),
                    DiagnosticNode(
                        stepTitle = "مرحله ۲: تست خازن‌های صافی خروجی SMPS",
                        instruction = "خازن‌های خروجی تغذیه را بررسی کنید. وجود بادکردگی یا مقاومت نشتی بالا مانع روشن شدن می‌شود.",
                        testPoint = "خازن‌های الکترولیتی 1000uF / 16V و 470uF / 25V",
                        expectedValue = "ظرفیت کامل نامی و ESR کمتر از 0.2 اهم",
                        tool = "خازن‌سنج / ESR متر",
                        suspectPart = "خازن‌های 1000uF 16V و 1000uF 10V",
                        solution = "خازن‌های آسیب‌دیده را با مدل باکیفیت 105 درجه سانتی‌گراد Low-ESR جایگزین کنید."
                    ),
                    DiagnosticNode(
                        stepTitle = "مرحله ۳: تست رگولاتورهای تبدیل DC-DC برد اصلی",
                        instruction = "ولتاژ خروجی رگولاتورهای 3.3V (تغذیه فلش و کریستال) و 1.1V (هسته پردازنده) را چک کنید.",
                        testPoint = "پین خروجی آیسی‌های ۵ پایه DC-DC (کد S10, S15)",
                        expectedValue = "3.3V برای پین 8 آیسی فلش و 1.15V برای سلف هسته CPU",
                        tool = "مولتی‌متر با پراب نوک‌تیز",
                        suspectPart = "آیسی سوئیچینگ DC-DC SMD یا اتصال کوتاه در پردازنده",
                        solution = "در صورت افت ولتاژ به صفر و داغی آیسی، رگولاتور را تعویض کنید."
                    )
                )
            ),
            TroubleshootingFlow(
                title = "دستگاه چراغ قرمز/سبز مانده و بوت نمی‌شود (Boot Loop)",
                initialSymptom = "چراغ پاور روشن است یا کلمه Boot روی پنل می‌ماند و تصویر بالا نمی‌آید.",
                steps = listOf(
                    DiagnosticNode(
                        stepTitle = "مرحله ۱: بررسی ولتاژهای اصلی برد",
                        instruction = "ولتاژ پایه ۸ آیسی فلش SPI (3.3V) و ولتاژ رم DDR (1.5V یا 1.8V) را اندازه بگیرید.",
                        testPoint = "پایه ۸ آیسی هشت پایه SPI Flash (25Q64 / 25Q128)",
                        expectedValue = "3.3V ثابت بدون نوسان",
                        tool = "مولتی‌متر دیجیتال",
                        suspectPart = "رگولاتور 3.3 ولت یا خازن‌های SMD اطراف فلش",
                        solution = "در صورت نوسان، خازن 100uF کنار رگولاتور را تعویض کنید."
                    ),
                    DiagnosticNode(
                        stepTitle = "مرحله ۲: پروگرام نرم‌افزار فلش",
                        instruction = "خرابی دیتای فلش رایج‌ترین عامل هنگ روی بوت است. آیسی فلش باید با فایل دامپ سالم پروگرام شود.",
                        testPoint = "پین‌های SPI: CS, CLK, MOSI, MISO",
                        expectedValue = "دیتای سالم هماهنگ با سریال چیپ",
                        tool = "پروگرامر RT809F یا CH341A",
                        suspectPart = "داده‌های نرم‌افزاری آسیب‌دیده فلش",
                        solution = "با پروگرامر فایل فلش اورجینال را روی آیسی رایت کرده و دوباره نصب نمایید."
                    )
                )
            ),
            TroubleshootingFlow(
                title = "عدم دریافت سیگنال یا سیگنال صفر در تمام فرکانس‌ها",
                initialSymptom = "دیش تنظیم است اما رسیور قدرت و کیفیت سیگنال را صفر درصد نشان می‌دهد.",
                steps = listOf(
                    DiagnosticNode(
                        stepTitle = "مرحله ۱: اندازه‌گیری ولتاژ خروجی پورت تیونر به دیش",
                        instruction = "یک سر پراب منفی به بدنه و پراب مثبت داخل سوراخ مغزی فیش ورودی تیونر قرار دهید.",
                        testPoint = "مغزی پورت تیونر F-Type (LNB IN)",
                        expectedValue = "در فرکانس‌های V باید 13V الی 14V و در H باید 18V الی 19V باشد.",
                        tool = "مولتی‌متر دیجیتال در حالت ولتاژ DC",
                        suspectPart = "آیسی سوئیچینگ ولتاژ تیونر (کد S8116, A8293, LM317)",
                        solution = "اگر ولتاژ صفر است، آیسی تغذیه تیونر سوخته و باید تعویض شود."
                    ),
                    DiagnosticNode(
                        stepTitle = "مرحله ۲: تست مقاومت‌های فیوزی مسیر 12V به تیونر",
                        instruction = "مقاومت‌های صفر اهم یا کم‌اهم فیوزی که ولتاژ 12V را به آیسی تیونر می‌رسانند بررسی کنید.",
                        testPoint = "مقاومت فیوزی SMD مسیر تغذیه تیونر",
                        expectedValue = "کمتر از 1 اهم (تست بوق پیوستگی)",
                        tool = "تست بیزر مولتی‌متر",
                        suspectPart = "مقاومت فیوزی سوخته بر اثر اتصالی در کابل یا دایسک",
                        solution = "پس از رفع اتصالی کابل، مقاومت را جایگزین نمایید."
                    )
                )
            )
        )
    }

    var selectedFlowIndex by remember { mutableStateOf(0) }
    val currentFlow = flows[selectedFlowIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Text(
                text = "انتخاب ایراد فنی دستگاه:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(flows.indices.toList()) { index ->
            val flow = flows[index]
            val isSelected = selectedFlowIndex == index
            GlassCard(
                modifier = Modifier.clickable { selectedFlowIndex = index },
                borderColor = if (isSelected) SignalCyan else SpaceNavySurfaceVariant,
                backgroundColor = if (isSelected) SignalCyanContainer.copy(alpha = 0.4f) else SpaceNavySurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SignalCyan else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = flow.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SignalCyan else Color.White
                    )
                }
            }
        }

        item {
            Divider(color = SpaceNavySurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "مراحل تست و عیب‌یابی درختی:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SignalGreen
            )
        }

        items(currentFlow.steps) { step ->
            GlassCard(borderColor = SignalGreen.copy(alpha = 0.3f)) {
                Text(
                    text = step.stepTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLock
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = step.instruction,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpaceNavyDark)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "📍 نقطه تست: ${step.testPoint}", fontSize = 11.sp, color = SignalCyan)
                    Text(text = "📊 مقدار نرمال مورد انتظار: ${step.expectedValue}", fontSize = 11.sp, color = SignalGreen)
                    Text(text = "🛠️ ابزار سنجش: ${step.tool}", fontSize = 11.sp, color = Color.LightGray)
                    Text(text = "🔍 قطعه مشکوک: ${step.suspectPart}", fontSize = 11.sp, color = Color(0xFFFF8A65))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 راهکار تعمیر: ${step.solution}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SignalGreen
                )
            }
        }
    }
}

@Composable
private fun ReceiverModelsView(receivers: List<ReceiverModelEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(receivers) { rec ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${rec.brand} ${rec.modelName}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "پردازنده: ${rec.cpuModel} • تیونر: ${rec.tunerType}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldLock.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = rec.brand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLock,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "مشخصات منبع تغذیه: ${rec.powerSupplySpecs}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "خرابی‌های شایع گزارش‌شده: ${rec.commonIssues}",
                    fontSize = 11.sp,
                    color = Color(0xFFFF8A65),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun RepairCasesView(
    repairs: List<RepairCaseEntity>,
    onAddCase: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "پرونده‌های ثبت‌شده تعمیرگاه (${PersianDateUtil.toPersianDigits(repairs.size.toString())} مورد):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onAddCase,
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت دستگاه جدید", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (repairs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هنوز پرونده تعمیری ثبت نشده است.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }

        items(repairs) { rep ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${rep.receiverModel} (${rep.customerName})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ایراد اولیه: ${rep.symptom}",
                            fontSize = 11.sp,
                            color = Color(0xFFFFB74D)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SignalGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = rep.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "قطعات تعویضی: ${rep.partsReplaced}",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "تاریخ: ${PersianDateUtil.toPersianDigits(rep.dateJalali)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "هزینه کل: ${PersianDateUtil.toPersianDigits(String.format("%,d", rep.totalCost))} تومان",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLock
                    )
                }
            }
        }
    }
}
