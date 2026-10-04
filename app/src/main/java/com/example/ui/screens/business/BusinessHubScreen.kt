package com.example.ui.screens.business

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
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.MissionEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHubScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = مأموریت‌ها, 1 = مشتریان, 2 = انبار, 3 = فاکتورها

    val missions by viewModel.missions.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val invoices by viewModel.invoices.collectAsState()

    var showAddMissionDialog by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var showAddInvoiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مدیریت نصابی، مشتریان و انبار",
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
                actions = {
                    IconButton(
                        onClick = {
                            when (selectedTab) {
                                0 -> showAddMissionDialog = true
                                1 -> showAddCustomerDialog = true
                                2 -> showAddInventoryDialog = true
                                3 -> showAddInvoiceDialog = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزودن", tint = SignalCyan)
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceNavySurface,
                contentColor = SignalCyan,
                edgePadding = 8.dp,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("مأموریت‌ها (${PersianDateUtil.toPersianDigits(missions.size.toString())})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("مشتریان (${PersianDateUtil.toPersianDigits(customers.size.toString())})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("انبار قطعات (${PersianDateUtil.toPersianDigits(inventory.size.toString())})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("فاکتورها (${PersianDateUtil.toPersianDigits(invoices.size.toString())})", fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> MissionsListView(missions, onUpdateStatus = { m, s -> viewModel.updateMissionStatus(m, s) })
                1 -> CustomersListView(customers)
                2 -> InventoryListView(inventory, onStockDelta = { item, delta -> viewModel.updateInventoryStock(item, delta) })
                3 -> InvoicesListView(invoices)
            }
        }
    }

    // Dialog: Add Mission
    if (showAddMissionDialog) {
        var customerName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("نصب دیش") }
        var address by remember { mutableStateOf("") }
        var feeText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMissionDialog = false },
            title = { Text("ثبت مأموریت کاری جدید", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("نام مشتری") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("شماره تماس") }, singleLine = true)
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("نوع خدمت (نصب، تنظیم، قیچی، تعمیر)") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس محل کار") }, singleLine = true)
                    OutlinedTextField(value = feeText, onValueChange = { feeText = it }, label = { Text("اجرت برآوردی (تومان)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fee = feeText.toLongOrNull() ?: 0L
                        viewModel.addMission(
                            MissionEntity(
                                customerId = 0,
                                customerName = customerName.ifBlank { "مشتری جدید" },
                                customerPhone = phone,
                                missionType = type,
                                status = "برنامه‌ریزی‌شده",
                                scheduledDateJalali = viewModel.currentDatePersian.format(),
                                priority = "عادی",
                                costEstimate = fee,
                                finalFee = fee,
                                address = address
                            )
                        )
                        showAddMissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("ثبت مأموریت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMissionDialog = false }) { Text("انصراف", color = Color.Gray) }
            },
            containerColor = SpaceNavySurface
        )
    }

    // Dialog: Add Customer
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCustomerDialog = false },
            title = { Text("افزودن مشتری جدید", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام و نام خانوادگی") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("شماره تماس") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("آدرس دقیق") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات تکمیلی (نوع دیش، سوییچ)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCustomer(
                            CustomerEntity(
                                fullName = name.ifBlank { "مشتری" },
                                phone = phone,
                                address = address,
                                notes = notes
                            )
                        )
                        showAddCustomerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }) { Text("انصراف", color = Color.Gray) }
            },
            containerColor = SpaceNavySurface
        )
    }

    // Dialog: Add Inventory Item
    if (showAddInventoryDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("LNB") }
        var qtyText by remember { mutableStateOf("1") }
        var priceText by remember { mutableStateOf("100000") }

        AlertDialog(
            onDismissRequest = { showAddInventoryDialog = false },
            title = { Text("افزودن کالا به انبار", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام کالا") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("دسته‌بندی (LNB، دیش، کابل، سوییچ)") }, singleLine = true)
                    OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("تعداد موجودی") }, singleLine = true)
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("قیمت فروش (تومان)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = qtyText.toIntOrNull() ?: 1
                        val price = priceText.toLongOrNull() ?: 0L
                        viewModel.addInventoryItem(
                            InventoryItemEntity(
                                name = name.ifBlank { "قطعه جدید" },
                                category = category,
                                quantity = qty,
                                salePrice = price,
                                purchasePrice = (price * 0.75).toLong()
                            )
                        )
                        showAddInventoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddInventoryDialog = false }) { Text("انصراف", color = Color.Gray) }
            },
            containerColor = SpaceNavySurface
        )
    }

    // Dialog: Add Invoice
    if (showAddInvoiceDialog) {
        var customerName by remember { mutableStateOf("") }
        var itemsSummary by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddInvoiceDialog = false },
            title = { Text("صدور فاکتور جدید", color = SignalCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("نام مشتری") }, singleLine = true)
                    OutlinedTextField(value = itemsSummary, onValueChange = { itemsSummary = it }, label = { Text("شرح اقلام و خدمات") }, singleLine = true)
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("مبلغ کل (تومان)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        val invNum = "INV-${System.currentTimeMillis() % 10000}"
                        viewModel.addInvoice(
                            InvoiceEntity(
                                invoiceNumber = invNum,
                                customerId = 0,
                                customerName = customerName.ifBlank { "مشتری" },
                                dateJalali = viewModel.currentDatePersian.format(),
                                itemsSummary = itemsSummary,
                                totalAmount = amount,
                                paidAmount = amount,
                                remainingAmount = 0
                            )
                        )
                        showAddInvoiceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalCyan, contentColor = SpaceNavyDark)
                ) {
                    Text("ثبت فاکتور")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddInvoiceDialog = false }) { Text("انصراف", color = Color.Gray) }
            },
            containerColor = SpaceNavySurface
        )
    }
}

