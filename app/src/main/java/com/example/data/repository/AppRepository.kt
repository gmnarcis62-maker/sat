package com.example.data.repository

import android.content.Context
import com.example.data.gemini.GeminiService
import com.example.data.hardware.HardwareSignalMeterAdapter
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppRepository(context: Context, scope: CoroutineScope) {

    private val database = AppDatabase.getDatabase(context, scope)
    val satelliteDao = database.satelliteDao()
    val receiverDao = database.receiverDao()
    val businessDao = database.businessDao()

    val signalMeterAdapter = HardwareSignalMeterAdapter()
    val geminiService = GeminiService(context)
    val atriaAiService = com.example.data.api.atria.AtriaAiService(context)

    // Flow getters
    val allSatellites: Flow<List<SatelliteEntity>> = satelliteDao.getAllSatellites()
    val allReceivers: Flow<List<ReceiverModelEntity>> = receiverDao.getAllReceivers()
    val allCustomers: Flow<List<CustomerEntity>> = businessDao.getAllCustomers()
    val allMissions: Flow<List<MissionEntity>> = businessDao.getAllMissions()
    val allInvoices: Flow<List<InvoiceEntity>> = businessDao.getAllInvoices()
    val allInventory: Flow<List<InventoryItemEntity>> = businessDao.getAllInventory()
    val allRepairs: Flow<List<RepairCaseEntity>> = businessDao.getAllRepairs()

    init {
        // Trigger pre-population on first access if needed
        scope.launch(Dispatchers.IO) {
            AppDatabase.populateDatabase(database)
        }
    }

    fun getTranspondersForSatellite(satId: Long): Flow<List<TransponderEntity>> {
        return satelliteDao.getTranspondersForSatellite(satId)
    }

    suspend fun addSatellite(satellite: SatelliteEntity): Long {
        return satelliteDao.insertSatellite(satellite)
    }

    suspend fun addTransponder(transponder: TransponderEntity): Long {
        return satelliteDao.insertTransponder(transponder)
    }

    suspend fun addCustomer(customer: CustomerEntity): Long {
        return businessDao.insertCustomer(customer)
    }

    suspend fun addMission(mission: MissionEntity): Long {
        return businessDao.insertMission(mission)
    }

    suspend fun updateMission(mission: MissionEntity) {
        businessDao.updateMission(mission)
    }

    suspend fun addInvoice(invoice: InvoiceEntity): Long {
        return businessDao.insertInvoice(invoice)
    }

    suspend fun addInventoryItem(item: InventoryItemEntity): Long {
        return businessDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) {
        businessDao.updateInventoryItem(item)
    }

    suspend fun addRepairCase(repair: RepairCaseEntity): Long {
        return businessDao.insertRepairCase(repair)
    }
}
