package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_settings")
data class AdminSettings(
    @PrimaryKey val id: Int = 1,
    val adminPassword: String = "",
    val isPasswordSet: Boolean = false,
    val storeName: String = "ElectroFix Electronics & Repairs",
    val storePhone: String = "+91 98765 43210",
    val storeAddress: String = "Main Bazar, Station Road, Rajkot, Gujarat",
    val storeUpiId: String = "electrofix@upi",
    val lastBackupTimestamp: Long = System.currentTimeMillis()
)
