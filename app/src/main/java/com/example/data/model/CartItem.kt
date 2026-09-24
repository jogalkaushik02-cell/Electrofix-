package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val productId: Long,
    val productName: String,
    val category: String,
    val price: Double,
    val quantity: Int = 1,
    val iconCategory: String = "gadget",
    val imageUrl: String = ""
) {
    val totalItemPrice: Double
        get() = price * quantity
}
