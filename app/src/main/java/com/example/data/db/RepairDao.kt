package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RepairRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {
    @Query("SELECT * FROM repair_requests ORDER BY createdAt DESC")
    fun getAllRepairs(): Flow<List<RepairRequest>>

    @Query("SELECT * FROM repair_requests ORDER BY createdAt DESC")
    suspend fun getAllRepairsList(): List<RepairRequest>

    @Query("SELECT * FROM repair_requests WHERE id = :id LIMIT 1")
    fun getRepairById(id: String): Flow<RepairRequest?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepair(repair: RepairRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repairs: List<RepairRequest>)

    @Update
    suspend fun updateRepair(repair: RepairRequest)

    @Query("UPDATE repair_requests SET status = :status WHERE id = :repairId")
    suspend fun updateRepairStatus(repairId: String, status: String)

    @Query("SELECT COUNT(*) FROM repair_requests")
    suspend fun getRepairCount(): Int
}
