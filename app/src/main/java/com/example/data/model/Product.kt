package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val mrp: Double,
    val stock: Int,
    val isAvailable: Boolean = true,
    val description: String,
    val specifications: String = "",
    val imageUrl: String = "",
    val iconCategory: String = "gadget", // phone, laptop, battery, cable, screen, tool, audio
    val rating: Float = 4.8f,
    val reviewsCount: Int = 24
) {
    val discountPercent: Int
        get() = if (mrp > price && mrp > 0) (((mrp - price) / mrp) * 100).toInt() else 0

    val isInStock: Boolean
        get() = isAvailable && stock > 0
}
