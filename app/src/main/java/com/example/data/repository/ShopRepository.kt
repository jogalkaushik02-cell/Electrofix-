package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.InitialData
import com.example.data.firebase.FirebaseService
import com.example.data.firebase.FirebaseStatus
import com.example.data.model.AdminSettings
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.RepairRequest
import com.example.data.security.EncryptedPreferencesManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ShopRepository(
    private val context: Context,
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

    init {
        // 1. Live Admin Settings Real-time Stream
        firebaseService?.onRemoteAdminSettingsChanged = { remoteSettings ->
            CoroutineScope(Dispatchers.IO).launch {
                if (remoteSettings.adminPassword.isNotBlank()) {
                    EncryptedPreferencesManager.saveAdminPassword(context, remoteSettings.adminPassword)
                } else if (!remoteSettings.isPasswordSet) {
                    EncryptedPreferencesManager.deleteAdminPassword(context)
                }
                adminSettingsDao.insertOrUpdate(remoteSettings.copy(id = 1))
            }
        }

        // 2. Live Products Real-time Stream (Amazon / Flipkart Instant Inventory & Price Push)
        firebaseService?.onRemoteProductsChanged = { remoteProducts ->
            CoroutineScope(Dispatchers.IO).launch {
                productDao.insertAll(remoteProducts)
            }
        }

        // 3. Live Orders Real-time Stream (Instant Live Status Dispatch)
        firebaseService?.onRemoteOrdersChanged = { remoteOrders ->
            CoroutineScope(Dispatchers.IO).launch {
                orderDao.insertAll(remoteOrders)
            }
        }

        // 4. Live Repairs Real-time Stream (Instant Live Repair Status Push)
        firebaseService?.onRemoteRepairsChanged = { remoteRepairs ->
            CoroutineScope(Dispatchers.IO).launch {
                repairDao.insertAll(remoteRepairs)
            }
        }
    }

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
            val encPass = EncryptedPreferencesManager.getAdminPassword(context) ?: ""
            adminSettingsDao.insertOrUpdate(
                AdminSettings(
                    id = 1,
                    adminPassword = encPass,
                    isPasswordSet = encPass.isNotBlank(),
                    storeName = "ElectroFix Electronics & Repairs",
                    storePhone = "+91 98765 43210",
                    storeAddress = "Main Bazar, Station Road, Rajkot, Gujarat",
                    storeUpiId = "electrofix@upi"
                )
            )
        }

        // Initial cloud pull
        try {
            syncSettingsFromCloud()
        } catch (_: Exception) {}
    }

    suspend fun syncSettingsFromCloud(): Result<AdminSettings?> = withContext(Dispatchers.IO) {
        val fb = firebaseService ?: return@withContext Result.failure(IllegalStateException("Firebase offline"))
        try {
            val cloudSettings = fb.downloadAdminSettings().getOrNull()
            if (cloudSettings != null && cloudSettings.isPasswordSet && cloudSettings.adminPassword.isNotBlank()) {
                EncryptedPreferencesManager.saveAdminPassword(context, cloudSettings.adminPassword)
                adminSettingsDao.insertOrUpdate(cloudSettings.copy(id = 1))
                return@withContext Result.success(cloudSettings)
            } else {
                val local = adminSettingsDao.getSettings()
                if (local != null && local.isPasswordSet && local.adminPassword.isNotBlank()) {
                    fb.uploadAdminSettings(local)
                }
            }
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
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

        // 2. Decrement stock in SQLite & push updated stock
        cartItems.forEach { item ->
            productDao.decrementStock(item.productId, item.quantity)
            try {
                productDao.getProductDirect(item.productId)?.let { updatedProd ->
                    firebaseService?.updateProductPriceAndStock(updatedProd.id, updatedProd.price, updatedProd.stock)
                }
            } catch (_: Exception) {}
        }

        // 3. Clear cart
        cartDao.clearCart()

        // 4. Real-time push to Cloud Firestore immediately
        try {
            firebaseService?.uploadOrder(order)
        } catch (_: Exception) {}

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

        // 2. Real-time push to Cloud Firestore immediately
        try {
            firebaseService?.uploadRepair(request)
        } catch (_: Exception) {}

        Result.success(repairId)
    }

    // Admin Operations
    suspend fun updateProductPriceAndStock(productId: Long, price: Double, stock: Int) = withContext(Dispatchers.IO) {
        productDao.updatePriceAndStock(productId, price, stock)
        try {
            firebaseService?.updateProductPriceAndStock(productId, price, stock)
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
        try {
            firebaseService?.updateOrderStatus(orderId, status)
        } catch (_: Exception) {}
    }

    suspend fun updateRepairStatus(repairId: String, status: String) = withContext(Dispatchers.IO) {
        repairDao.updateRepairStatus(repairId, status)
        try {
            firebaseService?.updateRepairStatus(repairId, status)
        } catch (_: Exception) {}
    }

    // Admin Settings & Password Operations (Room SQLite Persistence + EncryptedSharedPreferences + Cloud Firestore)
    suspend fun getAdminSettings(): AdminSettings? = withContext(Dispatchers.IO) {
        val settings = adminSettingsDao.getSettings()
        val encPass = EncryptedPreferencesManager.getAdminPassword(context)
        if (encPass != null && settings != null && settings.adminPassword != encPass) {
            settings.copy(adminPassword = encPass, isPasswordSet = encPass.isNotBlank())
        } else {
            settings
        }
    }

    suspend fun saveAdminPassword(password: String): Unit = withContext(Dispatchers.IO) {
        val isSet = password.isNotBlank()
        if (isSet) {
            EncryptedPreferencesManager.saveAdminPassword(context, password)
        } else {
            EncryptedPreferencesManager.deleteAdminPassword(context)
        }

        val current = adminSettingsDao.getSettings() ?: AdminSettings()
        val updated = current.copy(
            adminPassword = password,
            isPasswordSet = isSet,
            lastBackupTimestamp = System.currentTimeMillis()
        )
        adminSettingsDao.insertOrUpdate(updated)

        // Sync immediately to Firebase Cloud Firestore so all mobile devices receive it!
        try {
            firebaseService?.uploadAdminSettings(updated)
        } catch (_: Exception) {}
    }

    suspend fun updateAdminSettings(settings: AdminSettings) = withContext(Dispatchers.IO) {
        adminSettingsDao.insertOrUpdate(settings)
        try {
            firebaseService?.uploadAdminSettings(settings)
        } catch (_: Exception) {}
    }

    suspend fun resetDatabaseToInitial() = withContext(Dispatchers.IO) {
        productDao.insertAll(InitialData.getInitialProducts())
        orderDao.insertAll(InitialData.getInitialOrders())
        repairDao.insertAll(InitialData.getInitialRepairs())
    }

    // ==========================================
    // Cloud Firestore Manual Sync Operations
    // ==========================================

    suspend fun syncAllToFirestore(): Result<String> = withContext(Dispatchers.IO) {
        val fb = firebaseService ?: return@withContext Result.failure(IllegalStateException("Firebase service is offline"))
        val products = productDao.getAllProductsList()
        val orders = orderDao.getAllOrdersList()
        val repairs = repairDao.getAllRepairsList()
        val settings = adminSettingsDao.getSettings() ?: AdminSettings()

        val pCount = fb.uploadProducts(products).getOrNull() ?: 0
        val oCount = fb.uploadOrders(orders).getOrNull() ?: 0
        val rCount = fb.uploadRepairs(repairs).getOrNull() ?: 0
        fb.uploadAdminSettings(settings)

        Result.success("Cloud Sync Successful: $pCount products, $oCount orders, $rCount repairs, admin settings synced to Firestore.")
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

        val remoteSettings = fb.downloadAdminSettings().getOrNull()
        if (remoteSettings != null) {
            if (remoteSettings.adminPassword.isNotBlank()) {
                EncryptedPreferencesManager.saveAdminPassword(context, remoteSettings.adminPassword)
            } else if (!remoteSettings.isPasswordSet) {
                EncryptedPreferencesManager.deleteAdminPassword(context)
            }
            adminSettingsDao.insertOrUpdate(remoteSettings.copy(id = 1))
        }

        Result.success("Cloud Pull Successful: ${remoteProducts.size} products, ${remoteOrders.size} orders, ${remoteRepairs.size} repairs, admin settings restored.")
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
