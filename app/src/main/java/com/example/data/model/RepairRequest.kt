package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_requests")
data class RepairRequest(
    @PrimaryKey
    val id: String, // e.g. "#R001"
    val customerName: String,
    val customerPhone: String,
    val itemType: String, // "Smartphone", "Laptop", "Tablet", etc.
    val problemTitle: String,
    val problemDescription: String,
    val photoUri: String? = null,
    val preferredDate: String,
    val status: String = STATUS_RECEIVED,
    val estimatedPrice: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_RECEIVED = "Request received"
        const val STATUS_CONTACTED = "Contacted"
        const val STATUS_REPAIRING = "Repairing"
        const val STATUS_READY = "Ready"
        const val STATUS_COMPLETED = "Completed"

        val ALL_STATUSES = listOf(
            STATUS_RECEIVED,
            STATUS_CONTACTED,
            STATUS_REPAIRING,
            STATUS_READY,
            STATUS_COMPLETED
        )

        val ITEM_TYPES = listOf(
            "Smartphone",
            "Laptop & PC",
            "Tablet / iPad",
            "Smartwatch",
            "Audio & Headphones",
            "TV & Monitor",
            "Gaming Console",
            "Other Device"
        )
    }

    val stageIndex: Int
        get() = when (status) {
            STATUS_CONTACTED -> 1
            STATUS_REPAIRING -> 2
            STATUS_READY -> 3
            STATUS_COMPLETED -> 4
            else -> 0
        }
}
