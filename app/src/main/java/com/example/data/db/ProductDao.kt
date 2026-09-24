package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsList(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<Product?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductDirect(id: Long): Product?

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET price = :price, stock = :stock WHERE id = :id")
    suspend fun updatePriceAndStock(id: Long, price: Double, stock: Int)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :id AND stock >= :quantity")
    suspend fun decrementStock(id: Long, quantity: Int)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}
