package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class OrderItem(
    val productId: Long,
    val productName: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String, // e.g. "#1001"
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val paymentMethod: String, // COD or UPI
    val itemsSummary: String, // Formatted summary e.g. "Screen Assembly x1, Fast Charger x2"
    val subtotal: Double,
    val deliveryFee: Double = 40.0,
    val total: Double,
    val status: String = STATUS_PLACED, // Placed, Confirmed, Packed, Shipped, Delivered
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PLACED = "Order placed"
        const val STATUS_CONFIRMED = "Confirmed"
        const val STATUS_PACKED = "Packed"
        const val STATUS_SHIPPED = "Shipped"
        const val STATUS_DELIVERED = "Delivered"

        val ALL_STATUSES = listOf(
            STATUS_PLACED,
            STATUS_CONFIRMED,
            STATUS_PACKED,
            STATUS_SHIPPED,
            STATUS_DELIVERED
        )
    }

    val stageIndex: Int
        get() = when (status) {
            STATUS_CONFIRMED -> 1
            STATUS_PACKED -> 2
            STATUS_SHIPPED -> 3
            STATUS_DELIVERED -> 4
            else -> 0
        }
}
