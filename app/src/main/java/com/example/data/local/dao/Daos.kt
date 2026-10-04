package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteDao {
    @Query("SELECT * FROM satellites ORDER BY orbitalPositionDeg ASC")
    fun getAllSatellites(): Flow<List<SatelliteEntity>>

    @Query("SELECT * FROM satellites WHERE isFavorite = 1 ORDER BY orbitalPositionDeg ASC")
    fun getFavoriteSatellites(): Flow<List<SatelliteEntity>>

    @Query("SELECT * FROM satellites WHERE id = :id LIMIT 1")
    suspend fun getSatelliteById(id: Long): SatelliteEntity?

    @Query("SELECT * FROM transponders WHERE satelliteId = :satelliteId ORDER BY frequencyMHz ASC")
    fun getTranspondersForSatellite(satelliteId: Long): Flow<List<TransponderEntity>>

    @Query("SELECT * FROM transponders ORDER BY frequencyMHz ASC")
    fun getAllTransponders(): Flow<List<TransponderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatellites(satellites: List<SatelliteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatellite(satellite: SatelliteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransponders(transponders: List<TransponderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransponder(transponder: TransponderEntity): Long

    @Update
    suspend fun updateSatellite(satellite: SatelliteEntity)

    @Delete
    suspend fun deleteSatellite(satellite: SatelliteEntity)

    @Delete
    suspend fun deleteTransponder(transponder: TransponderEntity)

    @Query("SELECT COUNT(*) FROM satellites")
    suspend fun getSatelliteCount(): Int
}

@Dao
interface ReceiverDao {
    @Query("SELECT * FROM receivers ORDER BY brand ASC, modelName ASC")
    fun getAllReceivers(): Flow<List<ReceiverModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivers(receivers: List<ReceiverModelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiver(receiver: ReceiverModelEntity): Long

    @Query("SELECT COUNT(*) FROM receivers")
    suspend fun getReceiverCount(): Int
}

@Dao
interface BusinessDao {
    // Customers
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    // Missions
    @Query("SELECT * FROM missions ORDER BY createdAt DESC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Update
    suspend fun updateMission(mission: MissionEntity)

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    // Inventory
    @Query("SELECT * FROM inventory ORDER BY category ASC, name ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItemEntity)

    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getInventoryCount(): Int

    // Repairs
    @Query("SELECT * FROM repairs ORDER BY id DESC")
    fun getAllRepairs(): Flow<List<RepairCaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepairCase(repair: RepairCaseEntity): Long

    @Delete
    suspend fun deleteRepairCase(repair: RepairCaseEntity)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int
}
