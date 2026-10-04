package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SatelliteEntity::class,
        TransponderEntity::class,
        ReceiverModelEntity::class,
        CustomerEntity::class,
        MissionEntity::class,
        InvoiceEntity::class,
        InventoryItemEntity::class,
        RepairCaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun satelliteDao(): SatelliteDao
    abstract fun receiverDao(): ReceiverDao
    abstract fun businessDao(): BusinessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "satellite_pro_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val satDao = db.satelliteDao()
            val recDao = db.receiverDao()
            val busDao = db.businessDao()

            if (satDao.getSatelliteCount() == 0) {
                satDao.insertSatellites(InitialData.satellites)
                satDao.insertTransponders(InitialData.transponders)
            }
            if (recDao.getReceiverCount() == 0) {
                recDao.insertReceivers(InitialData.receivers)
            }
            if (busDao.getInventoryCount() == 0) {
                busDao.insertInventoryItems(InitialData.inventory)
            }
            if (busDao.getCustomerCount() == 0) {
                InitialData.customers.forEach { busDao.insertCustomer(it) }
                InitialData.missions.forEach { busDao.insertMission(it) }
            }
        }
    }
}