@Composable
private fun MissionsListView(
    missions: List<MissionEntity>,
    onUpdateStatus: (MissionEntity, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        if (missions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("هیچ مأموریتی ثبت نشده است.", color = Color.Gray)
                }
            }
        }

        items(missions) { mission ->
            val statusColor = when (mission.status) {
                "تکمیل‌شده" -> SignalGreen
                "در حال انجام" -> GoldLock
                "در مسیر" -> SignalCyan
                else -> Color.LightGray
            }

            GlassCard(borderColor = statusColor.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${mission.missionType} • ${mission.customerName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تاریخ: ${PersianDateUtil.toPersianDigits(mission.scheduledDateJalali)} • اولویت: ${mission.priority}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = mission.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (mission.address.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "آدرس: ${mission.address}", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اجرت: ${PersianDateUtil.toPersianDigits(String.format("%,d", mission.finalFee))} تومان",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLock
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (mission.status != "تکمیل‌شده") {
                            FilledTonalButton(
                                onClick = { onUpdateStatus(mission, "تکمیل‌شده") },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SignalGreen.copy(alpha = 0.2f), contentColor = SignalGreen)
                            ) {
                                Text("تکمیل کار", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomersListView(customers: List<CustomerEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(customers) { c ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = c.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "تلفن: ${PersianDateUtil.toPersianDigits(c.phone)}", fontSize = 12.sp, color = SignalCyan)
                    }
                    if (c.totalDebt > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SignalRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "بدهی: ${PersianDateUtil.toPersianDigits(String.format("%,d", c.totalDebt))} ت",
                                fontSize = 10.sp,
                                color = SignalRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "آدرس: ${c.address}", fontSize = 11.sp, color = Color.LightGray)
                if (c.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "یادداشت: ${c.notes}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun InventoryListView(
    inventory: List<InventoryItemEntity>,
    onStockDelta: (InventoryItemEntity, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(inventory) { item ->
            val isLowStock = item.quantity <= item.minStockThreshold

            GlassCard(
                borderColor = if (isLowStock) SignalRed.copy(alpha = 0.4f) else SpaceNavySurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            if (isLowStock) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = SignalRed.copy(alpha = 0.2f)) {
                                    Text("کسری موجودی", fontSize = 9.sp, color = SignalRed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "قیمت فروش: ${PersianDateUtil.toPersianDigits(String.format("%,d", item.salePrice))} تومان • دسته: ${item.category}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    // Stock Counter Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onStockDelta(item, -1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SpaceNavyDark)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${PersianDateUtil.toPersianDigits(item.quantity.toString())} ${item.unit}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) SignalRed else SignalGreen,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onStockDelta(item, 1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SpaceNavyDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SignalCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoicesListView(invoices: List<InvoiceEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        items(invoices) { inv ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "فاکتور #${inv.invoiceNumber}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SignalCyan)
                        Text(text = "مشتری: ${inv.customerName} • تاریخ: ${PersianDateUtil.toPersianDigits(inv.dateJalali)}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        text = "${PersianDateUtil.toPersianDigits(String.format("%,d", inv.totalAmount))} تومان",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLock
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "شرح اقلام: ${inv.itemsSummary}", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}
