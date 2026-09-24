package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdminSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminSettingsDao {
    @Query("SELECT * FROM admin_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AdminSettings?>

    @Query("SELECT * FROM admin_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AdminSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AdminSettings)

    @Query("UPDATE admin_settings SET adminPassword = :password, isPasswordSet = :isSet WHERE id = 1")
    suspend fun updatePassword(password: String, isSet: Boolean)
}
