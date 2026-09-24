package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    suspend fun getAllOrdersList(): List<Order>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: String): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<Order>)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
}
