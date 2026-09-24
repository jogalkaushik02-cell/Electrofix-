package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.security.PasswordSecurityHelper

@Entity(tableName = "admin_settings")
data class AdminSettings(
    @PrimaryKey val id: Int = 1,
    val adminPassword: String = DEFAULT_ADMIN_PASSWORD,
    val passwordHash: String = PasswordSecurityHelper.DEFAULT_ADMIN_HASH,
    val isPasswordSet: Boolean = true,
    val storeName: String = "ElectroFix Electronics & Repairs",
    val storePhone: String = "+91 98765 43210",
    val storeAddress: String = "Main Bazar, Station Road, Rajkot, Gujarat",
    val storeUpiId: String = "electrofix@upi",
    val lastBackupTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_ADMIN_PASSWORD = "admin123"
    }
}
