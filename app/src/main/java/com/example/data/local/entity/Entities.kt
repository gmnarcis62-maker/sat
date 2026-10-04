package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val orbitalPositionDeg: Double,
    val direction: String, // "E" or "W"
    val operator: String,
    val coverage: String,
    val bands: String,
    val isFavorite: Boolean = false,
    val verificationDate: String = "1403/07",
    val source: String = "FlySat / LyngSat Verified"
)

@Entity(tableName = "transponders")
data class TransponderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val satelliteId: Long,
    val frequencyMHz: Int,
    val polarization: String, // "H" or "V"
    val symbolRate: Int,
    val fec: String,
    val standard: String, // "DVB-S2 8PSK" or "DVB-S QPSK"
    val channels: String,
    val isStrong: Boolean = false, // Recommended lock frequency (فرکانس مادر)
    val status: String = "فعال",
    val beam: String = "Middle East / Iran"
)

@Entity(tableName = "receivers")
data class ReceiverModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val modelName: String,
    val tunerType: String,
    val cpuModel: String,
    val powerSupplySpecs: String,
    val flashChip: String = "SPI Flash 8MB/16MB",
    val ramSpecs: String = "DDR3 128MB/256MB",
    val commonIssues: String
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val secondaryPhone: String = "",
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val totalDebt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val missionType: String, // "نصب دیش", "تنظیم دیش", "تعمیر رسیور", "نصب قیچی", "تنظیم موتور"
    val status: String = "برنامه‌ریزی‌شده", // "جدید", "برنامه‌ریزی‌شده", "در مسیر", "در حال انجام", "تکمیل‌شده", "لغوشده"
    val scheduledDateJalali: String,
    val priority: String = "عادی", // "عادی", "فوری", "بسیار مهم"
    val costEstimate: Long = 0,
    val finalFee: Long = 0,
    val address: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val dateJalali: String,
    val itemsSummary: String, // e.g. "نصب دیش + کابل RG6 (۲۰ متر) + ال‌ان‌بی پریمیوم"
    val totalAmount: Long,
    val discount: Long = 0,
    val paidAmount: Long,
    val remainingAmount: Long,
    val notes: String = ""
)

@Entity(tableName = "inventory")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "LNB", "دیش", "کابل", "سوییچ", "موتور", "اتصالات"
    val quantity: Int,
    val minStockThreshold: Int = 3,
    val purchasePrice: Long,
    val salePrice: Long,
    val unit: String = "عدد"
)

@Entity(tableName = "repairs")
data class RepairCaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val receiverModel: String,
    val symptom: String, // "روشن نمی‌شود", "چراغ قرمز مانده", "بدون سیگنال", "هنگ روی بوت"
    val diagnosis: String,
    val partsReplaced: String,
    val laborCost: Long,
    val partsCost: Long,
    val totalCost: Long,
    val status: String = "تکمیل‌شده", // "در دست بررسی", "منتظر قطعه", "تکمیل‌شده"
    val dateJalali: String,
    val notes: String = ""
)
