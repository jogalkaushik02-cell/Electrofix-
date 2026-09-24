package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.InitialData
import com.example.data.firebase.FirebaseService
import com.example.data.firebase.FirebaseStatus
import com.example.data.model.AdminSettings
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.RepairRequest
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ShopRepository(
    private val database: AppDatabase,
    private val firebaseService: FirebaseService? = null
) {

    private val productDao = database.productDao()
    private val cartDao = database.cartDao()
    private val orderDao = database.orderDao()
    private val repairDao = database.repairDao()
    private val adminSettingsDao = database.adminSettingsDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allRepairs: Flow<List<RepairRequest>> = repairDao.getAllRepairs()
    val adminSettings: Flow<AdminSettings?> = adminSettingsDao.getSettingsFlow()

    val firebaseStatus: StateFlow<FirebaseStatus>? = firebaseService?.firebaseStatus
    val currentUser: StateFlow<FirebaseUser?>? = firebaseService?.currentUser

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val productCount = productDao.getCount()
        if (productCount == 0) {
            productDao.insertAll(InitialData.getInitialProducts())
        }
        val orderCount = orderDao.getOrderCount()
        if (orderCount == 0) {
            orderDao.insertAll(InitialData.getInitialOrders())
        }
        val repairCount = repairDao.getRepairCount()
        if (repairCount == 0) {
            repairDao.insertAll(InitialData.getInitialRepairs())
        }
        val currentSettings = adminSettingsDao.getSettings()
        if (currentSettings == null) {
            adminSettingsDao.insertOrUpdate(
                AdminSettings(
                    id = 1,
                    adminPassword = "",
                    isPasswordSet = false,
                    storeName = "ElectroFix Electronics & Repairs",
                    storePhone = "+91 98765 43210",
                    storeAddress = "Main Bazar, Station Road, Rajkot, Gujarat",
                    storeUpiId = "electrofix@upi"
                )
            )
        }
    }

    suspend fun addToCart(product: Product, quantityToAdd: Int = 1) = withContext(Dispatchers.IO) {
        val existing = cartDao.getItem(product.id)
        if (existing != null) {
            val newQty = (existing.quantity + quantityToAdd).coerceAtMost(product.stock)
            cartDao.updateItem(existing.copy(quantity = newQty))
        } else {
            val item = CartItem(
                productId = product.id,
                productName = product.name,
                category = product.category,
                price = product.price,
                quantity = quantityToAdd.coerceAtMost(product.stock),
                iconCategory = product.iconCategory,
                imageUrl = product.imageUrl
            )
            cartDao.insertItem(item)
        }
    }

    suspend fun updateCartQuantity(productId: Long, newQuantity: Int) = withContext(Dispatchers.IO) {
        if (newQuantity <= 0) {
            cartDao.deleteItem(productId)
        } else {
            val existing = cartDao.getItem(productId)
            if (existing != null) {
                cartDao.updateItem(existing.copy(quantity = newQuantity))
            }
        }
    }

    suspend fun removeFromCart(productId: Long) = withContext(Dispatchers.IO) {
        cartDao.deleteItem(productId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        paymentMethod: String,
        cartItems: List<CartItem>,
        deliveryFee: Double = 40.0
    ): Result<String> = withContext(Dispatchers.IO) {
        if (cartItems.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Cart is empty"))
        }

        val subtotal = cartItems.sumOf { it.price * it.quantity }
        val total = subtotal + deliveryFee
        val itemsSummary = cartItems.joinToString(", ") { "${it.productName} x${it.quantity}" }

        val orderNumber = Random.nextInt(1000, 9999)
        val orderId = "#$orderNumber"

        val order = Order(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            paymentMethod = paymentMethod,
            itemsSummary = itemsSummary,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            total = total,
            status = Order.STATUS_PLACED,
            createdAt = System.currentTimeMillis()
        )

        // 1. Save to Room SQLite database (offline reliable)
        orderDao.insertOrder(order)

        // 2. Decrement stock in SQLite
        cartItems.forEach { item ->
            productDao.decrementStock(item.productId, item.quantity)
        }

        // 3. Clear cart
        cartDao.clearCart()

        // 4. Sync to Cloud Firestore if available
        try {
            firebaseService?.uploadOrder(order)
        } catch (_: Exception) {
            // Local order placed safely
        }

        Result.success(orderId)
    }

    suspend fun submitRepairRequest(
        customerName: String,
        customerPhone: String,
        itemType: String,
        problemTitle: String,
        problemDescription: String,
        preferredDate: String,
        photoUri: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        val repairNumber = Random.nextInt(100, 999)
        val repairId = "#R$repairNumber"

        val request = RepairRequest(
            id = repairId,
            customerName = customerName,
            customerPhone = customerPhone,
            itemType = itemType,
            problemTitle = problemTitle,
            problemDescription = problemDescription,
            photoUri = photoUri,
            preferredDate = preferredDate,
            status = RepairRequest.STATUS_RECEIVED,
            estimatedPrice = null,
            createdAt = System.currentTimeMillis()
        )

        // 1. Save to Room SQLite database
        repairDao.insertRepair(request)

        // 2. Sync to Cloud Firestore if available
        try {
            firebaseService?.uploadRepair(request)
        } catch (_: Exception) {
            // Local repair ticket placed safely
        }

        Result.success(repairId)
    }

    // Admin Operations
    suspend fun updateProductPriceAndStock(productId: Long, price: Double, stock: Int) = withContext(Dispatchers.IO) {
        productDao.updatePriceAndStock(productId, price, stock)
        try {
            productDao.getProductDirect(productId)?.let {
                firebaseService?.uploadProducts(listOf(it))
            }
        } catch (_: Exception) {}
    }

    suspend fun addProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
        try {
            firebaseService?.uploadProducts(listOf(product))
        } catch (_: Exception) {}
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        try {
            firebaseService?.uploadProducts(listOf(product))
        } catch (_: Exception) {}
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun updateRepairStatus(repairId: String, status: String) = withContext(Dispatchers.IO) {
        repairDao.updateRepairStatus(repairId, status)
    }

    // Admin Settings & Password Operations (Room SQLite Persistence)
    suspend fun getAdminSettings(): AdminSettings? = withContext(Dispatchers.IO) {
        adminSettingsDao.getSettings()
    }

    suspend fun saveAdminPassword(password: String): Unit = withContext(Dispatchers.IO) {
        val isSet = password.isNotBlank()
        val current = adminSettingsDao.getSettings() ?: AdminSettings()
        adminSettingsDao.insertOrUpdate(
            current.copy(
                adminPassword = password,
                isPasswordSet = isSet
            )
        )
    }

    suspend fun updateAdminSettings(settings: AdminSettings) = withContext(Dispatchers.IO) {
        adminSettingsDao.insertOrUpdate(settings)
    }

    suspend fun resetDatabaseToInitial() = withContext(Dispatchers.IO) {
        productDao.insertAll(InitialData.getInitialProducts())
        orderDao.insertAll(InitialData.getInitialOrders())
        repairDao.insertAll(InitialData.getInitialRepairs())
    }

    // ==========================================
    // Cloud Firestore Sync Operations
    // ==========================================

    suspend fun syncAllToFirestore(): Result<String> = withContext(Dispatchers.IO) {
        val fb = firebaseService ?: return@withContext Result.failure(IllegalStateException("Firebase service is offline"))
        val products = productDao.getAllProductsList()
        val orders = orderDao.getAllOrdersList()
        val repairs = repairDao.getAllRepairsList()

        val pCount = fb.uploadProducts(products).getOrNull() ?: 0
        val oCount = fb.uploadOrders(orders).getOrNull() ?: 0
        val rCount = fb.uploadRepairs(repairs).getOrNull() ?: 0

        Result.success("Cloud Sync Successful: $pCount products, $oCount orders, $rCount repairs synced to Firestore.")
    }

    suspend fun pullAllFromFirestore(): Result<String> = withContext(Dispatchers.IO) {
        val fb = firebaseService ?: return@withContext Result.failure(IllegalStateException("Firebase service is offline"))
        val remoteProducts = fb.downloadProducts().getOrNull() ?: emptyList()
        if (remoteProducts.isNotEmpty()) {
            productDao.insertAll(remoteProducts)
        }
        val remoteOrders = fb.downloadOrders().getOrNull() ?: emptyList()
        if (remoteOrders.isNotEmpty()) {
            orderDao.insertAll(remoteOrders)
        }
        val remoteRepairs = fb.downloadRepairs().getOrNull() ?: emptyList()
        if (remoteRepairs.isNotEmpty()) {
            repairDao.insertAll(remoteRepairs)
        }

        Result.success("Cloud Pull Successful: ${remoteProducts.size} products, ${remoteOrders.size} orders, ${remoteRepairs.size} repairs restored.")
    }

    // ==========================================
    // Firebase Authentication Operations
    // ==========================================

    suspend fun firebaseSignIn(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        firebaseService?.signInWithEmail(email, pass) ?: Result.failure(IllegalStateException("Firebase Auth unavailable"))
    }

    suspend fun firebaseSignUp(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        firebaseService?.signUpWithEmail(email, pass) ?: Result.failure(IllegalStateException("Firebase Auth unavailable"))
    }

    suspend fun firebaseSignInGuest(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        firebaseService?.signInAnonymously() ?: Result.failure(IllegalStateException("Firebase Auth unavailable"))
    }

    fun firebaseSignOut() {
        firebaseService?.signOut()
    }
}
